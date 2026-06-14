package com.fibra.backend1.services;

import com.fibra.backend1.graph.Nodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.enums.EstadoNodo;

public class DistanceValidator {

    /**
     * Verifica si la conexión entre dos nodos es válida según:
     * - Distancias máximas permitidas
     * - Compatibilidad entre tipos de nodo
     * - Nodos no estén INACTIVOS
     * 
     * @param origen Nodo origen
     * @param destino Nodo destino
     * @return true si la conexión es válida, false si no
     */
    public boolean esValida(Nodo origen, Nodo destino) {
        if (origen == null || destino == null) {
            return false;
        }
        
        // Nodos INACTIVOS no pueden participar en conexiones
        if (origen.getEstado() == EstadoNodo.INACTIVO || destino.getEstado() == EstadoNodo.INACTIVO) {
            return false;
        }
        
        double distancia = origen.distanciaA(destino);
        
        // ========== REGLA 1: CLIENTE solo puede conectarse a un POSTE ==========
        if (esCliente(origen) || esCliente(destino)) {
            boolean esClienteOrigen = esCliente(origen);
            boolean esClienteDestino = esCliente(destino);
            boolean esPosteOrigen = esPoste(origen);
            boolean esPosteDestino = esPoste(destino);
            
            if (esClienteOrigen && esClienteDestino) {
                return false;
            }
            
            if ((esClienteOrigen && esCentral(destino)) || (esClienteDestino && esCentral(origen))) {
                return false;
            }
            
            if ((esClienteOrigen && esPosteDestino) || (esClienteDestino && esPosteOrigen)) {
                return distancia <= 1000.0;
            }
            
            return false;
        }
        
        // ========== REGLA 2: CENTRAL solo puede conectarse a POSTES ==========
        if (esCentral(origen) || esCentral(destino)) {
            boolean esCentralOrigen = esCentral(origen);
            boolean esCentralDestino = esCentral(destino);
            boolean esPosteOrigen = esPoste(origen);
            boolean esPosteDestino = esPoste(destino);
            
            if (esCentralOrigen && esCentralDestino) {
                return false;
            }
            
            if ((esCentralOrigen && esCliente(destino)) || (esCentralDestino && esCliente(origen))) {
                return false;
            }
            
            if ((esCentralOrigen && esPosteDestino) || (esCentralDestino && esPosteOrigen)) {
                return distancia <= 800.0;  // CAMBIADO: 150.0 → 800.0
            }
            
            return false;
        }
        
        // ========== REGLA 3: POSTE ↔ POSTE ==========
        if (esPoste(origen) && esPoste(destino)) {
            return distancia <= 500.0;  // CAMBIADO: 80.0 → 500.0
        }
        
        // ========== REGLA 4: SUGERIDO solo para expansión ==========
        if (esSugerido(origen) || esSugerido(destino)) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Verifica si la conexión entre dos nodos es válida solo para efectos de
     * sugerencia de postes (expansión), no para conexión real.
     */
    public boolean esValidaParaSugerencia(Nodo origen, Nodo destino) {
        if (origen == null || destino == null) {
            return false;
        }
        
        double distancia = origen.distanciaA(destino);
        
        if ((esPoste(origen) && esCliente(destino)) || (esPoste(destino) && esCliente(origen))) {
            return distancia <= 1000.0;
        }
        
        if (esPoste(origen) && esPoste(destino)) {
            return distancia <= 500.0;  // CAMBIADO: 130.0 → 500.0
        }
        
        if ((esCentral(origen) && esPoste(destino)) || (esCentral(destino) && esPoste(origen))) {
            return distancia <= 800.0;  // CAMBIADO: 180.0 → 800.0
        }
        
        return false;
    }
    
    /**
     * Obtiene la distancia máxima permitida entre dos tipos de nodo
     */
    public double getDistanciaMaxima(TipoNodo tipoA, TipoNodo tipoB) {
        if ((tipoA == TipoNodo.CLIENTE && esTipoPoste(tipoB)) ||
            (tipoB == TipoNodo.CLIENTE && esTipoPoste(tipoA))) {
            return 1000.0;
        }
        
        if ((tipoA == TipoNodo.CENTRAL && esTipoPoste(tipoB)) ||
            (tipoB == TipoNodo.CENTRAL && esTipoPoste(tipoA))) {
            return 800.0;  // CAMBIADO: 150.0 → 800.0
        }
        
        if (esTipoPoste(tipoA) && esTipoPoste(tipoB)) {
            return 500.0;  // CAMBIADO: 80.0 → 500.0
        }
        
        return 0.0;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private boolean esCentral(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.CENTRAL;
    }
    
    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
    
    private boolean esCliente(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.CLIENTE;
    }
    
    private boolean esSugerido(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.SUGERIDO;
    }
    
    private boolean esTipoPoste(TipoNodo tipo) {
        return tipo == TipoNodo.POSTE_PRINCIPAL || tipo == TipoNodo.POSTE_SECUNDARIO;
    }
}