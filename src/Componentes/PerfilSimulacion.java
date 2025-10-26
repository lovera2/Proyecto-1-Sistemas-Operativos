/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import Estructuras.Cola;


/**
 *
 * @author luismarianolovera
 */
public class PerfilSimulacion {
    private int duracionCicloMS;
    private int duracionBloqueoIO;
    private int quantumRR;
    private Planificador.PoliticaPlanificacion politicaInicial;
    private int rafagaCPUDefault;
    private int ioCadaNDefault;
    private int prioridadBase;
    private Cola<PCB> procesosIniciales;
    private String rutaArchivo;

    private static class ConfigFile {
        int duracionCicloMS;
        int duracionBloqueoIO;
        int quantumRR;
        String politicaInicial;
        int rafagaCPUDefault;
        int ioCadaNDefault;
        int prioridadBase;

        ProcesoConfig[] procesos;
    }

    /**
     * Representación serializable de cada proceso en el JSON.
     */
    private static class ProcesoConfig {
        String nombre;
        int rafagaInicial;   
        int prioridad;
        int llegada;         // ciclo de llegada inicial
        int ioCadaN;         // cada cuántas instrucciones pide I/O (0 = nunca)
        int duracionES;      // cuántos ciclos queda bloqueado cuando pide I/O

        ProcesoConfig() {
        }

        ProcesoConfig(String nombre, int rafagaInicial, int prioridad, int llegada, int ioCadaN, int duracionES) {
            this.nombre = nombre;
            this.rafagaInicial = rafagaInicial;
            this.prioridad = prioridad;
            this.llegada = llegada;
            this.ioCadaN = ioCadaN;
            this.duracionES = duracionES;
        }
    }

    public PerfilSimulacion(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.duracionCicloMS = 200;
        this.duracionBloqueoIO = 5;
        this.quantumRR = 3;
        this.politicaInicial = Planificador.PoliticaPlanificacion.FCFS;

        this.rafagaCPUDefault = 8;
        this.ioCadaNDefault = 3;
        this.prioridadBase = 1;

        this.procesosIniciales = new Cola<PCB>();
    }

