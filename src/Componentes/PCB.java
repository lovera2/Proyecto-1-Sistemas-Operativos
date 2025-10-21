/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

/**
 *
 * @author luismarianolovera
 */
public class PCB {
    private int id;
    private String nombre;
    private Estado estado;
    private int pc;
    private int rafagaRestante;

    // HRRN: guarda la ráfaga original
    private Integer rafagaInicial;

    private int tiempoLlegada;
    private int tiempoEspera;
    private int tiempoServicio;
    private Integer primeraVezCPU;

    private int ioCadaN;
    private int bloqueoRestante;
    private int prioridad;
    private int quantumRestante;

    public PCB(int id, String nombre, int rafagaInicialValor, int prioridad, int tiempoLlegada, int ioCadaN) {
        this.id = id;
        this.nombre = nombre;
        this.rafagaRestante = rafagaInicialValor;
        this.prioridad = prioridad;
        this.tiempoLlegada = tiempoLlegada;
        this.ioCadaN = ioCadaN;

        this.estado = Estado.NUEVO;
        this.pc = 0;
        this.tiempoEspera = 0;
        this.tiempoServicio = 0;
        this.primeraVezCPU = null;
        this.bloqueoRestante = 0;
        this.quantumRestante = 0;

        // Opcional: si quieres fijarla desde el inicio
        this.rafagaInicial = rafagaInicialValor;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado nuevo) {
        this.estado = nuevo;
    }

    public int getPc() {
        return pc;
    }

    public void avanzarPC() {
        pc = pc + 1;
    }

    public int getRafagaRestante() {
        return rafagaRestante;
    }

    public void decrementarRafaga() {
        if (rafagaRestante > 0) {
            rafagaRestante = rafagaRestante - 1;
        }
    }

    // ====== HRRN helpers ======
    public void setRafagaInicialSiNula() {
        if (rafagaInicial == null) {
            rafagaInicial = rafagaRestante;
        }
    }

    public int getServicioPendiente() {
        return Math.max(1, rafagaRestante);
    }

    public Integer getRafagaInicial() {
        return rafagaInicial;
    }
    // ==========================

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int p) {
        prioridad = p;
    }

    public int getTiempoLlegada() {
        return tiempoLlegada;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }

    public void incrementarEspera() {
        tiempoEspera = tiempoEspera + 1;
    }

    public int getTiempoServicio() {
        return tiempoServicio;
    }

    public void incrementarServicio() {
        tiempoServicio = tiempoServicio + 1;
    }

    public Integer getPrimeraVezCPU() {
        return primeraVezCPU;
    }

    public void marcarPrimeraVezCPU(int ciclo) {
        if (primeraVezCPU == null) {
            primeraVezCPU = ciclo;
        }
    }

    public Integer getTiempoRespuesta() {
        if (primeraVezCPU == null) {
            return null;
        }
        return primeraVezCPU - tiempoLlegada;
    }

    public int getIoCadaN() {
        return ioCadaN;
    }

    public boolean esMomentoIO() {
        if (ioCadaN <= 0) {
            return false;
        }
        if (pc == 0) {
            return false;
        }
        return (pc % ioCadaN) == 0;
    }

    public void prepararBloqueo(int ciclosBloqueo) {
        bloqueoRestante = ciclosBloqueo;
        estado = Estado.BLOQUEADO;
    }

    public void tickBloqueo() {
        if (bloqueoRestante > 0) {
            bloqueoRestante = bloqueoRestante - 1;
        }
    }

    public boolean terminoBloqueo() {
        return bloqueoRestante == 0;
    }

    public void setQuantumRestante(int q) {
        quantumRestante = q;
    }

    public void consumirQuantum() {
        if (quantumRestante > 0) {
            quantumRestante = quantumRestante - 1;
        }
    }

    public boolean sinQuantum() {
        return quantumRestante == 0;
    }

    // MAR ilustrativo (opcional, útil para GUI)
    public int getMAR() {
        return id + pc;
    }

    @Override
    public String toString() {
        return "PCB{id=" + id + ", nombre=" + nombre + ", estado=" + estado + ", pc=" + pc + "}";
    }
}