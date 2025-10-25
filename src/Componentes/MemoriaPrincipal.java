/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Componentes;

import Estructuras.Cola;
import java.util.concurrent.Semaphore;

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

    /* Mutex para acceso exclusivo a las colas */
    private final Semaphore mutexMemoria;

    /**
     * Constructor. Inicializa todas las colas vacías y el semáforo de control.
     */
    public MemoriaPrincipal() {
        this.listos = new Cola<PCB>();
        this.bloqueados = new Cola<PCB>();
        this.terminados = new Cola<PCB>();
        this.listosSuspendidos = new Cola<PCB>();
        this.bloqueadosSuspendidos = new Cola<PCB>();

        /* Semáforo binario (1 permiso) usado como mutex */
        this.mutexMemoria = new Semaphore(1);
    }

    /* Altas básicas y movimientos entre colas principales
    */

    /**
     * Inserta un proceso al sistema operativo marcándolo como LISTO.
     * Si estaba en alguna otra cola, se elimina de ahí primero.
     *
     * @param p Proceso a admitir.
     */
    public void admitir(PCB p) {
        if (p == null) {
            return;
        }
        try {
            mutexMemoria.acquire();

            removerDeCualquierColaSinLock(p);
            p.setEstado(Estado.LISTO);
            listos.encolar(p);

        } catch (InterruptedException e) {
            // En simulación simple no hacemos nada especial
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Mueve un proceso a la cola de LISTO (estado LISTO).
     *
     * @param p Proceso que vuelve a Listo.
     */
    public void moverAListos(PCB p) {
        if (p == null) {
            return;
        }
        try {
            mutexMemoria.acquire();

            removerDeCualquierColaSinLock(p);
            p.setEstado(Estado.LISTO);
            listos.encolar(p);

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Mueve un proceso a la cola de BLOQUEADO.
     * Además configura el contador interno de bloqueo de E/S.
     *
     * @param p Proceso que pasa a bloqueo.
     * @param ciclosBloqueo Cantidad de ciclos que debe estar bloqueado.
     */
    public void moverABloqueados(PCB p, int ciclosBloqueo) {
        if (p == null) {
            return;
        }
        try {
            mutexMemoria.acquire();

            removerDeCualquierColaSinLock(p);
            p.prepararBloqueo(ciclosBloqueo);
            bloqueados.encolar(p);

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Marca un proceso como TERMINADO y lo coloca en la cola de terminados.
     *
     * @param p Proceso que ya terminó su ejecución.
     */
    public void moverATerminados(PCB p) {
        if (p == null) {
            return;
        }
        try {
            mutexMemoria.acquire();

            removerDeCualquierColaSinLock(p);
            p.setEstado(Estado.TERMINADO);
            terminados.encolar(p);

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Obtiene y retira el siguiente proceso LISTO para ejecución (tipo FCFS).
     * 
     * @return PCB listo o null si la cola está vacía.
     */
    public PCB sacarDeListos() {
        try {
            mutexMemoria.acquire();
            return listos.desencolar();
        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Indica si hay al menos un proceso listo para ejecutar.
     * 
     * @return true si la cola de listos no está vacía.
     */
    public boolean hayListos() {
        try {
            mutexMemoria.acquire();
            return !listos.esVacia();
        } catch (InterruptedException e) {
            return false;
        } finally {
            mutexMemoria.release();
        }
    }

    /*
       Suspensión y reactivación (swap out / swap in)
    */

    /**
     * Suspende el primer proceso en LISTO.
     * Pasa de LISTO -> LISTO_SUSPENDIDO.
     * 
     * @return Proceso suspendido o null si no había.
     */
    public PCB suspenderPrimeroListo() {
        try {
            mutexMemoria.acquire();

            PCB p = listos.desencolar();
            if (p == null) {
                return null;
            }
            p.setEstado(Estado.LISTO_SUSPENDIDO);
            listosSuspendidos.encolar(p);
            return p;

        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Suspende el primer proceso en BLOQUEADO.
     * Pasa de BLOQUEADO -> BLOQUEADO_SUSPENDIDO.
     * 
     * @return Proceso suspendido o null si no había.
     */
    public PCB suspenderPrimeroBloqueado() {
        try {
            mutexMemoria.acquire();

            PCB p = bloqueados.desencolar();
            if (p == null) {
                return null;
            }
            p.setEstado(Estado.BLOQUEADO_SUSPENDIDO);
            bloqueadosSuspendidos.encolar(p);
            return p;

        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Suspende un proceso específico según su estado actual.
     * LISTO -> LISTO_SUSPENDIDO
     * BLOQUEADO -> BLOQUEADO_SUSPENDIDO
     * 
     * Si el proceso está ejecutando en CPU, el Kernel debe evitar llamarla.
     *
     * @param p Proceso a suspender.
     */
    public void suspender(PCB p) {
        if (p == null) {
            return;
        }
        try {
            mutexMemoria.acquire();

            /* Lo saco de donde esté actualmente */
            removerDeCualquierColaSinLock(p);

            /* Según su estado actual, lo paso a la cola suspendida apropiada */
            if (p.getEstado() == Estado.LISTO) {
                p.setEstado(Estado.LISTO_SUSPENDIDO);
                listosSuspendidos.encolar(p);
            } else if (p.getEstado() == Estado.BLOQUEADO) {
                p.setEstado(Estado.BLOQUEADO_SUSPENDIDO);
                bloqueadosSuspendidos.encolar(p);
            } else {
                /* No hacemos nada si no aplica suspensión en ese estado */
            }

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Reactiva un proceso previamente suspendido en estado LISTO_SUSPENDIDO.
     * Pasa LISTO_SUSPENDIDO -> LISTO.
     *
     * @return Proceso reactivado o null si no había.
     */
    public PCB reactivarListoSuspendido() {
        try {
            mutexMemoria.acquire();

            PCB p = listosSuspendidos.desencolar();
            if (p == null) {
                return null;
            }
            p.setEstado(Estado.LISTO);
            listos.encolar(p);
            return p;

        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Reactiva un proceso previamente suspendido en BLOQUEADO_SUSPENDIDO.
     * Pasa BLOQUEADO_SUSPENDIDO -> BLOQUEADO.
     *
     * @return Proceso reactivado o null si no había.
     */
    public PCB reactivarBloqueadoSuspendido() {
        try {
            mutexMemoria.acquire();

            PCB p = bloqueadosSuspendidos.desencolar();
            if (p == null) {
                return null;
            }
            p.setEstado(Estado.BLOQUEADO);
            bloqueados.encolar(p);
            return p;

        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /* Avance de tiempo: bloqueo de E/S y espera
    */

    /**
     * Avanza un ciclo de reloj para los procesos BLOQUEADOS.
     * Disminuye su contador de bloqueo.
     * Si un proceso ya terminó su espera de E/S, vuelve a LISTO.
     */
    public void tickBloqueados() {
        try {
            mutexMemoria.acquire();

            int n = bloqueados.verTamano();
            int i = 0;
            while (i < n) {
                PCB p = bloqueados.getAt(i);
                if (p != null) {
                    p.tickBloqueo();
                    if (p.terminoBloqueo()) {
                        /* Saca de bloqueados y lo pasa a listo */
                        bloqueados.removeAt(i);
                        p.setEstado(Estado.LISTO);
                        listos.encolar(p);

                        n = n - 1;   // porque la cola ahora es más chica
                        continue;    // no incrementes i, revisa misma posición otra vez
                    }
                }
                i = i + 1;
            }

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Avanza un ciclo de reloj para los procesos BLOQUEADO_SUSPENDIDO.
     * Si terminan su E/S, pasan a LISTO_SUSPENDIDO.
     */
    public void tickBloqueadosSuspendidos() {
        try {
            mutexMemoria.acquire();

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

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Suma 1 ciclo de espera a todos los procesos que están en LISTO.
     * Esto se usa en métricas como HRRN.
     */
    public void incrementarEsperaListos() {
        try {
            mutexMemoria.acquire();

            int n = listos.verTamano();
            int i = 0;
            while (i < n) {
                PCB p = listos.getAt(i);
                if (p != null) {
                    p.incrementarEspera();
                }
                i = i + 1;
            }

        } catch (InterruptedException e) {
        } finally {
            mutexMemoria.release();
        }
    }

    /* Mini-API de ayuda para el planificador (mirar listos, sacar por índice...)
       */

    /**
     * Devuelve el proceso que está en la posición i de la cola LISTO
     * sin removerlo.
     *
     * @param i índice dentro de la cola de listos.
     * @return PCB en esa posición o null si está fuera de rango.
     */
    public PCB verListoEn(int i) {
        try {
            mutexMemoria.acquire();
            return listos.getAt(i);
        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Quita y devuelve el proceso que está en la posición i de la cola LISTO.
     *
     * @param i índice del proceso a quitar.
     * @return PCB removido o null si el índice no existe.
     */
    public PCB quitarListoEn(int i) {
        try {
            mutexMemoria.acquire();
            return listos.removeAt(i);
        } catch (InterruptedException e) {
            return null;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * Cantidad de procesos listos actualmente.
     *
     * @return número de elementos en la cola de listos.
     */
    public int cantidadListos() {
        try {
            mutexMemoria.acquire();
            return listos.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /* Getters útiles para interfaz / métricas en vivo
    */

    /**
     * @return tamaño de la cola LISTO.
     */
    public int tamanoListos() {
        try {
            mutexMemoria.acquire();
            return listos.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return tamaño de la cola BLOQUEADO.
     */
    public int tamanoBloqueados() {
        try {
            mutexMemoria.acquire();
            return bloqueados.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return tamaño de la cola TERMINADO.
     */
    public int tamanoTerminados() {
        try {
            mutexMemoria.acquire();
            return terminados.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return tamaño de la cola LISTO_SUSPENDIDO.
     */
    public int tamanoListosSuspendidos() {
        try {
            mutexMemoria.acquire();
            return listosSuspendidos.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return tamaño de la cola BLOQUEADO_SUSPENDIDO.
     */
    public int tamanoBloqueadosSuspendidos() {
        try {
            mutexMemoria.acquire();
            return bloqueadosSuspendidos.verTamano();
        } catch (InterruptedException e) {
            return 0;
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return representación en texto de la cola de listos.
     */
    public String verColaListos() {
        try {
            mutexMemoria.acquire();
            return listos.mostrarCola();
        } catch (InterruptedException e) {
            return "";
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return representación en texto de la cola de bloqueados.
     */
    public String verColaBloqueados() {
        try {
            mutexMemoria.acquire();
            return bloqueados.mostrarCola();
        } catch (InterruptedException e) {
            return "";
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return representación en texto de la cola de terminados.
     */
    public String verColaTerminados() {
        try {
            mutexMemoria.acquire();
            return terminados.mostrarCola();
        } catch (InterruptedException e) {
            return "";
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return representación en texto de la cola de listos suspendidos.
     */
    public String verColaListosSuspendidos() {
        try {
            mutexMemoria.acquire();
            return listosSuspendidos.mostrarCola();
        } catch (InterruptedException e) {
            return "";
        } finally {
            mutexMemoria.release();
        }
    }

    /**
     * @return representación en texto de la cola de bloqueados suspendidos.
     */
    public String verColaBloqueadosSuspendidos() {
        try {
            mutexMemoria.acquire();
            return bloqueadosSuspendidos.mostrarCola();
        } catch (InterruptedException e) {
            return "";
        } finally {
            mutexMemoria.release();
        }
    }

    /*
    * Método de uso interno.
    * Quita el proceso de cualquier cola en la que pueda estar.
    * IMPORTANTE: este método asume que ya se tomó el mutex antes de llamarlo.
    */
    private boolean removerDeCualquierColaSinLock(PCB p) {
        boolean removido = false;

        /* Intentar quitarlo de LISTO */
        int n = listos.verTamano();
        int i = 0;
        while (i < n) {
            if (listos.getAt(i) == p) {
                listos.removeAt(i);
                removido = true;
                n = n - 1;
                continue;
            }
            i = i + 1;
        }

        /* Intentar quitarlo de BLOQUEADO */
        n = bloqueados.verTamano();
        i = 0;
        while (i < n) {
            if (bloqueados.getAt(i) == p) {
                bloqueados.removeAt(i);
                removido = true;
                n = n - 1;
                continue;
            }
            i = i + 1;
        }

        /* Intentar quitarlo de TERMINADO */
        n = terminados.verTamano();
        i = 0;
        while (i < n) {
            if (terminados.getAt(i) == p) {
                terminados.removeAt(i);
                removido = true;
                n = n - 1;
                continue;
            }
            i = i + 1;
        }

        /* Intentar quitarlo de LISTO_SUSPENDIDO */
        n = listosSuspendidos.verTamano();
        i = 0;
        while (i < n) {
            if (listosSuspendidos.getAt(i) == p) {
                listosSuspendidos.removeAt(i);
                removido = true;
                n = n - 1;
                continue;
            }
            i = i + 1;
        }

        /* Intentar quitarlo de BLOQUEADO_SUSPENDIDO */
        n = bloqueadosSuspendidos.verTamano();
        i = 0;
        while (i < n) {
            if (bloqueadosSuspendidos.getAt(i) == p) {
                bloqueadosSuspendidos.removeAt(i);
                removido = true;
                n = n - 1;
                continue;
            }
            i = i + 1;
        }

        return removido;
    }
}