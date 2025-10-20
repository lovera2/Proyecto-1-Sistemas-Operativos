/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Estructuras;

/**
 * Implementación de un nodo para estructuras de datos.
 * Cada nodo contiene un dato genérico y una referencia al siguiente nodo en la secuencia.
 * 
 * @param <T> Tipo genérico del dato almacenado en el nodo
 * @author Luis Mariano Lovera
 */
public class Nodo<T> {
    private T dato;
    private Nodo<T> next;
    
    /**
     * Constructor que crea un nuevo nodo con el dato especificado.
     * El siguiente nodo se inicializa como null.
     * 
     * @param newDato Dato a almacenar en el nodo
     */
    public Nodo(T newDato) {
        this.dato = newDato;
        this.next = null;
    }

    /**
     * Obtiene el dato almacenado en el nodo.
     * 
     * @return Dato contenido en el nodo
     */
    public T getDato() {
        return dato;
    }

    /**
     * Establece o modifica el dato almacenado en el nodo.
     * 
     * @param dato Nuevo dato a almacenar
     */
    public void setDato(T dato) {
        this.dato = dato;
    }

    /**
     * Obtiene la referencia al siguiente nodo en la secuencia.
     * 
     * @return Referencia al siguiente nodo (null si es el último)
     */
    public Nodo<T> getNext() {
        return next;
    }

    /**
     * Establece o modifica la referencia al siguiente nodo.
     * 
     * @param next Nuevo nodo siguiente
     */
    public void setNext(Nodo<T> next) {
        this.next = next;
    }
}

