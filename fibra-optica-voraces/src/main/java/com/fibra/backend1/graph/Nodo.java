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

    public int getCapacidadMaximaPorTipo() {
        switch (this.tipo) {
            case CENTRAL:
                return 100;
            case POSTE_PRINCIPAL:
            case POSTE_SECUNDARIO:
                return this.capacidadMax;
            case CLIENTE:
                return 1;
            case SUGERIDO:
                return 0;
            default:
                return 1;
        }
    }

    public boolean puedeConectar() {
        // Cliente no puede conectarse si ya tiene conexión (como origen o destino)
        if (this.tipo == TipoNodo.CLIENTE) {
            return this.clientesActuales == 0;
        }
        
        if (this.tipo == TipoNodo.SUGERIDO) {
            return false;
        }
        
        return this.estado != EstadoNodo.SATURADO && this.estado != EstadoNodo.INACTIVO;
    }

    public boolean agregarCliente() {
        int maxPermitido = getCapacidadMaximaPorTipo();
        
        if (!puedeConectar() || clientesActuales >= maxPermitido) {
            return false;
        }
        
        // Cliente solo puede tener 1 conexión (como destino)
        if (this.tipo == TipoNodo.CLIENTE && clientesActuales >= 1) {
            return false;
        }
        
        // Central no puede tener clientes directos
        if (this.tipo == TipoNodo.CENTRAL && clientesActuales >= maxPermitido) {
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
        int maxPermitido = getCapacidadMaximaPorTipo();
        return maxPermitido - clientesActuales;
    }

    private void actualizarEstado() {
        if (this.estado == EstadoNodo.INACTIVO) {
            return;
        }
        
        int maxPermitido = getCapacidadMaximaPorTipo();
        
        if (clientesActuales >= maxPermitido && maxPermitido > 0) {
            this.estado = EstadoNodo.SATURADO;
        } else {
            this.estado = EstadoNodo.DISPONIBLE;
        }
    }

    public double distanciaA(Nodo destino) {
        final double RADIO_TIERRA_METROS = 6371000.0;

        double lat1Rad = Math.toRadians(this.latitud);
        double lat2Rad = Math.toRadians(destino.getLatitud());
        double deltaLat = Math.toRadians(destino.getLatitud() - this.latitud);
        double deltaLon = Math.toRadians(destino.getLongitud() - this.longitud);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_METROS * c;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoNodo getTipo() { return tipo; }
    public void setTipo(TipoNodo tipo) { this.tipo = tipo; }
    public EstadoNodo getEstado() { return estado; }
    public void setEstado(EstadoNodo estado) { 
        this.estado = estado;
        actualizarEstado();
    }
    public int getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(int capacidadMax) { this.capacidadMax = capacidadMax; }
    public int getClientesActuales() { return clientesActuales; }
    public void setClientesActuales(int clientesActuales) { 
        this.clientesActuales = clientesActuales;
        actualizarEstado();
    }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}