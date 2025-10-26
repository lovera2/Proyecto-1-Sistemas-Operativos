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
public class Planificador {
     public enum PoliticaPlanificacion {
        FCFS,
        RR,
        SPN,
        SRT,
        HRRN,
        FEEDBACK
    }

    private PoliticaPlanificacion politica;
    private int quantumRR;

    // Feedback (MLFQ): tres colas internas y sus quantums
    private Cola<PCB> q1;
    private Cola<PCB> q2;
    private Cola<PCB> q3;
    private int q1Quantum;
    private int q2Quantum;
    private int q3Quantum;

    public Planificador(PoliticaPlanificacion politica) {
        this.politica = politica;
        this.quantumRR = 3;

        if (politica == PoliticaPlanificacion.FEEDBACK) {
            this.q1 = new Cola<PCB>();
            this.q2 = new Cola<PCB>();
            this.q3 = new Cola<PCB>();
            this.q1Quantum = 2;
            this.q2Quantum = 4;
            this.q3Quantum = 8;
        }
    }

    public String nombre() {
        return politica.toString();
    }

    public boolean esFeedback() {
        return politica == PoliticaPlanificacion.FEEDBACK;
    }

    public void setPolitica(PoliticaPlanificacion nueva, MemoriaPrincipal memoria) {
        // Si salimos de FEEDBACK, drenamos Q1/Q2/Q3 hacia listos de MemoriaPrincipal
        if (this.politica == PoliticaPlanificacion.FEEDBACK && nueva != PoliticaPlanificacion.FEEDBACK) {
            drenarFeedbackAMemoria(memoria);
            this.q1 = null;
            this.q2 = null;
            this.q3 = null;
        }

        this.politica = nueva;

        // Si entramos a FEEDBACK, inicializamos Q1/Q2/Q3 (y quantums por defecto si faltaran)
        if (nueva == PoliticaPlanificacion.FEEDBACK) {
            this.q1 = new Cola<PCB>();
            this.q2 = new Cola<PCB>();
            this.q3 = new Cola<PCB>();
            if (this.q1Quantum <= 0) {
                this.q1Quantum = 2;
            }
            if (this.q2Quantum <= 0) {
                this.q2Quantum = 4;
            }
            if (this.q3Quantum <= 0) {
                this.q3Quantum = 8;
            }
        }
    }

    public void setQuantumRR(int q) {
        this.quantumRR = q;
    }

    public void setQuantumsFeedback(int q1q, int q2q, int q3q) {
        this.q1Quantum = q1q;
        this.q2Quantum = q2q;
        this.q3Quantum = q3q;
    }

    // Llegadas / Encolado a listos

