/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

import Estructuras.Cola;

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
    private Cola<String> logEventos;
    private int limiteProcesosEnMemoria;

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

        this.logEventos = new Cola<String>();
        this.limiteProcesosEnMemoria = 5; // valor base para decidir suspensión
    }

    /**
     * Arranca el hilo de la CPU.
     * (El hilo del Kernel se arranca con this.start() desde main.)
     */
    public void iniciarCPU() {
        cpu.start();
    }

    /**
     * Bucle principal del Kernel.
     * Avanza el reloj del sistema y coordina planificación.
     */
    @Override
    public void run() {
        while (corriendo) {

            // 1. Si la CPU está libre, le damos un proceso listo
            despacharProcesoACPU();

            // 2. Actualizamos bloqueos de E/S y suspensiones
            memoria.tickBloqueados();
            memoria.tickBloqueadosSuspendidos();

            // 3. Sumamos espera a los procesos que están listos
            planificador.incrementarEspera(memoria);

            // control de suspensión / swap
            controlarSuspension();

            // 4. Avanzar reloj global
            reloj = reloj + 1;

            // 5. Dormir un ciclo "de SO"
            try {
                Thread.sleep(duracionCicloMS);
            } catch (InterruptedException e) {
                // no hacemos nada especial
            }
        }

        // Cuando salimos del while, apagamos la CPU
        apagarCPU();
    }

    /**
     * Si la CPU está libre, se selecciona un proceso listo usando
     * la política de planificación y se carga en la CPU.
     */
    private void despacharProcesoACPU() {
        if (cpu.estaLibre()) {
            PCB candidato = planificador.seleccionar(memoria);
            if (candidato != null) {
                cpu.asignarProceso(candidato, reloj);
                agregarLog("Ciclo " + reloj + ": CPU toma proceso "
                        + candidato.getNombre() + " (PID " + candidato.getId() + ")");
            }
        }
    }

    /**
     * Rutina sencilla de suspensión / reactivación.
     * Si hay demasiados procesos en memoria principal (listos+bloqueados),
     * se suspende uno. Si hay espacio libre, se reactiva uno.
     */
    private void controlarSuspension() {
        int enListos = memoria.tamanoListos();
        int enBloqueados = memoria.tamanoBloqueados();
        int totalEnMem = enListos + enBloqueados;

        // demasiados procesos cargados => intentar suspender
        if (totalEnMem > limiteProcesosEnMemoria) {
            PCB suspendido = memoria.suspenderPrimeroListo();
            if (suspendido == null) {
                suspendido = memoria.suspenderPrimeroBloqueado();
            }
            if (suspendido != null) {
                agregarLog("Ciclo " + reloj + ": Proceso "
                        + suspendido.getNombre() + " (PID " + suspendido.getId()
                        + ") pasa a SUSPENDIDO");
            }
        }

        // hay espacio => intentar reactivar un suspendido
        if (totalEnMem < limiteProcesosEnMemoria) {
            PCB reactivado = memoria.reactivarListoSuspendido();
            if (reactivado != null) {
                agregarLog("Ciclo " + reloj + ": Proceso "
                        + reactivado.getNombre() + " (PID " + reactivado.getId()
                        + ") vuelve a LISTO desde SUSPENDIDO");
            }
        }
    }

    /**
     * Detiene el kernel.
     * Esto hace que el run() salga de su ciclo.
     */
    public void detenerKernel() {
        corriendo = false;
        agregarLog("Ciclo " + reloj + ": Kernel detenido por el usuario");
    }

    /**
     * Apaga la CPU al final de la simulación.
     */
    private void apagarCPU() {
        cpu.detenerCPU();
    }

    /**
     * Admite un proceso nuevo al sistema.
     * Lo mete en estado LISTO mediante el planificador.
     *
     * @param pcb Proceso que llega al sistema.
     */
    public void admitirProceso(PCB pcb) {
        if (pcb == null) {
            return;
        }
        planificador.encolar(pcb, memoria);
        agregarLog("Ciclo " + reloj + ": Proceso "
                + pcb.getNombre() + " (PID " + pcb.getId()
                + ") admitido al sistema (LISTO)");
    }

    /**
     * Cambia la política de planificación en tiempo de ejecución.
     *
     * @param nueva Nueva política.
     */
    public void cambiarPolitica(Planificador.PoliticaPlanificacion nueva) {
        planificador.setPolitica(nueva, memoria);
        agregarLog("Ciclo " + reloj + ": Política cambiada a " + nueva);
    }

    /**
     * La CPU avisa que el proceso terminó toda su ráfaga.
     * Se mueve a TERMINADO.
     *
     * @param p proceso terminado
     */
    public void notificarTerminado(PCB p) {
        if (p == null) {
            return;
        }
        p.setEstado(Estado.TERMINADO);
        memoria.moverATerminados(p);
        agregarLog("Ciclo " + reloj + ": Proceso "
                + p.getNombre() + " (PID " + p.getId()
                + ") TERMINADO");
    }

    /**
     * La CPU avisa que el proceso pidió E/S.
     * Se pasa a BLOQUEADO por una cantidad fija de ciclos.
     *
     * @param p proceso bloqueado
     */
    public void notificarBloqueadoPorIO(PCB p) {
        if (p == null) {
            return;
        }
        memoria.moverABloqueados(p, duracionBloqueoIO);
        agregarLog("Ciclo " + reloj + ": Proceso "
                + p.getNombre() + " (PID " + p.getId()
                + ") BLOQUEADO (E/S)");
    }

    /**
     * La CPU avisa que agotó quantum (Round Robin / Feedback).
     * Lo reencolamos según la política.
     *
     * @param p proceso que usó todo su quantum
     */
    public void notificarQuantumAgotado(PCB p) {
        if (p == null) {
            return;
        }
        planificador.alExpirarQuantum(p, memoria);
        agregarLog("Ciclo " + reloj + ": Quantum agotado para proceso "
                + p.getNombre() + " (PID " + p.getId() + ")");
    }

    /**
     * La CPU avisa que desalojó un proceso porque llegó otro
     * con ráfaga restante menor (SRT).
     * Lo regresamos a LISTO.
     *
     * @param p proceso desalojado
     */
    public void notificarDesalojoPorPrioridadCPU(PCB p) {
        if (p == null) {
            return;
        }
        memoria.moverAListos(p);
        agregarLog("Ciclo " + reloj + ": Proceso "
                + p.getNombre() + " (PID " + p.getId()
                + ") desalojado y devuelto a LISTO (SRT)");
    }

    /**
     * La CPU pregunta si debe ser expropiado el proceso actual
     * bajo la política SRT.
     *
     * @param p proceso actual en CPU
     * @return true si hay otro más corto esperando
     */
    public boolean debeExpropiarSRT(PCB p) {
        return planificador.debeExpropiarSRT(p, memoria);
    }

    /**
     * Devuelve el número de ciclo actual.
     */
    public int getReloj() {
        return reloj;
    }

    /**
     * Devuelve la duración de un ciclo de reloj en ms.
     */
    public int getDuracionCicloMS() {
        return duracionCicloMS;
    }

    /**
     * Permite cambiar la velocidad de simulación.
     */
    public void setDuracionCicloMS(int nuevaDuracionMS) {
        this.duracionCicloMS = nuevaDuracionMS;
        agregarLog("Ciclo " + reloj + ": Velocidad de ciclo = " + nuevaDuracionMS + "ms");
    }

    /**
     * Para que la interfaz (cuando la hagamos) pueda ver
     * las colas y estados.
     */
    public MemoriaPrincipal getMemoria() {
        return memoria;
    }

    /**
     * Para que la interfaz pueda ver qué política está activa.
     */
    public Planificador getPlanificador() {
        return planificador;
    }

    /**
     * Para que la interfaz pueda ver qué proceso está en CPU.
     */
    public CPU getCPU() {
        return cpu;
    }

    /**
     * Devuelve el log de eventos (para la interfaz).
     */
    public Cola<String> getLogEventos() {
        return logEventos;
    }

    /**
     * Agrega una línea al log interno.
     */
    private void agregarLog(String linea) {
        logEventos.encolar(linea);
    }
}
