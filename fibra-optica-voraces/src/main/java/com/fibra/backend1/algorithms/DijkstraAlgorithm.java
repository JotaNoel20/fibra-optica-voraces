package com.fibra.backend1.algorithms;

import java.util.*;
import com.fibra.backend1.graph.*;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.services.DistanceValidator;

public class DijkstraAlgorithm {

    private final DistanceValidator distanceValidator;

    public DijkstraAlgorithm() {
        this.distanceValidator = new DistanceValidator();
    }

    public DijkstraAlgorithm(DistanceValidator distanceValidator) {
        this.distanceValidator = distanceValidator;
    }

    /**
     * Calcula la ruta de costo mínimo entre origen y destino.
     * 
     * @param grafo Grafo con todos los nodos y aristas
     * @param origen Nodo de inicio
     * @param destino Nodo de destino
     * @return ResultadoRuta con la ruta óptima
     */
    public ResultadoRuta calcularRuta(Grafo grafo, Nodo origen, Nodo destino) {
        // Validaciones básicas
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser nulos.");
        }
        
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo.");
        }
        
        // Verificar que origen y destino sean válidos
        if (origen.getEstado() == EstadoNodo.INACTIVO) {
            throw new IllegalStateException("El nodo origen está INACTIVO");
        }
        
        if (destino.getEstado() == EstadoNodo.INACTIVO) {
            throw new IllegalStateException("El nodo destino está INACTIVO");
        }
        
        // Verificar que el destino pueda recibir conexión
        if (destino.getTipo() == TipoNodo.CLIENTE && destino.getClientesActuales() >= 1) {
            throw new IllegalStateException("El cliente destino ya tiene una conexión");
        }

        // Inicializar estructuras
        Map<Nodo, Double> distancias = new HashMap<>();
        Map<Nodo, Nodo> anteriores = new HashMap<>();
        Set<Nodo> visitados = new HashSet<>();

        // Inicializar distancias infinitas para todos los nodos
        for (Nodo nodo : grafo.getNodos()) {
            // Excluir nodos que no pueden participar en rutas
            if (nodo.getEstado() == EstadoNodo.INACTIVO || 
                nodo.getTipo() == TipoNodo.SUGERIDO) {
                continue;
            }
            distancias.put(nodo, Double.MAX_VALUE);
        }
        
        // Verificar que origen esté en el mapa
        if (!distancias.containsKey(origen)) {
            throw new IllegalStateException("El nodo origen no puede participar en la ruta");
        }
        
        distancias.put(origen, 0.0);

        // PriorityQueue optimizada por la distancia acumulada
        PriorityQueue<Nodo> cola = new PriorityQueue<>(
                Comparator.comparingDouble(distancias::get)
        );
        cola.add(origen);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();

            if (visitados.contains(actual)) {
                continue;
            }
            visitados.add(actual);

            // Llegamos al objetivo
            if (actual.equals(destino)) {
                break;
            }

            // Explorar vecinos del nodo actual
            for (Arista arista : grafo.getAristas()) {
                Nodo vecino = null;

                // Verificar si la arista conecta con el nodo actual
                if (arista.getOrigen().equals(actual)) {
                    vecino = arista.getDestino();
                } else if (arista.getDestino().equals(actual)) {
                    vecino = arista.getOrigen();
                }

                if (vecino == null || visitados.contains(vecino)) {
                    continue;
                }
                
                // Verificar que el vecino sea válido
                if (vecino.getEstado() == EstadoNodo.INACTIVO) {
                    continue;
                }
                
                if (vecino.getTipo() == TipoNodo.SUGERIDO) {
                    continue;
                }
                
                // Si el vecino es un cliente, verificar que no tenga ya conexión
                if (vecino.getTipo() == TipoNodo.CLIENTE && vecino.getClientesActuales() >= 1) {
                    continue;
                }

                // Validar que la conexión sea permitida por DistanceValidator
                if (!distanceValidator.esValida(actual, vecino)) {
                    continue;
                }

                // Obtener costo de la arista
                double costoArista = arista.getCosto();
                if (costoArista == Double.MAX_VALUE) {
                    continue;
                }

                double nuevaDistancia = distancias.get(actual) + costoArista;

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        // Verificar si se encontró una ruta
        if (!anteriores.containsKey(destino) && !origen.equals(destino)) {
            // No hay ruta disponible
            return new ResultadoRuta(new ArrayList<>(), -1, -1, 0);
        }

        // Reconstrucción de la ruta óptima
        List<Arista> ruta = new ArrayList<>();
        Nodo nodoActual = destino;

        while (anteriores.containsKey(nodoActual)) {
            Nodo previo = anteriores.get(nodoActual);
            Arista aristaConexa = null;

            for (Arista a : grafo.getAristas()) {
                if ((a.getOrigen().equals(previo) && a.getDestino().equals(nodoActual)) ||
                    (a.getOrigen().equals(nodoActual) && a.getDestino().equals(previo))) {
                    aristaConexa = a;
                    break;
                }
            }
            
            if (aristaConexa != null) {
                ruta.add(aristaConexa);
            }
            nodoActual = previo;
        }

        Collections.reverse(ruta);
        double costoTotal = distancias.get(destino);
        
        // Calcular distancia total sumando las distancias de las aristas
        double distanciaTotal = 0;
        for (Arista a : ruta) {
            distanciaTotal += a.getDistancia();
        }

        // Contar postes en la ruta
        Set<Nodo> postesUnicos = new HashSet<>();
        for (Arista a : ruta) {
            if (esPoste(a.getOrigen())) {
                postesUnicos.add(a.getOrigen());
            }
            if (esPoste(a.getDestino())) {
                postesUnicos.add(a.getDestino());
            }
        }

        return new ResultadoRuta(ruta, costoTotal, distanciaTotal, postesUnicos.size());
    }

    /**
     * Calcula la ruta más corta entre origen y destino usando solo la distancia
     * (sin penalización por saturación). Útil para comparaciones.
     */
    public ResultadoRuta calcularRutaSimple(Grafo grafo, Nodo origen, Nodo destino) {
        // Similar al método anterior pero usando getDistancia() en lugar de getCosto()
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser nulos.");
        }

        Map<Nodo, Double> distancias = new HashMap<>();
        Map<Nodo, Nodo> anteriores = new HashMap<>();
        Set<Nodo> visitados = new HashSet<>();

        for (Nodo nodo : grafo.getNodos()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        PriorityQueue<Nodo> cola = new PriorityQueue<>(
                Comparator.comparingDouble(distancias::get)
        );
        cola.add(origen);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            if (visitados.contains(actual)) continue;
            visitados.add(actual);
            if (actual.equals(destino)) break;

            for (Arista arista : grafo.getAristas()) {
                Nodo vecino = null;
                if (arista.getOrigen().equals(actual)) {
                    vecino = arista.getDestino();
                } else if (arista.getDestino().equals(actual)) {
                    vecino = arista.getOrigen();
                }
                if (vecino == null || visitados.contains(vecino)) continue;

                double nuevaDistancia = distancias.get(actual) + arista.getDistancia();
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        if (!anteriores.containsKey(destino) && !origen.equals(destino)) {
            return new ResultadoRuta(new ArrayList<>(), -1, -1, 0);
        }

        List<Arista> ruta = new ArrayList<>();
        Nodo nodoActual = destino;
        while (anteriores.containsKey(nodoActual)) {
            Nodo previo = anteriores.get(nodoActual);
            for (Arista a : grafo.getAristas()) {
                if ((a.getOrigen().equals(previo) && a.getDestino().equals(nodoActual)) ||
                    (a.getOrigen().equals(nodoActual) && a.getDestino().equals(previo))) {
                    ruta.add(a);
                    break;
                }
            }
            nodoActual = previo;
        }
        Collections.reverse(ruta);
        
        double distanciaTotal = distancias.get(destino);
        return new ResultadoRuta(ruta, distanciaTotal, distanciaTotal, ruta.size());
    }

    /**
     * Verifica si existe una ruta válida entre origen y destino
     */
    public boolean existeRuta(Grafo grafo, Nodo origen, Nodo destino) {
        try {
            ResultadoRuta resultado = calcularRuta(grafo, origen, destino);
            return resultado.getCostoTotal() >= 0 && !resultado.getConexiones().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
}