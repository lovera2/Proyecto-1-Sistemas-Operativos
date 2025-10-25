/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;


/**
 *
 * @author luismarianolovera
 */
public class Kernel extends Thread {
    private MemoriaPrincipal memoria;
    private Planificador planificador;
    private CPU cpu;

    private int reloj;
    private int duracionCicloMS;
    private int duracionBloqueoIO;

    private volatile boolean corriendo;

    /**
     * Crea el kernel y la CPU.
     *
     * @param politica política de planificación inicial
     * @param quantumRR quantum inicial (para RR / Feedback)
     * @param duracionBloqueoIO ciclos que dura un bloqueo de E/S
     * @param duracionCicloMS duración de cada ciclo del reloj en ms
     */
    public Kernel(
            Planificador.PoliticaPlanificacion politica,
            int quantumRR,
            int duracionBloqueoIO,
            int duracionCicloMS
    ) {
        this.memoria = new MemoriaPrincipal();
        this.planificador = new Planificador(politica);
        this.planificador.setQuantumRR(quantumRR);

        this.reloj = 0;
        this.duracionCicloMS = duracionCicloMS;
        this.duracionBloqueoIO = duracionBloqueoIO;
        this.corriendo = true;

        this.cpu = new CPU(0, this);
    }

    /**
     * Inicia el hilo de la CPU.
     * (El kernel se inicia con start() aparte).
     */
    public void iniciarCPU() {
        cpu.start();
    }

    /**
     * Bucle principal del kernel.
     * Avanza el reloj, mueve procesos y despacha a CPU.
     */
    @Override
    public void run() {
        while (corriendo) {

            despacharProcesoACPU();

            memoria.tickBloqueados();
            memoria.tickBloqueadosSuspendidos();

            planificador.incrementarEspera(memoria);

            reloj = reloj + 1;

            try {
                Thread.sleep(duracionCicloMS);
            } catch (InterruptedException e) {
                // ignorar interrupción suave
            }
        }

        apagarCPU();
    }

    /**
     * Si la CPU está libre, asigna un proceso LISTO a ejecución.
     */
    private void despacharProcesoACPU() {
        if (cpu.estaLibre()) {
            PCB candidato = planificador.seleccionar(memoria);
            if (candidato != null) {
                cpu.asignarProceso(candidato, reloj);
            }
        }
    }

    /**
     * Detiene el kernel (sale del run()).
     */
    public void detenerKernel() {
        corriendo = false;
    }

    /**
     * Detiene la CPU cuando el kernel ya terminó.
     */
    private void apagarCPU() {
        cpu.detenerCPU();
    }

    /**
     * Admite un proceso nuevo al sistema (lo pone en LISTO).
     *
     * @param pcb proceso que llega
     */
    public void admitirProceso(PCB pcb) {
        if (pcb == null) {
            return;
        }
        planificador.encolar(pcb, memoria);
    }

    /**
     * Cambia la política de planificación en vivo.
     *
     * @param nueva nueva política
     */
    public void cambiarPolitica(Planificador.PoliticaPlanificacion nueva) {
        planificador.setPolitica(nueva, memoria);
    }

    /**
     * Aviso de la CPU: el proceso terminó.
     *
     * @param p proceso que terminó
     */
    public void notificarTerminado(PCB p) {
        if (p == null) {
            return;
        }
        p.setEstado(Estado.TERMINADO);
        memoria.moverATerminados(p);
    }

    /**
     * Aviso de la CPU: el proceso pidió E/S.
     *
     * @param p proceso bloqueado por E/S
     */
    public void notificarBloqueadoPorIO(PCB p) {
        if (p == null) {
            return;
        }
        memoria.moverABloqueados(p, duracionBloqueoIO);
    }

    /**
     * Aviso de la CPU: se agotó el quantum.
     *
     * @param p proceso que debe volver a listo según la política
     */
    public void notificarQuantumAgotado(PCB p) {
        if (p == null) {
            return;
        }
        planificador.alExpirarQuantum(p, memoria);
    }

    /**
     * Aviso de la CPU: desalojar el proceso actual y devolverlo a LISTO.
     *
     * @param p proceso desalojado
     */
    public void notificarDesalojoPorPrioridadCPU(PCB p) {
        if (p == null) {
            return;
        }
        memoria.moverAListos(p);
    }

    /**
     * Pregunta de la CPU: ¿hay alguien más corto? (SRT)
     *
     * @param p proceso actual en CPU
     * @return true si debe ser expropiado
     */
    public boolean debeExpropiarSRT(PCB p) {
        return planificador.debeExpropiarSRT(p, memoria);
    }

    /**
     * @return número de ciclo global
     */
    public int getReloj() {
        return reloj;
    }

    /**
     * @return duración del ciclo del reloj en ms
     */
    public int getDuracionCicloMS() {
        return duracionCicloMS;
    }

    /**
     * Cambia la velocidad de simulación.
     *
     * @param nuevaDuracionMS nueva duración en ms
     */
    public void setDuracionCicloMS(int nuevaDuracionMS) {
        this.duracionCicloMS = nuevaDuracionMS;
    }

    /**
     * @return referencia a MemoriaPrincipal (colas de listos, bloqueados, etc.)
     */
    public MemoriaPrincipal getMemoria() {
        return memoria;
    }

    /**
     * @return referencia al planificador actual
     */
    public Planificador getPlanificador() {
        return planificador;
    }

    /**
     * @return referencia a la CPU simulada
     */
    public CPU getCPU() {
        return cpu;
    }
}