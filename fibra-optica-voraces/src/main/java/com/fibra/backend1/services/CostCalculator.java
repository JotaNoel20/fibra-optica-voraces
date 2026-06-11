package com.fibra.backend1.services;

import com.fibra.backend1.graph.Nodo;

public class CostCalculator {

    public double calcularCosto(Nodo origen, Nodo destino, double distancia) {
        double ocupacion = (double) origen.getClientesActuales() / origen.getCapacidadMax();
        double penalizacion = ocupacion * 100;
        return distancia + penalizacion;
    }
}
