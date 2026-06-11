package com.fibra.backend2.services;

import com.fibra.backend2.repositories.CalleRepository;

public class SpatialValidationService {

    // Distancia maxima permitida entre un poste y una calle.
    private static final double TOLERANCIA_CALLE_METROS = 50.0;

    private final CalleRepository calleRepository;

    public SpatialValidationService(CalleRepository calleRepository) {
        this.calleRepository = calleRepository;
    }

    public boolean validarPoste(double latitud, double longitud) {
        return calleRepository.existeCalleCercana(latitud, longitud, TOLERANCIA_CALLE_METROS);
    }

    public double distanciaACalle(double latitud, double longitud) {
        return calleRepository.distanciaMinimaACalle(latitud, longitud);
    }
}