    public void encolar(PCB p, MemoriaPrincipal memoria) {
        if (p == null) {
            return;
        }
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            memoria.moverAListos(p);
            return;
        }
        p.setPrioridad(1);
        p.setEstado(Estado.LISTO);
        q1.encolar(p);
    }

    // Selección del siguiente

    public PCB seleccionar(MemoriaPrincipal memoria) {
        if (politica == PoliticaPlanificacion.FCFS) {
            PCB p = memoria.sacarDeListos();
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
            }
            return p;
        }

        if (politica == PoliticaPlanificacion.RR) {
            PCB p = memoria.sacarDeListos();
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
                p.setQuantumRestante(quantumRR);
            }
            return p;
        }

        if (politica == PoliticaPlanificacion.SPN) {
            int n = memoria.cantidadListos();
            if (n == 0) {
                return null;
            }
            int mejor = -1;
            int mejorR = Integer.MAX_VALUE;
            int i = 0;
            while (i < n) {
                PCB cand = memoria.verListoEn(i);
                if (cand != null) {
                    int r = cand.getRafagaRestante();
                    if (r < mejorR) {
                        mejorR = r;
                        mejor = i;
                    }
                }
                i = i + 1;
            }
            PCB p = memoria.quitarListoEn(mejor);
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
            }
            return p;
        }

        if (politica == PoliticaPlanificacion.SRT) {
            int n = memoria.cantidadListos();
            if (n == 0) {
                return null;
            }
            int mejor = -1;
            int mejorR = Integer.MAX_VALUE;
            int i = 0;
            while (i < n) {
                PCB cand = memoria.verListoEn(i);
                if (cand != null) {
                    int r = cand.getRafagaRestante();
                    if (r < mejorR) {
                        mejorR = r;
                        mejor = i;
                    }
                }
                i = i + 1;
            }
            PCB p = memoria.quitarListoEn(mejor);
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
            }
            return p;
        }

        if (politica == PoliticaPlanificacion.HRRN) {
            int n = memoria.cantidadListos();
            if (n == 0) {
                return null;
            }
            int mejor = -1;
            double mejorIdx = -1.0;
            int i = 0;
            while (i < n) {
                PCB cand = memoria.verListoEn(i);
                if (cand != null) {
                    int W = cand.getTiempoEspera();
                    int S = Math.max(1, cand.getRafagaRestante());
                    double hrrn = (W + S) / (double) S;
                    if (hrrn > mejorIdx) {
                        mejorIdx = hrrn;
                        mejor = i;
                    }
                }
                i = i + 1;
            }
            PCB p = memoria.quitarListoEn(mejor);
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
            }
            return p;
        }

        if (politica == PoliticaPlanificacion.FEEDBACK) {
            PCB p = q1.desencolar();
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
                p.setQuantumRestante(q1Quantum);
                return p;
            }
            p = q2.desencolar();
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
                p.setQuantumRestante(q2Quantum);
                return p;
            }
            p = q3.desencolar();
            if (p != null) {
                p.setEstado(Estado.EJECUCION);
                p.setRafagaInicialSiNula();
                p.setQuantumRestante(q3Quantum);
                return p;
            }
            return null;
        }

        return null;
    }

    // Evento: expiración de quantum

    public void alExpirarQuantum(PCB p, MemoriaPrincipal memoria) {
        if (p == null) {
            return;
        }
        if (politica == PoliticaPlanificacion.RR) {
            memoria.moverAListos(p);
            return;
        }
        if (politica == PoliticaPlanificacion.FEEDBACK) {
            int pr = p.getPrioridad();
            if (pr < 3) {
                p.setPrioridad(pr + 1);
            }
            p.setEstado(Estado.LISTO);
            if (p.getPrioridad() == 1) {
                q1.encolar(p);
            } else if (p.getPrioridad() == 2) {
                q2.encolar(p);
            } else {
                q3.encolar(p);
            }
            return;
        }
        memoria.moverAListos(p);
    }
    
    public boolean usaQuantum() {
    return politica == PoliticaPlanificacion.RR
        || politica == PoliticaPlanificacion.FEEDBACK;
}

    // Evento: termina I/O (desbloqueo)

    public void alDesbloquear(PCB p, MemoriaPrincipal memoria) {
        if (p == null) {
            return;
        }
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            memoria.moverAListos(p);
            return;
        }
        p.setEstado(Estado.LISTO);
        int pr = p.getPrioridad();
        if (pr <= 1) {
            p.setPrioridad(1);
            q1.encolar(p);
        } else if (pr == 2) {
            q2.encolar(p);
        } else {
            q3.encolar(p);
        }
    }

    // Wrapper de espera por ciclo (Memoria vs. Feedback)

    public void incrementarEspera(MemoriaPrincipal memoria) {
        if (politica == PoliticaPlanificacion.FEEDBACK) {
            incrementarEsperaFeedback();
            return;
        }
        memoria.incrementarEsperaListos();
    }

    // Contabiliza espera en Q1/Q2/Q3 (solo Feedback)

    public void incrementarEsperaFeedback() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return;
        }

        int n;
        int i;

        n = q1.verTamano();
        i = 0;
        while (i < n) {
            PCB p = q1.getAt(i);
            if (p != null) {
                p.incrementarEspera();
            }
            i = i + 1;
        }

        n = q2.verTamano();
        i = 0;
        while (i < n) {
            PCB p = q2.getAt(i);
            if (p != null) {
                p.incrementarEspera();
            }
            i = i + 1;
        }

        n = q3.verTamano();
        i = 0;
        while (i < n) {
            PCB p = q3.getAt(i);
            if (p != null) {
                p.incrementarEspera();
            }
            i = i + 1;
        }
    }

    // Boost anti-inanición (sube todo a Q1)

    public void boostFeedback() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return;
        }
        int n = q2.verTamano();
        int i = 0;
        while (i < n) {
            PCB p = q2.desencolar();
            if (p != null) {
                p.setPrioridad(1);
                p.setEstado(Estado.LISTO);
                q1.encolar(p);
            }
            i = i + 1;
        }
        n = q3.verTamano();
        i = 0;
        while (i < n) {
            PCB p = q3.desencolar();
            if (p != null) {
                p.setPrioridad(1);
                p.setEstado(Estado.LISTO);
                q1.encolar(p);
            }
            i = i + 1;
        }
    }

    // Útil para SRT: analiza si conviene expropiar

    public boolean debeExpropiarSRT(PCB actual, MemoriaPrincipal memoria) {
        if (politica != PoliticaPlanificacion.SRT) {
            return false;
        }
        if (actual == null) {
            return false;
        }
        int n = memoria.cantidadListos();
        if (n == 0) {
            return false;
        }
        int mejorR = Integer.MAX_VALUE;
        int i = 0;
        while (i < n) {
            PCB cand = memoria.verListoEn(i);
            if (cand != null) {
                int r = cand.getRafagaRestante();
                if (r < mejorR) {
                    mejorR = r;
                }
            }
            i = i + 1;
        }
        return mejorR < actual.getRafagaRestante();
    }

    // Diagnóstico GUI: tamaños de colas Feedback

    public int tamanoQ1() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return 0;
        }
        return q1.verTamano();
    }

    public int tamanoQ2() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return 0;
        }
        return q2.verTamano();
    }

    public int tamanoQ3() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return 0;
        }
        return q3.verTamano();
    }

    // Drena Q1/Q2/Q3 a la cola de listos de MemoriaPrincipal (al salir de Feedback)

    private void drenarFeedbackAMemoria(MemoriaPrincipal memoria) {
        if (q1 != null) {
            int n = q1.verTamano();
            int i = 0;
            while (i < n) {
                PCB p = q1.desencolar();
                if (p != null) {
                    p.setEstado(Estado.LISTO);
                    memoria.moverAListos(p);
                }
                i = i + 1;
            }
        }
        if (q2 != null) {
            int n = q2.verTamano();
            int i = 0;
            while (i < n) {
                PCB p = q2.desencolar();
                if (p != null) {
                    p.setEstado(Estado.LISTO);
                    memoria.moverAListos(p);
                }
                i = i + 1;
            }
        }
        if (q3 != null) {
            int n = q3.verTamano();
            int i = 0;
            while (i < n) {
                PCB p = q3.desencolar();
                if (p != null) {
                    p.setEstado(Estado.LISTO);
                    memoria.moverAListos(p);
                }
                i = i + 1;
            }
        }
    }
    
    private Object[][] construirMatrizDesdeCola(Cola<PCB> cola) {
        int n = cola.verTamano();
        Object[][] data = new Object[n][6];
        for (int i = 0; i < n; i++) {
            PCB p = cola.getAt(i);
            if (p != null) {
                data[i][0] = p.getId();
                data[i][1] = p.getNombre();
                data[i][2] = p.getEstado();
                data[i][3] = p.getPc();
                data[i][4] = p.getMAR();
                data[i][5] = p.getQuantumRestante();
            } else {
                data[i] = new Object[]{"-","-","-","-","-","-"};
            }
        }
        return data;
    }
    
    public Object[][] obtenerTablaListosFeedback() {
        if (politica != PoliticaPlanificacion.FEEDBACK) {
            return new Object[0][0];
        }
    
        // concatenar q1, q2, q3
        int n1 = q1.verTamano();
        int n2 = q2.verTamano();
        int n3 = q3.verTamano();
        Object[][] data = new Object[n1 + n2 + n3][6];

        // copiar q1
        Object[][] d1 = construirMatrizDesdeCola(q1);
        for (int i = 0; i < n1; i++) {
            data[i] = d1[i];
        }

        // copiar q2
        Object[][] d2 = construirMatrizDesdeCola(q2);
        for (int j = 0; j < n2; j++) {
            data[n1 + j] = d2[j];
        }

        // copiar q3
        Object[][] d3 = construirMatrizDesdeCola(q3);
        for (int k = 0; k < n3; k++) {
            data[n1 + n2 + k] = d3[k];
        }

        return data;
    }
    
}