    public boolean cargarDesdeJSON() {
        try (Reader r = new FileReader(rutaArchivo)) {
            Gson gson = new Gson();
            ConfigFile cfg = gson.fromJson(r, ConfigFile.class);

            if (cfg == null) {
                System.err.println("JSON vacío o inválido.");
                return false;
            }

            // Copiar parámetros globales
            this.duracionCicloMS   = cfg.duracionCicloMS;
            this.duracionBloqueoIO = cfg.duracionBloqueoIO;
            this.quantumRR         = cfg.quantumRR;
            this.politicaInicial   = parsePolitica(cfg.politicaInicial);

            this.rafagaCPUDefault  = cfg.rafagaCPUDefault;
            this.ioCadaNDefault    = cfg.ioCadaNDefault;
            this.prioridadBase     = cfg.prioridadBase;

            // Limpiar procesosIniciales actuales
            this.procesosIniciales = new Cola<PCB>();

            // Recrear cada PCB y encolarlo
            int nextPid = 1;
            if (cfg.procesos != null) {
                int i = 0;
                while (i < cfg.procesos.length) {
                    ProcesoConfig pcfg = cfg.procesos[i];
                    if (pcfg != null) {
                        PCB pcb = new PCB(
                            nextPid,
                            pcfg.nombre,
                            pcfg.rafagaInicial,
                            pcfg.prioridad,
                            pcfg.llegada,
                            pcfg.ioCadaN,
                            pcfg.duracionES
                        );
                        pcb.setEstado(Estado.NUEVO);

                        procesosIniciales.encolar(pcb);

                        nextPid = nextPid + 1;
                    }
                    i = i + 1;
                }
            }

            return true;

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error leyendo JSON: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarEnJSON() {
        try (Writer w = new FileWriter(rutaArchivo)) {
            // volcar procesosIniciales (Cola<PCB>) a un arreglo ProcesoConfig[]
            int n = procesosIniciales.verTamano();
            ProcesoConfig[] arr = new ProcesoConfig[n];

            int i = 0;
            while (i < n) {
                PCB p = procesosIniciales.getAt(i);
                if (p == null) {
                    // relleno seguro si algo raro vino null
                    arr[i] = new ProcesoConfig(
                        "VACIO", 0, 0, 0, 0, 0
                    );
                } else {
                    int rafagaInicialSegura = (p.getRafagaInicial() != null)
                            ? p.getRafagaInicial()
                            : p.getRafagaRestante();

                    arr[i] = new ProcesoConfig(
                        p.getNombre(),
                        rafagaInicialSegura,
                        p.getPrioridad(),
                        p.getTiempoLlegada(),
                        p.getIoCadaN(),
                        p.getIoDuracionBloqueo()
                    );
                }
                i = i + 1;
            }

            // armar el objeto raíz para serializar
            ConfigFile cfg = new ConfigFile();
            cfg.duracionCicloMS   = this.duracionCicloMS;
            cfg.duracionBloqueoIO = this.duracionBloqueoIO;
            cfg.quantumRR         = this.quantumRR;
            cfg.politicaInicial   = politicaToString(this.politicaInicial);
            cfg.rafagaCPUDefault  = this.rafagaCPUDefault;
            cfg.ioCadaNDefault    = this.ioCadaNDefault;
            cfg.prioridadBase     = this.prioridadBase;
            cfg.procesos          = arr;

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(cfg, w);

            return true;

        } catch (IOException e) {
            System.err.println("Error escribiendo JSON: " + e.getMessage());
            return false;
        }
    }

 
    // Helpers para política (enum <-> String)
    private Planificador.PoliticaPlanificacion parsePolitica(String s) {
        if (s == null) {
            return Planificador.PoliticaPlanificacion.FCFS;
        }
        if (s.equals("FCFS"))     return Planificador.PoliticaPlanificacion.FCFS;
        if (s.equals("RR"))       return Planificador.PoliticaPlanificacion.RR;
        if (s.equals("SPN"))      return Planificador.PoliticaPlanificacion.SPN;
        if (s.equals("SRT"))      return Planificador.PoliticaPlanificacion.SRT;
        if (s.equals("HRRN"))     return Planificador.PoliticaPlanificacion.HRRN;
        if (s.equals("FEEDBACK")) return Planificador.PoliticaPlanificacion.FEEDBACK;
        return Planificador.PoliticaPlanificacion.FCFS;
    }

    private String politicaToString(Planificador.PoliticaPlanificacion p) {
        return p.toString();
    }

    
    // Getters y setters
    
    public int getDuracionCicloMS() {
        return duracionCicloMS;
    }

    public void setDuracionCicloMS(int duracionCicloMS) {
        this.duracionCicloMS = duracionCicloMS;
    }

    public int getDuracionBloqueoIO() {
        return duracionBloqueoIO;
    }

    public void setDuracionBloqueoIO(int duracionBloqueoIO) {
        this.duracionBloqueoIO = duracionBloqueoIO;
    }

    public int getQuantumRR() {
        return quantumRR;
    }

    public void setQuantumRR(int quantumRR) {
        this.quantumRR = quantumRR;
    }

    public Planificador.PoliticaPlanificacion getPoliticaInicial() {
        return politicaInicial;
    }

    public void setPoliticaInicial(Planificador.PoliticaPlanificacion politicaInicial) {
        this.politicaInicial = politicaInicial;
    }

    public int getRafagaCPUDefault() {
        return rafagaCPUDefault;
    }

    public void setRafagaCPUDefault(int rafagaCPUDefault) {
        this.rafagaCPUDefault = rafagaCPUDefault;
    }

    public int getIoCadaNDefault() {
        return ioCadaNDefault;
    }

    public void setIoCadaNDefault(int ioCadaNDefault) {
        this.ioCadaNDefault = ioCadaNDefault;
    }

    public int getPrioridadBase() {
        return prioridadBase;
    }

    public void setPrioridadBase(int prioridadBase) {
        this.prioridadBase = prioridadBase;
    }

    public Cola<PCB> getProcesosIniciales() {
        return procesosIniciales;
    }

    public void setProcesosIniciales(Cola<PCB> procesosIniciales) {
        this.procesosIniciales = procesosIniciales;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }
}
    
