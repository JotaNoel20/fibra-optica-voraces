package com.fibra.backend2.services;

import com.fibra.backend2.dto.NodoDTO;

public class SpatialValidationService {

    public boolean validarPoste(NodoDTO nodo) {
        return nodo != null;
    }

    public double distanciaACalle(NodoDTO nodo) {
        return 0.0;
    }
}
