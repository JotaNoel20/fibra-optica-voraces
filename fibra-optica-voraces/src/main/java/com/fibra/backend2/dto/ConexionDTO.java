package com.fibra.backend2.dto;

public class ConexionDTO {

    private Integer id;
    private Integer origenId;
    private Integer destinoId;
    private Double distancia;

    public ConexionDTO() {
    }

    public ConexionDTO(Integer id, Integer origenId, Integer destinoId, Double distancia) {
        this.id = id;
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.distancia = distancia;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Integer origenId) {
        this.origenId = origenId;
    }

    public Integer getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Integer destinoId) {
        this.destinoId = destinoId;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    @Override
    public String toString() {
        return "ConexionDTO{" +
                "id=" + id +
                ", origenId=" + origenId +
                ", destinoId=" + destinoId +
                ", distancia=" + distancia +
                '}';
    }
}