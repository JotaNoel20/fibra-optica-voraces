package com.fibra.backend2.dto;

public class CalleDTO {

    private Integer id;
    private String nombre;
    private String tipoVia;
    private Double longitud;

    public CalleDTO() {
    }

    public CalleDTO(Integer id, String nombre, String tipoVia, Double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipoVia = tipoVia;
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

    public String getTipoVia() {
        return tipoVia;
    }

    public void setTipoVia(String tipoVia) {
        this.tipoVia = tipoVia;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    @Override
    public String toString() {
        return "CalleDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipoVia='" + tipoVia + '\'' +
                ", longitud=" + longitud +
                '}';
    }
}
