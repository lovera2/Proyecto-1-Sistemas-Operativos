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
    private volatile boolean vivo; 
    private Cola<String> logEventos;
    private int limiteProcesosEnMemoria;
    private boolean yaArranqueHilo = false;
    
    //Necesario para desarrollo de grafica
    private static final int HIST_MAX = 1000;
    private int[] histTiempo = new int[HIST_MAX];
    private int[] histUsoCPU = new int[HIST_MAX];
    private int histCount = 0;
    private int totalTerminados = 0;
    private int sumaTurnaround = 0;
    private int sumaEspera = 0;

    private int ciclosTotalesSimulados = 0;
    private int ciclosTotalesCPUOcupada = 0;

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
        this.corriendo = false;
        this.vivo = true;   
        this.cpu = new CPU(0, this);

        this.logEventos = new Cola<String>();
        this.limiteProcesosEnMemoria = 5; // valor base para decidir suspensión
    }

    /**
     * Arranca el hilo de la CPU.
     */
    public void iniciarKernel() {
        if (!yaArranqueHilo) {
            yaArranqueHilo = true;
            corriendo = true;
            this.start();
            cpu.start();
            agregarLog("Kernel iniciado por el usuario");
        } else {
            // ya estaba creado antes, solo lo reanudo
            corriendo = true;
            agregarLog("Kernel reanudado por el usuario");
        }
    }
    
    public void pausarKernel() {
        corriendo = false;
        agregarLog("Ciclo " + reloj + ": Kernel pausado por el usuario");
    }

    /**
     * Bucle principal del Kernel.
     * Avanza el reloj del sistema y coordina planificación.
     */
    @Override
    public void run() {
        while (vivo) {

        // si está pausado, no avanzar reloj ni nada
        if (!corriendo) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            continue;
        }

        memoria.tickBloqueados();
        memoria.tickBloqueadosSuspendidos();

        reubicarListosEnFeedback();
        despacharProcesoACPU();
        planificador.incrementarEspera(memoria);
        controlarSuspension();

        int usoActualCPU = (cpu.getProcesoActual() != null) ? 100 : 0;

        if (histCount < HIST_MAX) {
            histTiempo[histCount] = reloj;
            histUsoCPU[histCount] = usoActualCPU;
            histCount = histCount + 1;
        }
        
        ciclosTotalesSimulados = ciclosTotalesSimulados + 1;
        if (cpu.getProcesoActual() != null) {
            ciclosTotalesCPUOcupada = ciclosTotalesCPUOcupada + 1;
        }

        reloj = reloj + 1;

        try {
            Thread.sleep(duracionCicloMS);
        } catch (InterruptedException e) {}
        }
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
        agregarLog("Ciclo " + reloj + ": Kernel pausado por el usuario");
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
        
        p.setTiempoFin(reloj);
        p.setEstado(Estado.TERMINADO);
        memoria.moverATerminados(p);
        
        totalTerminados = totalTerminados + 1;

        // turnaround = (fin - llegada)
        int turnaround = reloj - p.getTiempoLlegada();
        if (turnaround < 0) {
            turnaround = 0; // seguridad
        }
        sumaTurnaround = sumaTurnaround + turnaround;

        // espera total del proceso (asumimos PCB va acumulando tiempoEspera)
        sumaEspera = sumaEspera + p.getTiempoEspera();

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
        memoria.moverABloqueados(p, p.getIoDuracionBloqueo());
        agregarLog("Ciclo " + reloj + ": Proceso " + p.getNombre() + " (PID " + p.getId() + ") BLOQUEADO (E/S)");
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
    
    private void reubicarListosEnFeedback() {
        if (!planificador.esFeedback()) {
            return;
        }

        while (memoria.hayListos()) {
            PCB p = memoria.sacarDeListos();
            if (p == null) {
                break;
            }

            planificador.alDesbloquear(p, memoria);

            agregarLog("Ciclo " + reloj + ": Proceso " + p.getNombre() + " (PID " + p.getId() + ") reubicado en Feedback (Q" + p.getPrioridad() + ") tras E/S");
        }
    }

    
    public String getResumenEstadisticas() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Estadísticas del sistema ===\n");
        sb.append("Ciclo actual: ").append(reloj).append("\n");
        sb.append("Procesos terminados: ").append(totalTerminados).append("\n");

        if (totalTerminados > 0) {
            double promTurnaround = (double) sumaTurnaround / (double) totalTerminados;
            double promEspera = (double) sumaEspera / (double) totalTerminados;
            sb.append("Turnaround promedio: ").append(promTurnaround).append("\n");
            sb.append("Espera promedio: ").append(promEspera).append("\n");
        } else {
            sb.append("Turnaround promedio: N/A\n");
            sb.append("Espera promedio: N/A\n");
        }

        double usoCPU = (ciclosTotalesSimulados == 0)
                ? 0.0
                : (100.0 * (double) ciclosTotalesCPUOcupada / (double) ciclosTotalesSimulados);

        sb.append("Uso promedio CPU: ").append(usoCPU).append(" %\n");
        sb.append("Ciclos simulados totales: ").append(ciclosTotalesSimulados).append("\n");

        return sb.toString();
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
    
    public int[] getHistTiempo() {
        return histTiempo;
    }

    public int[] getHistUsoCPU() {
        return histUsoCPU;
    }

    public int getHistCount() {
        return histCount;
    }
}
