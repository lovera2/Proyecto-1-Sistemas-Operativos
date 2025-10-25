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
public class Kernel {
    private MemoriaPrincipal memoria;
    private Planificador planificador;
    private CPU cpu;
    private int duracionBloqueoIO;
    private int reloj;

    /**
     * Constructor del Kernel.
     * 
     * @param politica Algoritmo de planificación inicial (FCFS, RR, etc.).
     * @param quantumRR Quantum inicial para Round Robin (solo se usa si la política es RR).
     * @param duracionBloqueoIO Cantidad fija de ciclos que dura un bloqueo de E/S.
     */
    public Kernel(Planificador.PoliticaPlanificacion politica, int quantumRR, int duracionBloqueoIO) {
        this.memoria = new MemoriaPrincipal();
        this.planificador = new Planificador(politica);
        this.planificador.setQuantumRR(quantumRR);
        this.duracionBloqueoIO = duracionBloqueoIO;
        this.cpu = new CPU();
        this.reloj = 0;
    }

    /**
     * Devuelve el ciclo actual del reloj global.
     * 
     * @return ciclo actual de simulación.
     */
    public int getReloj() {
        return reloj;
    }

    /**
     * Inserta un proceso nuevo en el sistema.
     * 
     * Este método sería llamado cuando llega un proceso "nuevo" del mundo exterior.
     * El Kernel lo pasa a la estructura correspondiente según la política de planificación.

     * @param pcb Proceso que ingresa al sistema.
     */
    public void admitirProceso(PCB pcb) {
        if (pcb == null) {
            return;
        }
        planificador.encolar(pcb, memoria);
    }

    /**
     * Ejecuta un ciclo de CPU completo y actualiza el estado del sistema.
     * Este es el "paso" básico de la simulación.
     */
    public void tick() {

        // 1) Despachar si la CPU está libre
        if (cpu.estaLibre()) {
            PCB siguiente = planificador.seleccionar(memoria);
            if (siguiente != null) {
                cpu.cargarProceso(siguiente, reloj);
            }
        }

        // 2) Ejecutar un ciclo de CPU (si hay proceso actual)
        cpu.ejecutarCiclo();

        // 3) Revisar el estado del proceso que estaba corriendo
        PCB enCPU = cpu.getProcesoActual();

        if (enCPU != null) {

            // 3.a) ¿Terminó toda su ráfaga?
            if (cpu.terminoProcesoActual()) {
                enCPU.setEstado(Estado.TERMINADO);
                memoria.moverATerminados(enCPU);
                cpu.retirarProceso(); // CPU queda libre
            }

            // 3.b) ¿Pidió E/S?
            else if (cpu.requiereIO()) {
                // Lo removemos de CPU y lo pasamos a BLOQUEADO
                cpu.retirarProceso();
                memoria.moverABloqueados(enCPU, duracionBloqueoIO);
            }

            // 3.c) ¿Se acabó el quantum? (aplica a RR y Feedback)
            else if (cpu.quantumAgotado()) {
                cpu.retirarProceso();
                planificador.alExpirarQuantum(enCPU, memoria);
            }

            // 3.d) Si nada de lo anterior, el proceso sigue en CPU el próximo tick
        }

        // 4) Avanzar el tiempo de bloqueo E/S para procesos BLOQUEADOS
        memoria.tickBloqueados();

        // 4.b) También avanzar bloqueo en los BLOQUEADO_SUSPENDIDO
        memoria.tickBloqueadosSuspendidos();

        // 5) Sumar tiempo de espera a los procesos que están esperando CPU
        planificador.incrementarEspera(memoria);

        // 6) Avanzar reloj global
        reloj = reloj + 1;
    }

    /**
     * Ejecuta varios ciclos seguidos de simulación.
     * 
     * @param cantidad Ciclos de reloj a simular.
     */
    public void simular(int cantidad) {
        int i = 0;
        while (i < cantidad) {
            tick();
            i = i + 1;
        }
    }

    /**
     * Intenta suspender un proceso que está en memoria principal
     * (por ejemplo, para simular swapping por presión de memoria).
     * 
     * Elige un candidato y lo mueve a estado LISTO_SUSPENDIDO o BLOQUEADO_SUSPENDIDO.
     * 
     * @param p Proceso a suspender.
     */
    public void suspenderProceso(PCB p) {
        if (p == null) {
            return;
        }

        // No podemos suspender un proceso que está ejecutando justo ahora
        if (cpu.getProcesoActual() == p) {
            return;
        }

        memoria.suspender(p);
    }

    /**
     * Cambia la política de planificación en tiempo de ejecución.
     * 
     * @param nueva Nueva política (FCFS, RR, SPN, SRT, HRRN, FEEDBACK).
     */
    public void cambiarPolitica(Planificador.PoliticaPlanificacion nueva) {
        planificador.setPolitica(nueva, memoria);
    }

    /**
     * Devuelve la memoria principal (para que la GUI la consulte, por ejemplo).
     * 
     * @return referencia a MemoriaPrincipal.
     */
    public MemoriaPrincipal getMemoria() {
        return memoria;
    }

    /**
     * Devuelve el planificador actual.
     * Esto sirve para que la GUI muestre la política seleccionada
     * o las colas Feedback (Q1, Q2, Q3).
     * 
     * @return referencia al Planificador.
     */
    public Planificador getPlanificador() {
        return planificador;
    }

    /**
     * Devuelve la CPU simulada.
     * Esto permite que la GUI muestre qué proceso está corriendo ahora mismo.
     * 
     * @return referencia a CPU.
     */
    public CPU getCPU() {
        return cpu;
    }
}