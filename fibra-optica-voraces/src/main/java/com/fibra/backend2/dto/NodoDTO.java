package com.fibra.backend2.dto;

public class NodoDTO {

    private Integer id;
    private String nombre;
    private String tipo;
    private String estado;
    private Integer capacidadMax;
    private Integer clientesActuales;
    private double latitud;
    private double longitud;

    public NodoDTO() {
    }

    public NodoDTO(Integer id, String nombre, String tipo, String estado, Integer capacidadMax,
                   Integer clientesActuales, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.capacidadMax = capacidadMax;
        this.clientesActuales = clientesActuales;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCapacidadMax() {
        return capacidadMax;
    }

    public void setCapacidadMax(Integer capacidadMax) {
        this.capacidadMax = capacidadMax;
    }

    public Integer getClientesActuales() {
        return clientesActuales;
    }

    public void setClientesActuales(Integer clientesActuales) {
        this.clientesActuales = clientesActuales;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    @Override
    public String toString() {
        return "NodoDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", capacidadMax=" + capacidadMax +
                ", clientesActuales=" + clientesActuales +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                '}';
    }
}
