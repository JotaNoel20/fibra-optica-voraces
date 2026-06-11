package com.fibra.backend1.graph;

import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;

public class Nodo {

    private int id;
    private String nombre;
    private TipoNodo tipo;
    private EstadoNodo estado;
    private int capacidadMax;
    private int clientesActuales;
    private double latitud;
    private double longitud;

    public Nodo(int id, String nombre, TipoNodo tipo, int capacidadMax, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidadMax = capacidadMax;
        this.latitud = latitud;
        this.longitud = longitud;
        this.estado = EstadoNodo.DISPONIBLE;
        this.clientesActuales = 0;
    }

    public boolean puedeConectar() {
        return estado != EstadoNodo.SATURADO;
    }

    public boolean agregarCliente() {
        if (clientesActuales >= capacidadMax) {
            return false;
        }

        clientesActuales++;
        actualizarEstado();
        return true;
    }

    public void removerCliente() {
        if (clientesActuales > 0) {
            clientesActuales--;
        }

        actualizarEstado();
    }

    public int capacidadDisponible() {
        return capacidadMax - clientesActuales;
    }

    private void actualizarEstado() {
        if (clientesActuales >= capacidadMax) {
            estado = EstadoNodo.SATURADO;
        } else {
            estado = EstadoNodo.DISPONIBLE;
        }
    }

    public double distanciaA(Nodo destino) {
        double dx = this.latitud - destino.latitud;
        double dy = this.longitud - destino.longitud;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoNodo getTipo() {
        return tipo;
    }

    public EstadoNodo getEstado() {
        return estado;
    }

    public int getCapacidadMax() {
        return capacidadMax;
    }

    public int getClientesActuales() {
        return clientesActuales;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setEstado(EstadoNodo estado) {
        this.estado = estado;
    }
}
