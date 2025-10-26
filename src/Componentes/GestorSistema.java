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
public class GestorSistema {
    private int duracionCicloMS;
    private int duracionBloqueoIO;
    private int quantumRR;
    private Planificador.PoliticaPlanificacion politicaInicial;
    private int siguientePID;
    private int rafagaCPUDefault;
    private int ioCadaNDefault;
    private int prioridadBase;
    private Cola<PCB> procesosCargados;

    public GestorSistema(
            int duracionCicloMS,
            int duracionBloqueoIO,
            int quantumRR,
            Planificador.PoliticaPlanificacion politicaInicial,
            int rafagaCPUDefault,
            int ioCadaNDefault,
            int prioridadBase
    ) {
        this.duracionCicloMS = duracionCicloMS;
        this.duracionBloqueoIO = duracionBloqueoIO;
        this.quantumRR = quantumRR;
        this.politicaInicial = politicaInicial;

        this.rafagaCPUDefault = rafagaCPUDefault;
        this.ioCadaNDefault = ioCadaNDefault;
        this.prioridadBase = prioridadBase;

        this.siguientePID = 1;
        this.procesosCargados = new Cola<PCB>();
    }

    /**
     * Crea un PCB "inicial" (para el arranque) y lo guarda en procesosCargados.
     */
    public void crearProcesoInicial(String nombre, Tipo tipo) {
        int pid = siguientePID;
        siguientePID = siguientePID + 1;

        int instruccionesTotales = rafagaCPUDefault; // cantidad de instrucciones
        int prioridad = prioridadBase;
        int llegada = 0; // antes de iniciar el kernel asumimos llegada 0

        int ioCadaN;
        int duracionES;

        if (tipo == Tipo.CPU_BOUND) {
            ioCadaN   = 0;
            duracionES= 0;
        } else {
            ioCadaN   = ioCadaNDefault;
            duracionES= this.duracionBloqueoIO;
        }

        PCB pcb = new PCB(
            pid,
            nombre,
            instruccionesTotales,
            prioridad,
            llegada,
            ioCadaN,
            duracionES
        );

        pcb.setEstado(Estado.NUEVO);

        procesosCargados.encolar(pcb);
    }

    /**
     * Construye un Kernel con la configuración actual,
     * pero NO lo arranca.
     *
     */
    public Kernel construirKernel() {
        Kernel kernel = new Kernel(
            politicaInicial,
            quantumRR,
            duracionBloqueoIO,
            duracionCicloMS
        );
        return kernel;
    }

    /**
     * Igual que construirKernel(), pero tomando los valores de un PerfilSimulacion.
     */
    public Kernel construirKernelDesdePerfil(PerfilSimulacion perfil) {
        Kernel k = new Kernel(
            perfil.getPoliticaInicial(),
            perfil.getQuantumRR(),
            perfil.getDuracionBloqueoIO(),
            perfil.getDuracionCicloMS()
        );
        return k;
    }

    
    public Cola<PCB> getProcesosCargados() {
        return procesosCargados;
    }

    public int getDuracionCicloMS() {
        return duracionCicloMS;
    }

    public int getDuracionBloqueoIO() {
        return duracionBloqueoIO;
    }

    public int getQuantumRR() {
        return quantumRR;
    }

    public Planificador.PoliticaPlanificacion getPoliticaInicial() {
        return politicaInicial;
    }

    public int getPrioridadBase() {
        return prioridadBase;
    }

    public int getRafagaCPUDefault() {
        return rafagaCPUDefault;
    }

    public int getIoCadaNDefault() {
        return ioCadaNDefault;
    }

    public void setDuracionCicloMS(int nuevaDuracion) {
        this.duracionCicloMS = nuevaDuracion;
    }

    public void setQuantumRR(int nuevoQuantum) {
        this.quantumRR = nuevoQuantum;
    }

    public void setPoliticaInicial(Planificador.PoliticaPlanificacion nueva) {
        this.politicaInicial = nueva;
    }

    /**
     * Genera PIDs únicos.
     */
    public synchronized int generarPID() {
        int pid = siguientePID;
        siguientePID = siguientePID + 1;
        return pid;
    }
}