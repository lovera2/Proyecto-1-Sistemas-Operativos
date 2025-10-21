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
public class MemoriaPrincipal {
    private Cola<PCB> listos;
    private Cola<PCB> bloqueados;
    private Cola<PCB> terminados;
    private Cola<PCB> listosSuspendidos;
    private Cola<PCB> bloqueadosSuspendidos;

    public MemoriaPrincipal() {
        this.listos = new Cola<PCB>();
        this.bloqueados = new Cola<PCB>();
        this.terminados = new Cola<PCB>();
        this.listosSuspendidos = new Cola<PCB>();
        this.bloqueadosSuspendidos = new Cola<PCB>();
    }

    /* ====== Altas y movimientos básicos ====== */

    /**
     * Admite un proceso al sistema y lo coloca en LISTO.
     */
    public void admitir(PCB p) {
        if (p == null) {
            return;
        }
        p.setEstado(Estado.LISTO);
        listos.encolar(p);
    }

    /**
     * Pasa un proceso a LISTO.
     */
    public void moverAListos(PCB p) {
        if (p == null) {
            return;
        }
        p.setEstado(Estado.LISTO);
        listos.encolar(p);
    }

    /**
     * Bloquea un proceso por I/O por la cantidad de ciclos indicada.
     */
    public void moverABloqueados(PCB p, int ciclosBloqueo) {
        if (p == null) {
            return;
        }
        p.prepararBloqueo(ciclosBloqueo);
        bloqueados.encolar(p);
    }

    /**
     * Mueve el proceso a TERMINADOS.
     */
    public void moverATerminados(PCB p) {
        if (p == null) {
            return;
        }
        p.setEstado(Estado.TERMINADO);
        terminados.encolar(p);
    }

    /**
     * Saca el próximo LISTO para ejecutar (FCFS).
     */
    public PCB sacarDeListos() {
        return listos.desencolar();
    }

    public boolean hayListos() {
        return !listos.esVacia();
    }

    /* ====== Suspensión (swap-out/in) ====== */

    /**
     * Suspende el primer proceso en LISTO (LISTO -> LISTO_SUSPENDIDO).
     * Devuelve el proceso suspendido o null si no había.
     */
    public PCB suspenderPrimeroListo() {
        PCB p = listos.desencolar();
        if (p == null) {
            return null;
        }
        p.setEstado(Estado.LISTO_SUSPENDIDO);
        listosSuspendidos.encolar(p);
        return p;
    }

    /**
     * Suspende el primer proceso en BLOQUEADO (BLOQUEADO -> BLOQUEADO_SUSPENDIDO).
     * Devuelve el proceso suspendido o null si no había.
     */
    public PCB suspenderPrimeroBloqueado() {
        PCB p = bloqueados.desencolar();
        if (p == null) {
            return null;
        }
        p.setEstado(Estado.BLOQUEADO_SUSPENDIDO);
        bloqueadosSuspendidos.encolar(p);
        return p;
    }

    /**
     * Reactiva (swap-in) uno de LISTO_SUSPENDIDO -> LISTO.
     */
    public PCB reactivarListoSuspendido() {
        PCB p = listosSuspendidos.desencolar();
        if (p == null) {
            return null;
        }
        p.setEstado(Estado.LISTO);
        listos.encolar(p);
        return p;
    }

    /**
     * Reactiva (swap-in) uno de BLOQUEADO_SUSPENDIDO -> BLOQUEADO.
     */
    public PCB reactivarBloqueadoSuspendido() {
        PCB p = bloqueadosSuspendidos.desencolar();
        if (p == null) {
            return null;
        }
        p.setEstado(Estado.BLOQUEADO);
        bloqueados.encolar(p);
        return p;
    }

    /* ====== Ticks: avance de bloqueos y espera ====== */

    /**
     * Avanza un ciclo de reloj para procesos BLOQUEADOS:
     * - decrementa su bloqueo
     * - si termina el bloqueo, pasa a LISTO
     */
    public void tickBloqueados() {
        int n = bloqueados.verTamano();
        int i = 0;
        while (i < n) {
            PCB p = bloqueados.getAt(i);
            if (p != null) {
                p.tickBloqueo();
                if (p.terminoBloqueo()) {
                    bloqueados.removeAt(i);
                    p.setEstado(Estado.LISTO);
                    listos.encolar(p);
                    n = n - 1;
                    continue;
                }
            }
            i = i + 1;
        }
    }

    /**
     * Avanza un ciclo para BLOQUEADO_SUSPENDIDO:
     * - decrementa su bloqueo
     * - si termina, pasa a LISTO_SUSPENDIDO (sigue fuera de memoria)
     */
    public void tickBloqueadosSuspendidos() {
        int n = bloqueadosSuspendidos.verTamano();
        int i = 0;
        while (i < n) {
            PCB p = bloqueadosSuspendidos.getAt(i);
            if (p != null) {
                p.tickBloqueo();
                if (p.terminoBloqueo()) {
                    bloqueadosSuspendidos.removeAt(i);
                    p.setEstado(Estado.LISTO_SUSPENDIDO);
                    listosSuspendidos.encolar(p);
                    n = n - 1;
                    continue;
                }
            }
            i = i + 1;
        }
    }

    /**
     * Incrementa el tiempo de espera de todos los procesos en LISTO.
     */
    public void incrementarEsperaListos() {
        int n = listos.verTamano();
        int i = 0;
        while (i < n) {
            PCB p = listos.getAt(i);
            if (p != null) {
                p.incrementarEspera();
            }
            i = i + 1;
        }
    }

    /* ====== Getters para UI / métricas ====== */

    public int tamanoListos() {
        return listos.verTamano();
    }

    public int tamanoBloqueados() {
        return bloqueados.verTamano();
    }

    public int tamanoTerminados() {
        return terminados.verTamano();
    }

    public int tamanoListosSuspendidos() {
        return listosSuspendidos.verTamano();
    }

    public int tamanoBloqueadosSuspendidos() {
        return bloqueadosSuspendidos.verTamano();
    }

    public String verColaListos() {
        return listos.mostrarCola();
    }

    public String verColaBloqueados() {
        return bloqueados.mostrarCola();
    }

    public String verColaTerminados() {
        return terminados.mostrarCola();
    }

    public String verColaListosSuspendidos() {
        return listosSuspendidos.mostrarCola();
    }

    public String verColaBloqueadosSuspendidos() {
        return bloqueadosSuspendidos.mostrarCola();
    }
    
}
