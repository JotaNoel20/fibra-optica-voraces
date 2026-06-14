package com.fibra.backend1.services;

import com.fibra.backend1.graph.Nodo;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;

public class CostCalculator {

    private boolean modoSoloDistancia = false;  // NUEVO: Flag para modo solo distancia

    /**
     * Constructor por defecto
     */
    public CostCalculator() {
        this.modoSoloDistancia = false;
    }

    /**
     * Constructor con opción de modo solo distancia
     * @param modoSoloDistancia true = solo distancia, false = con penalización
     */
    public CostCalculator(boolean modoSoloDistancia) {
        this.modoSoloDistancia = modoSoloDistancia;
    }

    /**
     * Activa o desactiva el modo solo distancia
     * @param modoSoloDistancia true = solo distancia, false = con penalización
     */
    public void setModoSoloDistancia(boolean modoSoloDistancia) {
        this.modoSoloDistancia = modoSoloDistancia;
    }

    /**
     * Calcula el costo de una conexión entre origen y destino.
     * 
     * Fórmula modo normal: costo = distancia + penalización
     * Fórmula modo solo distancia: costo = distancia
     * 
     * @param origen Nodo origen (generalmente un poste o central)
     * @param destino Nodo destino
     * @param distancia Distancia en metros entre los nodos
     * @return Costo total de la conexión (Double.MAX_VALUE si es inválida)
     */
    public double calcularCosto(Nodo origen, Nodo destino, double distancia) {
        // 1. Protección: Nodos INACTIVOS no pueden usarse
        if (origen.getEstado() == EstadoNodo.INACTIVO || 
            destino.getEstado() == EstadoNodo.INACTIVO) {
            return Double.MAX_VALUE;
        }
        
        // 2. Protección: Nodos SUGERIDOS no pueden conectarse aún
        if (origen.getTipo() == TipoNodo.SUGERIDO || 
            destino.getTipo() == TipoNodo.SUGERIDO) {
            return Double.MAX_VALUE;
        }
        
        // 3. Protección: CLIENTE solo puede tener 1 conexión
        if (destino.getTipo() == TipoNodo.CLIENTE && destino.getClientesActuales() >= 1) {
            return Double.MAX_VALUE;
        }
        
        // 4. Clientes no tienen penalización (son terminales)
        if (origen.getTipo() == TipoNodo.CLIENTE) {
            return distancia;
        }
        
        // 5. CENTRAL no tiene penalización (es el punto de partida)
        if (origen.getTipo() == TipoNodo.CENTRAL) {
            return distancia;
        }
        
        // 6. Si está activado el modo solo distancia, retornar solo distancia
        if (modoSoloDistancia) {
            return distancia;
        }
        
        // 7. Calcular penalización para POSTES (solo en modo normal)
        double penalizacion = calcularPenalizacion(origen);
        
        // 8. Si el destino es un poste, también considerar su ocupación
        if (esPoste(destino)) {
            penalizacion += calcularPenalizacion(destino) / 2; // Mitad de peso
        }
        
        return distancia + penalizacion;
    }
    
    /**
     * Calcula la penalización por ocupación de un poste.
     * 
     * @param poste Nodo que debe ser un poste (PRINCIPAL o SECUNDARIO)
     * @return Penalización en metros (mayor = peor señal)
     */
    private double calcularPenalizacion(Nodo poste) {
        // Si no es poste, no hay penalización
        if (!esPoste(poste)) {
            return 0.0;
        }
        
        // Evitar división por cero
        if (poste.getCapacidadMax() <= 0) {
            return 0.0;
        }
        
        // Calcular porcentaje de ocupación (0.0 a 1.0)
        double ocupacion = (double) poste.getClientesActuales() / poste.getCapacidadMax();
        
        // Penalización lineal: a mayor ocupación, mayor costo
        double penalizacion = ocupacion * 100.0;
        
        return penalizacion;
    }
    
    /**
     * Calcula el costo de una conexión sin considerar penalización por saturación.
     * Útil para comparación o visualización.
     */
    public double calcularCostoSimple(double distancia) {
        return distancia;
    }
    
    /**
     * Obtiene el factor de calidad de un poste (menor = mejor señal)
     * @return Valor entre 0 y 100, donde 0 es mejor señal
     */
    public double obtenerFactorCalidad(Nodo poste) {
        if (!esPoste(poste) || poste.getCapacidadMax() <= 0) {
            return 0.0;
        }
        
        double ocupacion = (double) poste.getClientesActuales() / poste.getCapacidadMax();
        return ocupacion * 100.0;
    }
    
    /**
     * Verifica si un nodo es poste (PRINCIPAL o SECUNDARIO)
     */
    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
}