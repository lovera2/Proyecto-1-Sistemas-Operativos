/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Estructuras;

/**
 * Implementación de una estructura de datos Cola.
 * Permite las operaciones básicas de encolar, desencolar y verificación de estado.
 * 
 * @param <T> Tipo genérico de los elementos almacenados en la cola
 * @author Luis Mariano Lovera
 */
public class Cola<T> {
    private Nodo<T> front;
    private Nodo<T> back;
    private int tamaño;

    /**
     * Constructor que inicializa una cola vacía.
     */
    public Cola() {
        this.front = null;
        this.back = null;
        this.tamaño = 0;
    }

    public boolean esVacia() {
        return front == null;
    }

    public int verTamano() {
        return tamaño;
    }

    public void vaciar() {
        front = null;
        back = null;
        tamaño = 0;
    }

    public void encolar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        if (esVacia()) {
            front = back = nuevoNodo;
        } else {
            back.setNext(nuevoNodo);
            back = nuevoNodo;
        }
        tamaño++;
    }

    public T desencolar() {
        if (esVacia()) {
            return null;
        }
        T dato = front.getDato();
        front = front.getNext();
        tamaño--;
        if (front == null) {
            back = null;
        }
        return dato;
    }

    public T verFrente() {
        if (esVacia()) {
            return null;
        }
        return front.getDato();
    }

    public T verFinal() {
        if (esVacia()) {
            return null;
        }
        return back.getDato();
    }

    public String mostrarCola() {
        String cadena = "";
        Nodo<T> aux = front;
        while (aux != null) {
            cadena = cadena + aux.getDato() + "\n";
            aux = aux.getNext();
        }
        return cadena;
    }

    public T getAt(int i) {
        if (i < 0) {
            return null;
        }
        if (i >= tamaño) {
            return null;
        }
        Nodo<T> p = front;
        int k = 0;
        while (k < i) {
            p = p.getNext();
            k = k + 1;
        }
        return p.getDato();
    }

    public T removeAt(int i) {
        if (i < 0) {
            return null;
        }
        if (i >= tamaño) {
            return null;
        }
        if (i == 0) {
            return desencolar();
        }
        Nodo<T> prev = front;
        int k = 0;
        while (k < i - 1) {
            prev = prev.getNext();
            k = k + 1;
        }
        Nodo<T> del = prev.getNext();
        T dato = del.getDato();
        prev.setNext(del.getNext());
        if (del == back) {
            back = prev;
        }
        tamaño = tamaño - 1;
        return dato;
    }

    /* Getters y Setters con tipos genéricos */
    public Nodo<T> getFront() {
        return front;
    }

    public void setFront(Nodo<T> front) {
        this.front = front;
    }

    public Nodo<T> getBack() {
        return back;
    }

    public void setBack(Nodo<T> back) {
        this.back = back;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void setTamaño(int tamaño) {
        this.tamaño = tamaño;
    }
}