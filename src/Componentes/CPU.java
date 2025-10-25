/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

/**
 *
 * @author luismarianolovera
 */
public class CPU extends Thread {
        private int idCPU;
    private Kernel kernel;
    private PCB procesoActual;
    private volatile boolean encendida;

    public CPU(int idCPU, Kernel kernel) {
        this.idCPU = idCPU;
        this.kernel = kernel;
        this.procesoActual = null;
        this.encendida = true;
    }

    @Override
    public void run() {
        while (encendida) {

            PCB p = getProcesoActual();

            if (p == null) {
                // CPU idle
                dormirCiclo();
                continue;
            }

            // Ejecutar una instrucción
            ejecutarPaso(p);

            // Simular duración de ciclo
            dormirCiclo();
        }
    }

    private void ejecutarPaso(PCB p) {

        p.setEstado(Estado.EJECUCION);

        p.avanzarPC();
        p.decrementarRafaga();
        p.incrementarServicio();
        p.consumirQuantum();

        if (p.getRafagaRestante() <= 0) {
            liberarYNotificarTerminado(p);
            return;
        }

        if (p.esMomentoIO()) {
            liberarYNotificarBloqueadoIO(p);
            return;
        }

        if (kernel.debeExpropiarSRT(p)) {
            liberarYNotificarDesalojo(p);
            return;
        }

        if (p.sinQuantum()) {
            liberarYNotificarQuantum(p);
            return;
        }
    }

    private void liberarYNotificarTerminado(PCB p) {
        quitarProcesoActual();
        kernel.notificarTerminado(p);
    }

    private void liberarYNotificarBloqueadoIO(PCB p) {
        quitarProcesoActual();
        kernel.notificarBloqueadoPorIO(p);
    }

    private void liberarYNotificarDesalojo(PCB p) {
        quitarProcesoActual();
        kernel.notificarDesalojoPorPrioridadCPU(p);
    }

    private void liberarYNotificarQuantum(PCB p) {
        quitarProcesoActual();
        kernel.notificarQuantumAgotado(p);
    }

    public synchronized void asignarProceso(PCB p, int cicloActual) {
        if (p == null) {
            return;
        }
        this.procesoActual = p;

        p.setRafagaInicialSiNula();
        p.marcarPrimeraVezCPU(cicloActual);
    }

    private synchronized PCB quitarProcesoActual() {
        PCB p = this.procesoActual;
        this.procesoActual = null;
        return p;
    }

    public synchronized PCB getProcesoActual() {
        return procesoActual;
    }

    public synchronized boolean estaLibre() {
        return procesoActual == null;
    }

    public void detenerCPU() {
        encendida = false;
    }

    private void dormirCiclo() {
        try {
            Thread.sleep(kernel.getDuracionCicloMS());
        } catch (InterruptedException e) {
            // nada especial
        }
    }

    public int getIdCPU() {
        return idCPU;
    }
}