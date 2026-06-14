package com.fibra.frontend.controllers;

import javafx.fxml.FXML;
import com.fibra.backend2.dto.CalleDTO;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.services.CalleService;
import com.fibra.backend2.services.NodoService;
import com.fibra.backend2.services.ConexionService;
import com.fibra.backend2.services.SpatialValidationService;
import com.fibra.backend2.services.GrafoBuilderService;
import com.fibra.backend2.repositories.CalleRepository;
import com.fibra.backend2.repositories.NodoRepository;
import com.fibra.backend2.repositories.ConexionRepository;
import com.fibra.backend2.mapper.NodoMapper;
import com.fibra.backend1.graph.Grafo;
import com.fibra.backend1.graph.Nodo;
import com.fibra.backend1.graph.Arista;
import com.fibra.backend1.graph.ResultadoRuta;
import com.fibra.backend1.services.NetworkOptimizationService;
import com.fibra.backend1.enums.TipoNodo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.application.Platform;

public class MainController {

    // Flag para activar/desactivar logs detallados
    private static final boolean DEBUG = false;

    private final CalleRepository calleRepository = new CalleRepository();
    private final NodoRepository nodoRepository = new NodoRepository();
    private final ConexionRepository conexionRepository = new ConexionRepository();
    
    private final SpatialValidationService spatialValidationService = new SpatialValidationService(calleRepository);
    private final NodoMapper nodoMapper = new NodoMapper();

    private final CalleService calleService = new CalleService(calleRepository);
    private final NodoService nodoService = new NodoService(nodoRepository, spatialValidationService);
    private final ConexionService conexionService = new ConexionService(conexionRepository, nodoRepository);
    private final GrafoBuilderService grafoBuilderService = new GrafoBuilderService(
            nodoRepository, conexionRepository, nodoMapper
    );
    
    private final NetworkOptimizationService networkService = new NetworkOptimizationService();

    @FXML private MapController mapController;
    @FXML private ToolbarController toolbarController;
    @FXML private LegendController legendController;

