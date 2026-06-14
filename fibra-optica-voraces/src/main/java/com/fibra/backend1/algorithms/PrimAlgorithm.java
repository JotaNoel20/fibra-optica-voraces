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
     * 
     * @param grafo Grafo con todos los nodos y aristas potenciales
     * @param central Nodo central (punto de partida obligatorio)
     * @return ResultadoRuta con las conexiones seleccionadas
     */
    public ResultadoRuta generarRed(Grafo grafo, Nodo central) {
        List<Arista> resultado = new ArrayList<>();
        Set<Nodo> visitados = new HashSet<>();
        
        // Validación inicial
        if (central == null) {
            throw new IllegalArgumentException("El nodo central no puede ser nulo");
        }
        
        if (central.getEstado() == EstadoNodo.INACTIVO) {
            throw new IllegalStateException("La central está INACTIVA y no puede generar red");
        }
        
        if (central.getTipo() != TipoNodo.CENTRAL) {
            throw new IllegalArgumentException("El nodo proporcionado no es de tipo CENTRAL");
        }

        visitados.add(central);

        // Cola de prioridad para seleccionar siempre la arista más barata disponible
        PriorityQueue<Arista> colaAristas = new PriorityQueue<>(
                Comparator.comparingDouble(Arista::getCosto)
        );

        // Cargar las aristas iniciales de la central (solo conexiones válidas)
        agregarAristasValidasDeNodo(central, grafo, visitados, colaAristas);

        double costoTotal = 0;
        double distanciaTotal = 0;
        int cantidadPostesUsados = 0;

        while (!colaAristas.isEmpty() && visitados.size() < grafo.getNodos().size()) {
            Arista mejorArista = colaAristas.poll();

            // Verificar que la arista siga siendo válida (costo no infinito)
            if (mejorArista.getCosto() == Double.MAX_VALUE) {
                continue;
            }

            // Verificar cuál extremo no ha sido visitado aún
            Nodo nuevoNodo = null;
            if (visitados.contains(mejorArista.getOrigen()) && !visitados.contains(mejorArista.getDestino())) {
                nuevoNodo = mejorArista.getDestino();
            } else if (visitados.contains(mejorArista.getDestino()) && !visitados.contains(mejorArista.getOrigen())) {
                nuevoNodo = mejorArista.getOrigen();
            }

            // Si ambos ya fueron visitados o el nuevo nodo no es válido, descartar
            if (nuevoNodo == null) {
                continue;
            }
            
            // Verificar que el nuevo nodo pueda recibir conexión
            if (nuevoNodo.getEstado() == EstadoNodo.INACTIVO) {
                continue;
            }
            
            // Si es un cliente, verificar que no tenga ya una conexión
            if (nuevoNodo.getTipo() == TipoNodo.CLIENTE && nuevoNodo.getClientesActuales() >= 1) {
                continue;
            }

            // Validar la conexión específica según reglas de distancia
            if (!distanceValidator.esValida(mejorArista.getOrigen(), mejorArista.getDestino())) {
                continue;
            }

            // Confirmar y conectar el nuevo tramo
            resultado.add(mejorArista);
            costoTotal += mejorArista.getCosto();
            distanciaTotal += mejorArista.getDistancia();
            
            // Contar postes (excluyendo central y clientes)
            if (esPoste(nuevoNodo)) {
                cantidadPostesUsados++;
            }
            
            visitados.add(nuevoNodo);

            // Inyectar las nuevas aristas del nodo descubierto
            agregarAristasValidasDeNodo(nuevoNodo, grafo, visitados, colaAristas);
        }

        // Verificar si se conectaron todos los nodos
        boolean todosConectados = visitados.size() == grafo.getNodos().size();
        if (!todosConectados) {
            System.out.println("Advertencia: No se pudieron conectar todos los nodos. " +
                    "Conectados: " + visitados.size() + " de " + grafo.getNodos().size());
        }

        return new ResultadoRuta(resultado, costoTotal, distanciaTotal, cantidadPostesUsados);
    }

    /**
     * Agrega a la cola solo las aristas válidas desde un nodo
     */
    private void agregarAristasValidasDeNodo(Nodo nodo, Grafo grafo, 
                                              Set<Nodo> visitados, 
                                              PriorityQueue<Arista> cola) {
        for (Arista arista : grafo.getAristas()) {
            // Evitar aristas con costo infinito
            if (arista.getCosto() == Double.MAX_VALUE) {
                continue;
            }
            
            // Verificar si la arista conecta con el nodo actual
            boolean conectaOrigen = arista.getOrigen().equals(nodo);
            boolean conectaDestino = arista.getDestino().equals(nodo);
            
            if (conectaOrigen || conectaDestino) {
                Nodo otroNodo = conectaOrigen ? arista.getDestino() : arista.getOrigen();
                
                // Solo agregar si el otro nodo no ha sido visitado
                if (!visitados.contains(otroNodo)) {
                    // Verificar que la conexión sea válida según reglas de distancia
                    if (distanceValidator.esValida(nodo, otroNodo)) {
                        cola.add(arista);
                    }
                }
            }
        }
    }

    /**
     * Verifica si un nodo es poste (PRINCIPAL o SECUNDARIO)
     */
    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
}