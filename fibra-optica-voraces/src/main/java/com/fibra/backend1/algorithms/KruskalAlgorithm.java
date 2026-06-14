package com.fibra.backend1.algorithms;

import java.util.*;
import com.fibra.backend1.graph.*;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.services.DistanceValidator;

public class KruskalAlgorithm {

    private final DistanceValidator distanceValidator;

    public KruskalAlgorithm() {
        this.distanceValidator = new DistanceValidator();
    }

    public KruskalAlgorithm(DistanceValidator distanceValidator) {
        this.distanceValidator = distanceValidator;
    }

    /**
     * Genera la red de expansión mínima usando el algoritmo de KRUSKAL.
     * Este algoritmo conecta TODOS los nodos sin importar la central.
     * La central es tratada como un nodo más.
     * 
     * @param grafo Grafo con todos los nodos y aristas potenciales
     * @return ResultadoRuta con las conexiones seleccionadas
     */
    public ResultadoRuta generarRed(Grafo grafo) {
        List<Arista> resultado = new ArrayList<>();
        
        // Validación inicial
        if (grafo == null || grafo.getNodos().isEmpty()) {
            throw new IllegalArgumentException("El grafo no puede ser nulo o vacío");
        }
        
        // Filtrar nodos activos (excluir INACTIVOS y SUGERIDOS)
        List<Nodo> nodosActivos = new ArrayList<>();
        for (Nodo nodo : grafo.getNodos()) {
            if (nodo.getEstado() != EstadoNodo.INACTIVO && 
                nodo.getTipo() != TipoNodo.SUGERIDO) {
                nodosActivos.add(nodo);
            }
        }
        
        if (nodosActivos.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 nodos activos para generar la red");
        }
        
        // Filtrar aristas válidas (costo no infinito)
        List<Arista> aristasValidas = new ArrayList<>();
        for (Arista arista : grafo.getAristas()) {
            // Verificar que la arista sea válida según DistanceValidator
            if (arista.getCosto() != Double.MAX_VALUE && 
                distanceValidator.esValida(arista.getOrigen(), arista.getDestino())) {
                aristasValidas.add(arista);
            }
        }
        
        // Ordenar las aristas por su costo de menor a mayor
        aristasValidas.sort(Comparator.comparingDouble(Arista::getCosto));

        // Estructura Union-Find para control de ciclos (Disjoint-Set)
        Map<Nodo, Nodo> padre = new HashMap<>();
        for (Nodo n : nodosActivos) {
            padre.put(n, n); // Cada nodo es su propio padre al inicio
        }

        double costoTotal = 0;
        double distanciaTotal = 0;
        int cantidadPostesUsados = 0;
        int aristasAgregadas = 0;
        int aristasNecesarias = nodosActivos.size() - 1;

        for (Arista arista : aristasValidas) {
            // Si ya tenemos todas las aristas necesarias, salir
            if (aristasAgregadas >= aristasNecesarias) {
                break;
            }
            
            Nodo raizOrigen = encontrarRaiz(arista.getOrigen(), padre);
            Nodo raizDestino = encontrarRaiz(arista.getDestino(), padre);

            // Si las raíces son distintas, no hay ciclo. Se puede cablear este tramo.
            if (!raizOrigen.equals(raizDestino)) {
                resultado.add(arista);
                costoTotal += arista.getCosto();
                distanciaTotal += arista.getDistancia();
                aristasAgregadas++;
                
                // Contar postes (solo la primera vez que se agrega un poste)
                if (esPoste(arista.getOrigen()) && !yaContadoPoste(arista.getOrigen(), resultado)) {
                    cantidadPostesUsados++;
                }
                if (esPoste(arista.getDestino()) && !yaContadoPoste(arista.getDestino(), resultado)) {
                    cantidadPostesUsados++;
                }

                // Unión de los dos subconjuntos
                padre.put(raizOrigen, raizDestino);
            }
        }

        // Verificar si se conectaron todos los nodos
        if (aristasAgregadas < aristasNecesarias) {
            System.out.println("Advertencia: No se pudieron conectar todos los nodos. " +
                    "Conexiones: " + aristasAgregadas + " de " + aristasNecesarias);
        }

        return new ResultadoRuta(resultado, costoTotal, distanciaTotal, cantidadPostesUsados);
    }

    /**
     * Función auxiliar de Union-Find con compresión de caminos (versión iterativa)
     */
    private Nodo encontrarRaiz(Nodo nodo, Map<Nodo, Nodo> padre) {
        Nodo actual = nodo;
        List<Nodo> camino = new ArrayList<>();
        
        // Encontrar la raíz
        while (!padre.get(actual).equals(actual)) {
            camino.add(actual);
            actual = padre.get(actual);
        }
        
        Nodo raiz = actual;
        
        // Compresión de camino: todos los nodos del camino apuntan directamente a la raíz
        for (Nodo n : camino) {
            padre.put(n, raiz);
        }
        
        return raiz;
    }

    /**
     * Verifica si un nodo es poste (PRINCIPAL o SECUNDARIO)
     */
    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
    
    /**
     * Verifica si un poste ya ha sido contado en el resultado
     */
    private boolean yaContadoPoste(Nodo poste, List<Arista> resultado) {
        // Solo importa si ya aparece en alguna arista del resultado
        for (Arista arista : resultado) {
            if (arista.getOrigen().equals(poste) || arista.getDestino().equals(poste)) {
                return true;
            }
        }
        return false;
    }
}