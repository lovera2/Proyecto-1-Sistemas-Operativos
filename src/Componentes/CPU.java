/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

import java.util.concurrent.Semaphore;

/**
 *
 * @author luismarianolovera
 */
public class CPU extends Thread {

    private int cpuId;
    private Kernel kernel;

    /* Proceso que está corriendo en esta CPU en este momento */
    private volatile PCB procesoActual;

    /* Controla si la CPU sigue activa */
    private volatile boolean corriendo;

    /**
     * Crea la CPU.
     *
     * @param cpuId identificador de la CPU (0 en este proyecto)
     * @param kernel referencia al kernel
     */
    public CPU(int cpuId, Kernel kernel) {
        this.cpuId = cpuId;
        this.kernel = kernel;
        this.procesoActual = null;
        this.corriendo = true;
    }

    /**
     * Bucle principal de la CPU.
     * Si hay proceso asignado, ejecuta una instrucción.
     * Si no hay proceso, espera.
     */
    @Override
    public void run() {
        while (corriendo) {

            if (procesoActual == null) {
                /* CPU idle, duerme un ciclo */
                dormirUnCiclo();
                continue;
            }

            /* Ejecutar 1 instrucción del proceso */
            ejecutarUnaInstruccion();

            /* Revisar qué pasó con el proceso después de ejecutar */
            manejarEventosDelProceso();

            /* Dormir para simular duración del ciclo de CPU */
            dormirUnCiclo();
        }
    }

    /**
     * Asigna un proceso a esta CPU para que empiece / continúe su ejecución.
     *
     * @param p proceso asignado
     * @param cicloActual ciclo global actual (para métricas de respuesta)
     */
    public void asignarProceso(PCB p, int cicloActual) {
        if (p == null) {
            return;
        }
        procesoActual = p;
        p.setEstado(Estado.EJECUCION);
        p.setRafagaInicialSiNula();
        p.marcarPrimeraVezCPU(cicloActual);
    }

    /**
     * Ejecuta una sola instrucción del proceso actual.
     * Avanza PC, consume CPU y acumula servicio.
     */
    private void ejecutarUnaInstruccion() {
        if (procesoActual == null) {
            return;
        }

        procesoActual.avanzarPC();         // PC++
        procesoActual.decrementarRafaga(); // ráfaga restante--
        procesoActual.incrementarServicio(); // tiempo de servicio++
    }

    /**
     * Verifica el estado del proceso después de ejecutar
     * y le avisa al kernel si:
     * - terminó
     * - pidió E/S
     * - debe ser desalojado por SRT
     */
    private void manejarEventosDelProceso() {
        if (procesoActual == null) {
            return;
        }

        /* 1. ¿Terminó toda su ráfaga de CPU? */
        if (procesoActual.getRafagaRestante() <= 0) {
            PCB fin = procesoActual;
            procesoActual = null;
            kernel.notificarTerminado(fin);
            return;
        }

        /* 2. ¿Pidió E/S en este punto? (proceso I/O bound) */
        if (procesoActual.esMomentoIO()) {
            PCB bloqueado = procesoActual;
            procesoActual = null;
            kernel.notificarBloqueadoPorIO(bloqueado);
            return;
        }

        /* 3. ¿Debo desalojarlo por SRT (llega otro más corto)? */
        if (kernel.debeExpropiarSRT(procesoActual)) {
            PCB desalojado = procesoActual;
            procesoActual = null;
            kernel.notificarDesalojoPorPrioridadCPU(desalojado);
            return;
        }

        /* Nota: manejo de quantum (RR / Feedback) se haría aquí.
           Por ahora no lo incluimos para compilar sin errores. */
    }

    /**
     * Marca la CPU como detenida.
     * El hilo sale del run().
     */
    public void detenerCPU() {
        corriendo = false;
        this.interrupt();
    }

    /**
     * Indica si la CPU no tiene proceso asignado.
     *
     * @return true si está libre
     */
    public boolean estaLibre() {
        return procesoActual == null;
    }

    /**
     * Devuelve el proceso que está corriendo en este instante.
     *
     * @return PCB actual o null si está libre
     */
    public PCB getProcesoActual() {
        return procesoActual;
    }

    /**
     * Devuelve el id lógico de esta CPU.
     *
     * @return id de CPU
     */
    public int getCpuId() {
        return cpuId;
    }

    /**
     * Duerme la CPU el tiempo configurado por el kernel.
     */
    private void dormirUnCiclo() {
        try {
            Thread.sleep(kernel.getDuracionCicloMS());
        } catch (InterruptedException e) {
            // si nos interrumpen el sleep, seguimos
        }
    }
}