    @FXML
    public void initialize() {
        if (mapController != null) {
            mapController.setNodoService(nodoService);
            mapController.setConexionService(conexionService);
            
            try {
                List<CalleDTO> calles = calleService.obtenerCalles();
                List<NodoDTO> nodos = nodoService.listarNodos();
                List<ConexionDTO> conexiones = conexionService.listarConexiones();
                
                if (DEBUG) {
                    System.out.println("──► CANTIDAD DE CALLES RECUPERADAS: " + calles.size());
                    System.out.println("──► CANTIDAD DE NODOS RECUPERADOS: " + nodos.size());
                    System.out.println("──► CANTIDAD DE CONEXIONES RECUPERADAS: " + conexiones.size());
                }
                
                mapController.setDatosIniciales(calles, nodos, conexiones);
            } catch (Exception e) {
                mostrarAlertaError("Error de Conexión", "No se pudieron recuperar los datos: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (toolbarController != null && mapController != null) {
            toolbarController.setMapController(mapController);
            toolbarController.setMainController(this);
        }
    }

    /**
     * Genera la red utilizando el algoritmo seleccionado por el usuario (método legacy)
     */
    @FXML
    public void implementarRed() {
        implementarRed(false);
    }
    
    /**
     * Genera la red utilizando el algoritmo seleccionado por el usuario
     * @param conectarTodosPostes true = conecta todos los postes aunque no tengan clientes
     */
    public void implementarRed(boolean conectarTodosPostes) {
        List<NodoDTO> nodos = nodoService.listarNodos();
        boolean existeCentral = nodos.stream()
                .anyMatch(n -> "CENTRAL".equalsIgnoreCase(n.getTipo()));
        
        if (!existeCentral) {
            mostrarAlertaError("Error", "No existe una central en la red. Debe agregar una central primero.");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PRIM", "PRIM", "KRUSKAL");
        dialog.setTitle("Seleccionar Algoritmo");
        dialog.setHeaderText("¿Qué algoritmo deseas usar para generar la red?");
        dialog.setContentText("Algoritmo:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(algoritmo -> {
            generarRedConAlgoritmo(algoritmo, conectarTodosPostes);
        });
    }

    /**
     * Compara los resultados de PRIM y KRUSKAL
     */
    @FXML
    public void compararAlgoritmos() {
        List<NodoDTO> nodos = nodoService.listarNodos();
        boolean existeCentral = nodos.stream()
                .anyMatch(n -> "CENTRAL".equalsIgnoreCase(n.getTipo()));
        
        if (!existeCentral) {
            mostrarAlertaError("Error", "No existe una central en la red.");
            return;
        }
        
        try {
            Grafo grafo = grafoBuilderService.construirGrafo();
            
            Nodo central = grafo.getNodos().stream()
                    .filter(n -> n.getTipo() == TipoNodo.CENTRAL)
                    .findFirst()
                    .orElse(null);
            
            if (central == null) {
                mostrarAlertaError("Error", "No se encontró la central en el grafo.");
                return;
            }
            
            Alert alertaEspera = new Alert(AlertType.INFORMATION);
            alertaEspera.setTitle("Procesando");
            alertaEspera.setHeaderText("Comparando algoritmos...");
            alertaEspera.setContentText("Por favor espere...");
            alertaEspera.show();
            
            new Thread(() -> {
                try {
                    Map<String, ResultadoRuta> resultados = networkService.compararAlgoritmos(grafo, central);
                    
                    Platform.runLater(() -> {
                        alertaEspera.close();
                        mostrarComparacionResultados(resultados);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        alertaEspera.close();
                        mostrarAlertaError("Error al comparar", e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();
            
        } catch (Exception e) {
            mostrarAlertaError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera la red con el algoritmo especificado
     */
    private void generarRedConAlgoritmo(String algoritmo, boolean conectarTodosPostes) {
        try {
            Alert alertaEspera = new Alert(AlertType.INFORMATION);
            alertaEspera.setTitle("Procesando");
            alertaEspera.setHeaderText("Generando red con " + algoritmo + "...");
            alertaEspera.setContentText("Modo: " + (conectarTodosPostes ? "Conectar todos los postes" : "Normal"));
            alertaEspera.show();
            
            new Thread(() -> {
                long tiempoInicio = System.currentTimeMillis();
                
                try {
                    if (DEBUG) System.out.println("\n=== PASO 1: Construyendo grafo desde BD ===");
                    Grafo grafo = grafoBuilderService.construirGrafo();
                    
                    if (DEBUG) {
                        System.out.println("  Nodos en grafo: " + grafo.getNodos().size());
                        for (Nodo n : grafo.getNodos()) {
                            System.out.println("    Nodo ID: " + n.getId() + " | Tipo: " + n.getTipo() + " | Estado: " + n.getEstado());
                        }
                    }
                    
                    Nodo central = grafo.getNodos().stream()
                            .filter(n -> n.getTipo() == TipoNodo.CENTRAL)
                            .findFirst()
                            .orElse(null);
                    
                    if (central == null) {
                        Platform.runLater(() -> {
                            alertaEspera.close();
                            mostrarAlertaError("Error", "No se encontró una central en la red");
                        });
                        return;
                    }
                    
                    if (DEBUG) {
                        System.out.println("\n=== PASO 2: Ejecutando algoritmo " + algoritmo + " ===");
                        System.out.println("  Modo conectarTodosPostes: " + conectarTodosPostes);
                    }
                    
                    ResultadoRuta resultado = networkService.generarRed(grafo, central, algoritmo, conectarTodosPostes);
                    
                    if (DEBUG) System.out.println("\n=== PASO 3: Procesando resultado ===");
                    
                    if (resultado.getConexiones().isEmpty() && resultado.getCostoTotal() < 0) {
                        Platform.runLater(() -> {
                            alertaEspera.close();
                            mostrarAlertaError("Error", "No se pudo generar la red. Verifique que haya nodos suficientes.");
                        });
                        return;
                    }
                    
                    if (DEBUG) System.out.println("\n=== PASO 4: Eliminando conexiones existentes ===");
                    conexionRepository.eliminarTodas();
                    
                    if (DEBUG) System.out.println("\n=== PASO 4.5: Reseteando contadores de postes ===");
                    nodoRepository.resetearContadoresPostes();
                    
                    if (DEBUG) System.out.println("\n=== PASO 5: Guardando nuevas conexiones en BD (BATCH) ===");
                    
                    // PREPARAR BATCH DE CONEXIONES
                    List<ConexionDTO> conexionesBatch = new ArrayList<>();
                    for (Arista arista : resultado.getConexiones()) {
                        ConexionDTO conexion = new ConexionDTO();
                        conexion.setOrigenId(arista.getOrigen().getId());
                        conexion.setDestinoId(arista.getDestino().getId());
                        conexion.setDistancia(arista.getDistancia());
                        conexionesBatch.add(conexion);
                    }
                    
                    if (DEBUG) System.out.println("  Conexiones a guardar: " + conexionesBatch.size());
                    
                    // GUARDAR BATCH
                    if (!conexionesBatch.isEmpty()) {
                        conexionRepository.guardarBatch(conexionesBatch);
                        if (DEBUG) System.out.println("  Batch guardado exitosamente");
                    }
                    
                    long tiempoFin = System.currentTimeMillis();
                    long tiempoEjecucion = tiempoFin - tiempoInicio;
                    
                    Platform.runLater(() -> {
                        alertaEspera.close();
                        mostrarResultado(algoritmo, resultado, conectarTodosPostes, tiempoEjecucion);
                        refrescarMapa();
                    });
                    
                } catch (Exception e) {
                    System.err.println("\n=== ERROR GENERANDO RED ===");
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        alertaEspera.close();
                        mostrarAlertaError("Error al generar red", e.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception e) {
            mostrarAlertaError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra los resultados de la generación de red
     */
    private void mostrarResultado(String algoritmo, ResultadoRuta resultado, boolean conectarTodosPostes, long tiempoMs) {
        String modoTexto = conectarTodosPostes ? " (Modo: Todos los postes)" : "";
        
        String tiempoTexto = (tiempoMs < 1000) ? String.format("%d ms", tiempoMs) : String.format("%.2f s", tiempoMs / 1000.0);
        
        String mensaje = String.format(
                "✅ Red generada exitosamente con %s%s\n\n" +
                "📊 Conexiones creadas: %d\n" +
                "📏 Distancia total: %.2f m\n" +
                "💰 Costo total: %.2f\n" +
                "🗼 Postes utilizados: %d\n" +
                "⏱️ Tiempo de ejecución: %s",
                algoritmo, modoTexto,
                resultado.getConexiones().size(),
                resultado.getDistanciaTotal(),
                resultado.getCostoTotal(),
                resultado.getCantidadPostes(),
                tiempoTexto
        );
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Red Generada");
        alert.setHeaderText("Resultado de " + algoritmo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra la comparación entre PRIM y KRUSKAL
     */
    private void mostrarComparacionResultados(Map<String, ResultadoRuta> resultados) {
        ResultadoRuta prim = resultados.get("PRIM");
        ResultadoRuta kruskal = resultados.get("KRUSKAL");
        
        String ganador = "";
        if (prim != null && kruskal != null && prim.getCostoTotal() >= 0 && kruskal.getCostoTotal() >= 0) {
            if (kruskal.getCostoTotal() < prim.getCostoTotal()) {
                ganador = "\n\n✅ KRUSKAL es mejor (menor costo)";
            } else if (prim.getCostoTotal() < kruskal.getCostoTotal()) {
                ganador = "\n\n✅ PRIM es mejor (menor costo)";
            } else {
                ganador = "\n\n⚖️ Ambos algoritmos tienen el mismo costo";
            }
        }
        
        String mensaje = String.format(
                "┌─────────────────┬─────────────────┐\n" +
                "│      PRIM       │     KRUSKAL     │\n" +
                "├─────────────────┼─────────────────┤\n" +
                "│ Costo: %.2f     │ Costo: %.2f    │\n" +
                "│ Distancia: %.2f │ Distancia: %.2f │\n" +
                "│ Postes: %d      │ Postes: %d      │\n" +
                "└─────────────────┴─────────────────┘%s",
                prim != null ? prim.getCostoTotal() : -1,
                kruskal != null ? kruskal.getCostoTotal() : -1,
                prim != null ? prim.getDistanciaTotal() : -1,
                kruskal != null ? kruskal.getDistanciaTotal() : -1,
                prim != null ? prim.getCantidadPostes() : 0,
                kruskal != null ? kruskal.getCantidadPostes() : 0,
                ganador
        );
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Comparación de Algoritmos");
        alert.setHeaderText("PRIM vs KRUSKAL");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Refresca el mapa con los datos actuales de la BD
     */
    public void refrescarMapa() {
        try {
            List<NodoDTO> nodos = nodoService.listarNodos();
            List<ConexionDTO> conexiones = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodos, conexiones);
            }
        } catch (Exception e) {
            System.err.println("Error al refrescar mapa: " + e.getMessage());
        }
    }

    /**
     * Despliega la red automática (método legacy)
     */
    @FXML
    public void desplegarRedAutomatica() {
        implementarRed();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Elimina TODOS los nodos y conexiones
     */
    public void limpiarTodo() {
        try {
            conexionRepository.eliminarTodas();
            nodoService.eliminarTodosNodos();
            
            List<NodoDTO> nodosVacios = nodoService.listarNodos();
            List<ConexionDTO> conexionesVacias = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodosVacios, conexionesVacias);
            }
            
            mostrarAlertaError("Éxito", "Se han eliminado todos los nodos y conexiones.");
        } catch (Exception e) {
            mostrarAlertaError("Error", "No se pudo limpiar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elimina SOLO las conexiones
     */
    public void borrarConexiones() {
        try {
            conexionRepository.eliminarTodas();
            nodoRepository.resetearContadoresPostes();
            
            List<NodoDTO> nodos = nodoService.listarNodos();
            List<ConexionDTO> conexionesVacias = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodos, conexionesVacias);
            }
            
            mostrarAlertaError("Éxito", "Se han eliminado todas las conexiones.");
        } catch (Exception e) {
            mostrarAlertaError("Error", "No se pudo eliminar las conexiones: " + e.getMessage());
            e.printStackTrace();
        }
    }
}