package com.fibra.backend1.algorithms;

import java.util.*;
import com.fibra.backend1.graph.*;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.services.DistanceValidator;

public class PrimAlgorithm {

    private final DistanceValidator distanceValidator;

    public PrimAlgorithm() {
        this.distanceValidator = new DistanceValidator();
    }

    public PrimAlgorithm(DistanceValidator distanceValidator) {
        this.distanceValidator = distanceValidator;
    }

    /**
     * Genera la red de expansión mínima usando el algoritmo de PRIM.
     * PRIMERO: Conecta todos los postes usando PRIM.
     * LUEGO: Conecta cada cliente al poste más cercano (por distancia real).
     * 
     * @param grafo Grafo con todos los nodos y aristas potenciales
     * @param central Nodo central (punto de partida obligatorio)
     * @return ResultadoRuta con las conexiones seleccionadas
     */
    public ResultadoRuta generarRed(Grafo grafo, Nodo central) {
        List<Arista> resultado = new ArrayList<>();
        Set<Nodo> visitados = new HashSet<>();
        Set<Integer> clientesConectados = new HashSet<>();
        
        if (central == null) {
            throw new IllegalArgumentException("El nodo central no puede ser nulo");
        }
        
        if (central.getEstado() == EstadoNodo.INACTIVO) {
            throw new IllegalStateException("La central está INACTIVA");
        }
        
        if (central.getTipo() != TipoNodo.CENTRAL) {
            throw new IllegalArgumentException("El nodo proporcionado no es de tipo CENTRAL");
        }

        // Separar postes y clientes
        List<Nodo> postes = new ArrayList<>();
        List<Nodo> clientes = new ArrayList<>();
        
        for (Nodo nodo : grafo.getNodos()) {
            if (nodo.getEstado() == EstadoNodo.INACTIVO || nodo.getTipo() == TipoNodo.SUGERIDO) {
                continue;
            }
            if (esPoste(nodo)) {
                postes.add(nodo);
            } else if (nodo.getTipo() == TipoNodo.CLIENTE) {
                clientes.add(nodo);
            }
        }

        // ========== FASE 1: Conectar postes usando PRIM ==========
        if (DEBUG) System.out.println("=== FASE 1: Conectando postes con PRIM ===");
        
        visitados.add(central);
        
        PriorityQueue<Arista> colaAristas = new PriorityQueue<>(
                Comparator.comparingDouble(Arista::getCosto)
        );
        
        // Agregar aristas desde la central solo hacia postes
        for (Arista arista : grafo.getAristas()) {
            if (arista.getCosto() == Double.MAX_VALUE) continue;
            
            Nodo otro = null;
            if (arista.getOrigen().equals(central) && esPoste(arista.getDestino())) {
                otro = arista.getDestino();
            } else if (arista.getDestino().equals(central) && esPoste(arista.getOrigen())) {
                otro = arista.getOrigen();
            }
            
            if (otro != null && distanceValidator.esValida(central, otro)) {
                colaAristas.add(arista);
            }
        }
        
        // Conectar postes entre sí
        while (!colaAristas.isEmpty() && visitados.size() <= postes.size() + 1) {
            Arista mejorArista = colaAristas.poll();
            
            if (mejorArista.getCosto() == Double.MAX_VALUE) continue;
            
            Nodo nuevoNodo = null;
            if (visitados.contains(mejorArista.getOrigen()) && !visitados.contains(mejorArista.getDestino())) {
                nuevoNodo = mejorArista.getDestino();
            } else if (visitados.contains(mejorArista.getDestino()) && !visitados.contains(mejorArista.getOrigen())) {
                nuevoNodo = mejorArista.getOrigen();
            }
            
            if (nuevoNodo == null) continue;
            if (nuevoNodo.getEstado() == EstadoNodo.INACTIVO) continue;
            if (nuevoNodo.getTipo() == TipoNodo.CLIENTE) continue;
            
            if (!distanceValidator.esValida(mejorArista.getOrigen(), mejorArista.getDestino())) continue;
            
            resultado.add(mejorArista);
            visitados.add(nuevoNodo);
            
            // Agregar nuevas aristas desde el nuevo poste
            for (Arista arista : grafo.getAristas()) {
                if (arista.getCosto() == Double.MAX_VALUE) continue;
                
                Nodo otro = null;
                if (arista.getOrigen().equals(nuevoNodo) && !visitados.contains(arista.getDestino()) && esPoste(arista.getDestino())) {
                    otro = arista.getDestino();
                } else if (arista.getDestino().equals(nuevoNodo) && !visitados.contains(arista.getOrigen()) && esPoste(arista.getOrigen())) {
                    otro = arista.getOrigen();
                }
                
                if (otro != null && distanceValidator.esValida(nuevoNodo, otro)) {
                    colaAristas.add(arista);
                }
            }
        }
        
        // ========== FASE 2: Conectar clientes al poste más cercano ==========
        if (DEBUG) System.out.println("=== FASE 2: Conectando clientes al poste más cercano ===");
        
        // Obtener todos los nodos conectados (central + postes)
        Set<Nodo> nodosConectados = new HashSet<>(visitados);
        
        // Para cada cliente, encontrar el nodo conectado más cercano
        for (Nodo cliente : clientes) {
            if (clientesConectados.contains(cliente.getId())) continue;
            
            Nodo nodoMasCercano = null;
            double distanciaMinima = Double.MAX_VALUE;
            
            for (Nodo nodo : nodosConectados) {
                double distancia = cliente.distanciaA(nodo);
                if (distancia <= 1000.0 && distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    nodoMasCercano = nodo;
                }
            }
            
            if (nodoMasCercano != null) {
                Arista arista = new Arista(nodoMasCercano, cliente, distanciaMinima);
                resultado.add(arista);
                clientesConectados.add(cliente.getId());
                
                if (DEBUG) {
                    System.out.println("  Cliente " + cliente.getId() + " conectado a " + 
                                       nodoMasCercano.getTipo() + " " + nodoMasCercano.getId() + 
                                       " (dist: " + distanciaMinima + "m)");
                }
            }
        }

        // Calcular estadísticas finales
        double costoTotal = 0;
        double distanciaTotal = 0;
        Set<Nodo> postesUsados = new HashSet<>();
        
        for (Arista arista : resultado) {
            costoTotal += arista.getCosto();
            distanciaTotal += arista.getDistancia();
            if (esPoste(arista.getOrigen())) postesUsados.add(arista.getOrigen());
            if (esPoste(arista.getDestino())) postesUsados.add(arista.getDestino());
        }

        ResultadoRuta resultadoRuta = new ResultadoRuta(resultado, costoTotal, distanciaTotal, postesUsados.size());
        resultadoRuta.setIdsClientesConectados(clientesConectados);
        
        return resultadoRuta;
    }

    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
    
    // Flag para depuración
    private static final boolean DEBUG = false;
}