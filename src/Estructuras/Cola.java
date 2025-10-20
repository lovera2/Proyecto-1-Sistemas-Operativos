/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Estructuras;

/**
 * Implementación de una estructura de datos Cola (FIFO).
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
    
    /**
     * Comprueba si la cola se encuentra vacia
     * 
     * @return true si la cola no contiene elementos, false en caso contrario
     */
    public boolean esVacia(){
        return front == null;
    }
    
    /**
     * Elimina todos los elementos de una cola, dejandola vacia.
     * 
     */
    public void vaciar(){
        front=null;
        back=null;
        tamaño=0;
    }
    
    /**
     * Añade un elemento al final de la cola.
     * 
     * @param dato Elemento que se quiere encolar.
     */
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
    
    /**
     * Elimina el elemento al frente de la cola.
     *      
     * @throws IllegalStateException si se intenta desencolar de una cola vacía
     */
    public void desencolar() {
        if(esVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        front = front.getNext();
        if (front == null) {
            back = null;
        }
        tamaño--;
    }
    
    /**
     * Elimina y devuelve el elemento del frente de la cola.
     * 
     * @return Elemento removido del frente, o null si la cola está vacía
     */
    public T desencolarDato() {
        if (this.esVacia()) {
            return null;
        }
       
        T dato = front.getDato();
        front = front.getNext();
        tamaño--; 
        if(this.esVacia()){
            back = null; 
        }
        return dato;
    }
    
    /**
     * Genera una representación en cadena de todos los elementos de la cola.
     * Los elementos se muestran en orden desde el frente hasta el final,
     * cada uno en una línea separada.
     * 
     * @return Cadena con los elementos de la cola separados por saltos de línea
     */
    public String mostrarCola(){
        String cadena="";
        Nodo aux = front;
        
        while (aux!=null){
            cadena=cadena+aux.getDato()+"\n";
            aux=aux.getNext();
        }
        return cadena;
    } 
    
    // NUEVO: tamaño actual (útil para recorrer)
    public int verTamano(){ 
        return tamaño;
    }

    // NUEVO: ver el primero sin sacarlo (peek)
    public T verFrente() {
        if (esVacia()){
        return null;
        } else {
            return front.getDato();
        }
    }



    //obtener elemento por índice (para evaluar candidatos)
    public T getAt(int i) {
        if (i < 0 || i >= tamaño) return null;
        Nodo<T> p = front;
        for (int k = 0; k < i; k++) p = p.getNext();
        return p.getDato();
    }

    //quitar y devolver el elemento en índice i
    public T removeAt(int i) {
        if (i < 0 || i >= tamaño) return null;
        if (i == 0) return desencolarDato();           // reusa método
        Nodo<T> prev = front;
        for (int k = 0; k < i - 1; k++) prev = prev.getNext();
        Nodo<T> del = prev.getNext();
        T dato = del.getDato();
        prev.setNext(del.getNext());
        if (del == back) back = prev;
        tamaño--;
        return dato;

    /**
     * Getters y Setters
     */
    
    public Nodo getFront() {
        return front;
    }

    public void setFront(Nodo front) {
        this.front = front;
    }

    public Nodo getBack() {
        return back;
    }

    public void setBack(Nodo back) {
        this.back = back;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void setTamaño(int tamaño) {
        this.tamaño = tamaño;
    }

}

   
