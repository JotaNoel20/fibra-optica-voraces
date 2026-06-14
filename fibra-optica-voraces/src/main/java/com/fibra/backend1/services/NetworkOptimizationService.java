package com.fibra.backend1.services;

import com.fibra.backend1.graph.Grafo;
import com.fibra.backend1.graph.Nodo;
import com.fibra.backend1.graph.Arista;
import com.fibra.backend1.graph.ResultadoRuta;
import com.fibra.backend1.algorithms.PrimAlgorithm;
import com.fibra.backend1.algorithms.KruskalAlgorithm;
import com.fibra.backend1.algorithms.DijkstraAlgorithm;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend2.repositories.CalleRepository;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkOptimizationService {

    // Flag para activar/desactivar logs (desactivar en producción para mejorar rendimiento)
    private static final boolean DEBUG = false;
    
    private final PrimAlgorithm prim;
    private final KruskalAlgorithm kruskal;
    private final DijkstraAlgorithm dijkstra;
    private final DistanceValidator distanceValidator;
    private final CostCalculator costCalculator;
    private final ExpansionService expansionService;

    public NetworkOptimizationService() {
        this.prim = new PrimAlgorithm();
        this.kruskal = new KruskalAlgorithm();
        this.dijkstra = new DijkstraAlgorithm();
        this.distanceValidator = new DistanceValidator();
        this.costCalculator = new CostCalculator();
        this.expansionService = new ExpansionService();
    }
    
    public NetworkOptimizationService(CalleRepository calleRepository) {
        this.prim = new PrimAlgorithm();
        this.kruskal = new KruskalAlgorithm();
        this.dijkstra = new DijkstraAlgorithm();
        this.distanceValidator = new DistanceValidator();
        this.costCalculator = new CostCalculator();
        this.expansionService = new ExpansionService(calleRepository);
    }

    public NetworkOptimizationService(PrimAlgorithm prim, KruskalAlgorithm kruskal,
                                      DijkstraAlgorithm dijkstra, DistanceValidator distanceValidator,
                                      CostCalculator costCalculator, ExpansionService expansionService) {
        this.prim = prim;
        this.kruskal = kruskal;
        this.dijkstra = dijkstra;
        this.distanceValidator = distanceValidator;
        this.costCalculator = costCalculator;
        this.expansionService = expansionService;
    }

    /**
     * Genera la red usando el algoritmo especificado (modo normal)
     */
    public ResultadoRuta generarRed(Grafo grafo, Nodo central, String algoritmo) {
        return generarRed(grafo, central, algoritmo, false);
    }

    /**
     * Genera la red usando el algoritmo especificado
     */
    public ResultadoRuta generarRed(Grafo grafo, Nodo central, String algoritmo, boolean conectarTodosPostes) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo");
        }

        if (algoritmo == null || algoritmo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar un algoritmo (PRIM o KRUSKAL)");
        }

        // Configurar el modo del CostCalculator
        costCalculator.setModoSoloDistancia(conectarTodosPostes);

        if (DEBUG) {
            System.out.println("\n=== GENERANDO RED CON " + algoritmo + " ===");
            System.out.println("  Modo conectarTodosPostes: " + conectarTodosPostes);
            System.out.println("  Nodos totales en grafo: " + grafo.getNodos().size());
        }

        boolean existeCentral = grafo.getNodos().stream()
                .anyMatch(n -> n.getTipo() == TipoNodo.CENTRAL);
        
        if (!existeCentral) {
            throw new IllegalStateException("No existe una central en la red. Debe agregar una central primero.");
        }

        List<Nodo> nodosActivos = grafo.getNodos().stream()
                .filter(n -> n.getEstado() != EstadoNodo.INACTIVO)
                .filter(n -> n.getTipo() != TipoNodo.SUGERIDO)
                .collect(Collectors.toList());

        if (DEBUG) {
            System.out.println("  Nodos activos: " + nodosActivos.size());
        }

        if (nodosActivos.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 nodos activos para generar la red");
        }

        Grafo grafoActivo = new Grafo();
        for (Nodo nodo : nodosActivos) {
            grafoActivo.agregarNodo(nodo);
        }

        generarAristasPotenciales(grafoActivo, conectarTodosPostes);

        if (DEBUG) {
            System.out.println("  Aristas generadas: " + grafoActivo.getAristas().size());
        }

        ResultadoRuta resultado = null;
        
        switch (algoritmo.toUpperCase()) {
            case "PRIM":
                if (central == null) {
                    throw new IllegalArgumentException("PRIM requiere un nodo central");
                }
                if (central.getEstado() == EstadoNodo.INACTIVO) {
                    throw new IllegalStateException("La central está INACTIVA");
                }
                if (DEBUG) System.out.println("  Ejecutando PRIM desde central ID: " + central.getId());
                resultado = prim.generarRed(grafoActivo, central);
                break;

            case "KRUSKAL":
                if (DEBUG) System.out.println("  Ejecutando KRUSKAL");
                resultado = kruskal.generarRed(grafoActivo);
                break;

            default:
                throw new IllegalArgumentException("Algoritmo no soportado: " + algoritmo);
        }
        
        if (DEBUG && resultado != null) {
            System.out.println("  Conexiones generadas: " + resultado.getConexiones().size());
        }
        
        return resultado;
    }

    /**
     * Genera todas las aristas potenciales entre nodos del grafo
     */
    private void generarAristasPotenciales(Grafo grafo, boolean conectarTodosPostes) {
        List<Nodo> nodos = grafo.getNodos();
        int n = nodos.size();
        int aristasCreadas = 0;
        
        if (DEBUG) {
            System.out.println("\n=== GENERANDO ARISTAS POTENCIALES ===");
            System.out.println("  Nodos en el grafo: " + n);
        }
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Nodo origen = nodos.get(i);
                Nodo destino = nodos.get(j);
                
                // Si NO estamos en modo "conectarTodosPostes", filtrar clientes sin conexión
                if (!conectarTodosPostes) {
                    if (destino.getTipo() == TipoNodo.CLIENTE && destino.getClientesActuales() >= 1) {
                        continue;
                    }
                    if (origen.getTipo() == TipoNodo.CLIENTE && origen.getClientesActuales() >= 1) {
                        continue;
                    }
                }
                
                double distancia = origen.distanciaA(destino);
                if (distanceValidator.esValida(origen, destino)) {
                    Arista arista = new Arista(origen, destino, distancia);
                    grafo.agregarArista(arista);
                    aristasCreadas++;
                }
            }
        }
        
        if (DEBUG) {
            System.out.println("  Total aristas creadas: " + aristasCreadas);
        }
    }

    /**
     * Versión legacy sin el parámetro (mantiene compatibilidad)
     */
    private void generarAristasPotenciales(Grafo grafo) {
        generarAristasPotenciales(grafo, false);
    }

    public ResultadoRuta conectarCliente(Grafo grafo, Nodo cliente) {
        if (grafo == null || cliente == null) {
            throw new IllegalArgumentException("Grafo y cliente no pueden ser nulos");
        }

        if (cliente.getTipo() != TipoNodo.CLIENTE) {
            throw new IllegalArgumentException("El nodo debe ser de tipo CLIENTE");
        }

        List<Nodo> postes = grafo.getNodos().stream()
                .filter(n -> esPoste(n))
                .filter(n -> n.getEstado() == EstadoNodo.DISPONIBLE)
                .collect(Collectors.toList());

        if (postes.isEmpty()) {
            throw new IllegalStateException("No hay postes disponibles para conectar el cliente");
        }

        Nodo posteMasCercano = null;
        double distanciaMinima = Double.MAX_VALUE;

        for (Nodo poste : postes) {
            double distancia = poste.distanciaA(cliente);
            
            if (distancia <= 1000.0 && distancia < distanciaMinima) {
                distanciaMinima = distancia;
                posteMasCercano = poste;
            }
        }

        if (posteMasCercano != null) {
            return dijkstra.calcularRuta(grafo, posteMasCercano, cliente);
        }

        throw new IllegalStateException("No se pudo conectar el cliente a la red. No hay postes cercanos.");
    }

    public Map<String, ResultadoRuta> compararAlgoritmos(Grafo grafo, Nodo central) {
        Map<String, ResultadoRuta> resultados = new HashMap<>();

        if (DEBUG) System.out.println("\n=== COMPARANDO ALGORITMOS ===");

        List<Nodo> nodosActivos = grafo.getNodos().stream()
                .filter(n -> n.getEstado() != EstadoNodo.INACTIVO)
                .filter(n -> n.getTipo() != TipoNodo.SUGERIDO)
                .collect(Collectors.toList());

        Grafo grafoActivo = new Grafo();
        for (Nodo nodo : nodosActivos) {
            grafoActivo.agregarNodo(nodo);
        }
        
        generarAristasPotenciales(grafoActivo, false);

        if (central != null && central.getEstado() != EstadoNodo.INACTIVO) {
            try {
                ResultadoRuta resultadoPrim = prim.generarRed(grafoActivo, central);
                resultados.put("PRIM", resultadoPrim);
            } catch (Exception e) {
                System.err.println("  PRIM - Error: " + e.getMessage());
                resultados.put("PRIM", new ResultadoRuta(new ArrayList<>(), -1, -1, 0));
            }
        }

        try {
            ResultadoRuta resultadoKruskal = kruskal.generarRed(grafoActivo);
            resultados.put("KRUSKAL", resultadoKruskal);
        } catch (Exception e) {
            System.err.println("  KRUSKAL - Error: " + e.getMessage());
            resultados.put("KRUSKAL", new ResultadoRuta(new ArrayList<>(), -1, -1, 0));
        }

        return resultados;
    }

    public double getCostoConexion(Nodo origen, Nodo destino) {
        if (!distanceValidator.esValida(origen, destino)) {
            return Double.MAX_VALUE;
        }
        double distancia = origen.distanciaA(destino);
        return costCalculator.calcularCosto(origen, destino, distancia);
    }

    public boolean esConexionValida(Nodo origen, Nodo destino) {
        return distanceValidator.esValida(origen, destino);
    }

    public double getDistanciaMaxima(Nodo origen, Nodo destino) {
        if (origen.getTipo() == TipoNodo.CENTRAL && esPoste(destino)) {
            return 800.0;
        }
        if (esPoste(origen) && esPoste(destino)) {
            return 500.0;
        }
        if ((esPoste(origen) && destino.getTipo() == TipoNodo.CLIENTE) ||
            (esPoste(destino) && origen.getTipo() == TipoNodo.CLIENTE)) {
            return 1000.0;
        }
        return 0.0;
    }

    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
}