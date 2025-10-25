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
     * Crea un PCB con parámetros básicos y lo guarda en procesosCargados.
     * Sirve para definir los procesos que existirán cuando la simulación arranca.
     *
     * @param nombre nombre del proceso (ej: "P1")
     * @param tipo Tipo.CPU_BOUND o Tipo.IO_BOUND
     */
    public void crearProcesoInicial(String nombre, Tipo tipo) {
        int pid = siguientePID;
        siguientePID = siguientePID + 1;

        int rafaga = rafagaCPUDefault;        // instrucciones totales
        int prioridad = prioridadBase;        // prioridad inicial
        int llegada = 0;                      // entran al sistema en t=0
        int ioCadaN;

        if (tipo == Tipo.CPU_BOUND) {
            // casi nunca pide E/S
            ioCadaN = 0; // 0 => nunca dispara I/O automática
        } else {
            // IO_BOUND: dispara E/S periódico
            ioCadaN = ioCadaNDefault;
        }

        PCB pcb = new PCB(
                pid,
                nombre,
                rafaga,
                prioridad,
                llegada,
                ioCadaN
        );

        // al principio el proceso está NUEVO
        pcb.setEstado(Estado.NUEVO);

        procesosCargados.encolar(pcb);
    }

    /**
     * Inicia el sistema completo:
     * - construye el Kernel con la config guardada acá
     * - arranca el hilo de la CPU
     * - arranca el hilo del Kernel
     * - inyecta los procesos iniciales al Kernel
     *
     * @return Kernel ya corriendo
     */
    public Kernel iniciarSistema() {
        // 1. crear Kernel con la config
        Kernel kernel = new Kernel(
                politicaInicial,
                quantumRR,
                duracionBloqueoIO,
                duracionCicloMS
        );

        // 2. arrancar CPU (hilo interno de ejecución de instrucciones)
        kernel.iniciarCPU();

        // 3. inyectar procesos iniciales
        while (!procesosCargados.esVacia()) {
            PCB pcb = procesosCargados.desencolar();
            // cuando lo admitimos, el planificador lo pasa a LISTO
            kernel.admitirProceso(pcb);
        }

        // 4. arrancar el Kernel (reloj global)
        kernel.start();

        return kernel;
    }

    /**
     * Devuelve la cola con los procesos creados antes de iniciar.
     * Útil si querés mostrarlos en una vista previa.
     *
     * @return cola de PCBs creados aún no admitidos
     */
    public Cola<PCB> getProcesosCargados() {
        return procesosCargados;
    }

    /**
     * Devuelve la duración de un ciclo del reloj en ms.
     */
    public int getDuracionCicloMS() {
        return duracionCicloMS;
    }

    /**
     * Devuelve la duración típica de un bloqueo de E/S en ciclos.
     */
    public int getDuracionBloqueoIO() {
        return duracionBloqueoIO;
    }

    /**
     * Devuelve el quantum base actual.
     */
    public int getQuantumRR() {
        return quantumRR;
    }

    /**
     * Devuelve la política inicial configurada.
     */
    public Planificador.PoliticaPlanificacion getPoliticaInicial() {
        return politicaInicial;
    }

    /**
     * Cambia la duración de ciclo (ms) durante la ejecución.
     * Esto actualiza solo este gestor. Para que tenga efecto real,
     * también hay que avisarle al Kernel con kernel.setDuracionCicloMS(...).
     *
     * @param nuevaDuracion nueva duración en ms
     */
    public void setDuracionCicloMS(int nuevaDuracion) {
        this.duracionCicloMS = nuevaDuracion;
    }

    /**
     * Cambia el quantum configurado.
     * Igual que arriba: si el Kernel ya está corriendo, habría que
     * cambiarlo también allá usando su planificador.
     *
     * @param nuevoQuantum nuevo quantum base
     */
    public void setQuantumRR(int nuevoQuantum) {
        this.quantumRR = nuevoQuantum;
    }

    /**
     * Cambia la política de planificación por defecto
     * para las próximas simulaciones que se arranquen con este gestor.
     *
     * @param nueva nueva política
     */
    public void setPoliticaInicial(Planificador.PoliticaPlanificacion nueva) {
        this.politicaInicial = nueva;
    }
}
    
