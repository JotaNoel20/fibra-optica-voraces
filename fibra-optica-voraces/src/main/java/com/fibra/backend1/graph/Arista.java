package com.fibra.backend1.graph;

import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.services.CostCalculator;

public class Arista {

    private Nodo origen;
    private Nodo destino;
    private double distancia;
    private double costo;

    public Arista(Nodo origen, Nodo destino, double distancia) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        // Calcular el costo usando CostCalculator
        this.costo = calcularCosto();
    }

    public Nodo getOrigen() { 
        return origen; 
    }
    
    public Nodo getDestino() { 
        return destino; 
    }
    
    public double getDistancia() { 
        return distancia; 
    }

    /**
     * Obtiene el costo actual de la arista.
     * Si el origen o destino están INACTIVOS, el costo es infinito.
     * Si son SUGERIDOS, también es infinito (no se pueden conectar aún).
     */
    public double getCosto() {
        // Nodos INACTIVOS no pueden usarse
        if (origen.getEstado() == EstadoNodo.INACTIVO || 
            destino.getEstado() == EstadoNodo.INACTIVO) {
            return Double.MAX_VALUE;
        }
        
        // Nodos SUGERIDOS no pueden conectarse aún
        if (origen.getTipo() == TipoNodo.SUGERIDO || 
            destino.getTipo() == TipoNodo.SUGERIDO) {
            return Double.MAX_VALUE;
        }
        
        // Cliente que ya tiene conexión no puede recibir más
        if (destino.getTipo() == TipoNodo.CLIENTE && destino.getClientesActuales() >= 1) {
            return Double.MAX_VALUE;
        }
        
        // Cliente como origen tampoco puede (solo es destino)
        if (origen.getTipo() == TipoNodo.CLIENTE) {
            return Double.MAX_VALUE;
        }
        
        return costo;
    }

    /**
     * Calcula el costo utilizando CostCalculator con la fórmula de penalización.
     */
    public double calcularCosto() {
        CostCalculator calculator = new CostCalculator();
        this.costo = calculator.calcularCosto(origen, destino, distancia);
        return this.costo;
    }
    
    /**
     * Recalcula el costo (útil cuando cambia el estado de los nodos)
     */
    public void recalcularCosto() {
        calcularCosto();
    }
    
    /**
     * Obtiene el costo simple sin penalización (solo distancia)
     */
    public double getCostoSimple() {
        return distancia;
    }
    
    /**
     * Verifica si la arista es válida (ambos nodos pueden conectarse)
     */
    public boolean esValida() {
        return getCosto() != Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "Arista{" +
                "origen=" + (origen != null ? origen.getId() : "null") +
                ", destino=" + (destino != null ? destino.getId() : "null") +
                ", distancia=" + distancia +
                ", costo=" + (costo == Double.MAX_VALUE ? "INFINITO" : costo) +
                '}';
    }
}