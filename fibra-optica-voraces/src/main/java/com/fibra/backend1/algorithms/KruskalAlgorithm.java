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
     * PRIMERO: Conecta todos los postes.
     * LUEGO: Conecta cada cliente al poste más cercano (por distancia real).
     * 
     * @param grafo Grafo con todos los nodos y aristas potenciales
     * @return ResultadoRuta con las conexiones seleccionadas
     */
    public ResultadoRuta generarRed(Grafo grafo) {
        List<Arista> resultado = new ArrayList<>();
        Set<Integer> clientesConectados = new HashSet<>();
        
        if (grafo == null || grafo.getNodos().isEmpty()) {
            throw new IllegalArgumentException("El grafo no puede ser nulo o vacío");
        }
        
        List<Nodo> nodosActivos = new ArrayList<>();
        List<Nodo> postes = new ArrayList<>();
        List<Nodo> clientes = new ArrayList<>();
        
        for (Nodo nodo : grafo.getNodos()) {
            if (nodo.getEstado() != EstadoNodo.INACTIVO && 
                nodo.getTipo() != TipoNodo.SUGERIDO) {
                nodosActivos.add(nodo);
                if (esPoste(nodo)) {
                    postes.add(nodo);
                } else if (nodo.getTipo() == TipoNodo.CLIENTE) {
                    clientes.add(nodo);
                }
            }
        }
        
        if (nodosActivos.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 nodos activos para generar la red");
        }
        
        // ========== FASE 1: Conectar SOLO aristas que NO involucran clientes ==========
        List<Arista> aristasPostes = new ArrayList<>();
        
        for (Arista arista : grafo.getAristas()) {
            if (arista.getCosto() == Double.MAX_VALUE || 
                !distanceValidator.esValida(arista.getOrigen(), arista.getDestino())) {
                continue;
            }
            
            boolean tieneCliente = (arista.getOrigen().getTipo() == TipoNodo.CLIENTE ||
                                    arista.getDestino().getTipo() == TipoNodo.CLIENTE);
            
            if (!tieneCliente) {
                aristasPostes.add(arista);
            }
        }
        
        aristasPostes.sort(Comparator.comparingDouble(Arista::getCosto));

        Map<Nodo, Nodo> padre = new HashMap<>();
        for (Nodo n : nodosActivos) {
            padre.put(n, n);
        }

        double costoTotal = 0;
        double distanciaTotal = 0;
        int cantidadPostesUsados = 0;
        int aristasAgregadas = 0;
        int aristasNecesariasPostes = postes.size();  // Conectar todos los postes

        if (DEBUG) System.out.println("=== FASE 1: Conectando postes ===");
        
        // Conectar todos los postes entre sí
        for (Arista arista : aristasPostes) {
            if (aristasAgregadas >= aristasNecesariasPostes - 1) {
                break;
            }
            
            // Solo conectar si ambos son postes
            if (!esPoste(arista.getOrigen()) || !esPoste(arista.getDestino())) {
                continue;
            }
            
            Nodo raizOrigen = encontrarRaiz(arista.getOrigen(), padre);
            Nodo raizDestino = encontrarRaiz(arista.getDestino(), padre);

            if (!raizOrigen.equals(raizDestino)) {
                resultado.add(arista);
                costoTotal += arista.getCosto();
                distanciaTotal += arista.getDistancia();
                aristasAgregadas++;
                
                if (!yaContadoPoste(arista.getOrigen(), resultado)) {
                    cantidadPostesUsados++;
                }
                if (!yaContadoPoste(arista.getDestino(), resultado)) {
                    cantidadPostesUsados++;
                }

                padre.put(raizOrigen, raizDestino);
            }
        }
        
        // ========== FASE 2: Conectar clientes al poste más cercano ==========
        if (DEBUG) System.out.println("=== FASE 2: Conectando clientes al poste más cercano ===");
        
        // Obtener todos los postes ya conectados
        Set<Nodo> postesConectados = new HashSet<>();
        for (Arista arista : resultado) {
            if (esPoste(arista.getOrigen())) {
                postesConectados.add(arista.getOrigen());
            }
            if (esPoste(arista.getDestino())) {
                postesConectados.add(arista.getDestino());
            }
        }
        
        // También incluir la central si no está ya
        for (Nodo nodo : nodosActivos) {
            if (nodo.getTipo() == TipoNodo.CENTRAL) {
                postesConectados.add(nodo);
            }
        }
        
        // Para cada cliente, encontrar el poste más cercano
        for (Nodo cliente : clientes) {
            if (clientesConectados.contains(cliente.getId())) {
                continue;
            }
            
            Nodo posteMasCercano = null;
            double distanciaMinima = Double.MAX_VALUE;
            
            for (Nodo poste : postesConectados) {
                double distancia = cliente.distanciaA(poste);
                
                // Verificar si la conexión es válida (distancia máxima 1000m)
                if (distancia <= 1000.0 && distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    posteMasCercano = poste;
                }
            }
            
            // También considerar postes no conectados pero cercanos
            for (Nodo poste : postes) {
                if (postesConectados.contains(poste)) {
                    continue;
                }
                double distancia = cliente.distanciaA(poste);
                if (distancia <= 1000.0 && distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    posteMasCercano = poste;
                    // No marcamos el poste como conectado todavía
                }
            }
            
            if (posteMasCercano != null) {
                // Crear arista directa al poste más cercano
                Arista arista = new Arista(posteMasCercano, cliente, distanciaMinima);
                resultado.add(arista);
                costoTotal += arista.getCosto();
                distanciaTotal += distanciaMinima;
                clientesConectados.add(cliente.getId());
                
                if (DEBUG) {
                    System.out.println("  Cliente " + cliente.getId() + " conectado a poste " + 
                                       posteMasCercano.getId() + " (dist: " + distanciaMinima + "m)");
                }
            }
        }

        ResultadoRuta resultadoRuta = new ResultadoRuta(resultado, costoTotal, distanciaTotal, cantidadPostesUsados);
        resultadoRuta.setIdsClientesConectados(clientesConectados);
        
        return resultadoRuta;
    }

    private Nodo encontrarRaiz(Nodo nodo, Map<Nodo, Nodo> padre) {
        Nodo actual = nodo;
        List<Nodo> camino = new ArrayList<>();
        
        while (!padre.get(actual).equals(actual)) {
            camino.add(actual);
            actual = padre.get(actual);
        }
        
        Nodo raiz = actual;
        
        for (Nodo n : camino) {
            padre.put(n, raiz);
        }
        
        return raiz;
    }

    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
    
    private boolean yaContadoPoste(Nodo poste, List<Arista> resultado) {
        for (Arista arista : resultado) {
            if (arista.getOrigen().equals(poste) || arista.getDestino().equals(poste)) {
                return true;
            }
        }
        return false;
    }
    
    // Flag para depuración
    private static final boolean DEBUG = false;
}