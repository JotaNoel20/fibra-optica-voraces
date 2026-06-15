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
import com.fibra.backend2.config.DatabaseConnection;
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
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.application.Platform;

public class MainController {

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
                    System.out.println("──► CALLES: " + calles.size());
                    System.out.println("──► NODOS: " + nodos.size());
                    System.out.println("──► CONEXIONES: " + conexiones.size());
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

    @FXML
    public void implementarRed() {
        implementarRed(false);
    }
    
    public void implementarRed(boolean usarSoloDistancia) {
        List<NodoDTO> nodos = nodoService.listarNodos();
        boolean existeCentral = false;
        for (NodoDTO n : nodos) {
            if ("CENTRAL".equalsIgnoreCase(n.getTipo())) {
                existeCentral = true;
                break;
            }
        }
        
        if (!existeCentral) {
            mostrarAlertaError("Error", "No existe una central en la red.");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PRIM", "PRIM", "KRUSKAL");
        dialog.setTitle("Seleccionar Algoritmo");
        dialog.setHeaderText("¿Qué algoritmo deseas usar para generar la red?");
        dialog.setContentText("Algoritmo:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            final String algoritmo = result.get();
            generarRedConAlgoritmo(algoritmo, usarSoloDistancia);
        }
    }

    @FXML
    public void compararAlgoritmos() {
        List<NodoDTO> nodos = nodoService.listarNodos();
        boolean existeCentral = false;
        for (NodoDTO n : nodos) {
            if ("CENTRAL".equalsIgnoreCase(n.getTipo())) {
                existeCentral = true;
                break;
            }
        }
        
        if (!existeCentral) {
            mostrarAlertaError("Error", "No existe una central en la red.");
            return;
        }
        
        try {
            Grafo grafo = grafoBuilderService.construirGrafo();
            
            Nodo central = null;
            for (Nodo n : grafo.getNodos()) {
                if (n.getTipo() == TipoNodo.CENTRAL) {
                    central = n;
                    break;
                }
            }
            
            if (central == null) {
                mostrarAlertaError("Error", "No se encontró la central en el grafo.");
                return;
            }
            
            final Alert alertaEspera = new Alert(AlertType.INFORMATION);
            alertaEspera.setTitle("Procesando");
            alertaEspera.setHeaderText("Comparando algoritmos...");
            alertaEspera.setContentText("Por favor espere...");
            alertaEspera.show();
            
            final Grafo grafoFinal = grafo;
            final Nodo centralFinal = central;
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, ResultadoRuta> resultados = networkService.compararAlgoritmos(grafoFinal, centralFinal);
                        
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                alertaEspera.close();
                                mostrarComparacionResultados(resultados);
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                alertaEspera.close();
                                mostrarAlertaError("Error al comparar", e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }).start();
            
        } catch (Exception e) {
            mostrarAlertaError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarClientesConectados(final Set<Integer> idsClientes) {
        if (idsClientes == null || idsClientes.isEmpty()) {
            return;
        }
        
        String sql = "UPDATE nodos SET clientes_actuales = 1 WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (Integer id : idsClientes) {
                statement.setInt(1, id);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            if (DEBUG) System.out.println("  Clientes actualizados: " + idsClientes.size());
        } catch (SQLException e) {
            System.err.println("  Error actualizando clientes: " + e.getMessage());
        }
    }

    private void actualizarContadoresPostes() {
        String sql = """
            UPDATE nodos 
            SET clientes_actuales = (
                SELECT COUNT(*) FROM conexiones 
                WHERE (conexiones.origen_id = nodos.id AND conexiones.destino_id IN (SELECT id FROM nodos WHERE tipo = 'CLIENTE'))
                   OR (conexiones.destino_id = nodos.id AND conexiones.origen_id IN (SELECT id FROM nodos WHERE tipo = 'CLIENTE'))
            )
            WHERE tipo LIKE 'POSTE%'
            """;
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int actualizados = statement.executeUpdate();
            if (DEBUG) System.out.println("  Contadores de postes actualizados: " + actualizados);
        } catch (SQLException e) {
            System.err.println("  Error actualizando contadores de postes: " + e.getMessage());
        }
    }

    private void generarRedConAlgoritmo(final String algoritmo, final boolean usarSoloDistancia) {
        try {
            final Alert alertaEspera = new Alert(AlertType.INFORMATION);
            alertaEspera.setTitle("Procesando");
            alertaEspera.setHeaderText("Generando red con " + algoritmo + "...");
            alertaEspera.setContentText("Modo: " + (usarSoloDistancia ? "Solo distancia" : "Costo con penalización"));
            alertaEspera.show();
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long tiempoInicio = System.currentTimeMillis();
                    
                    try {
                        if (DEBUG) System.out.println("\n=== RESETEANDO ===");
                        conexionRepository.eliminarTodas();
                        nodoRepository.resetearContadoresPostes();
                        nodoRepository.resetearClientes();
                        
                        if (DEBUG) System.out.println("\n=== CONSTRUYENDO GRAFO ===");
                        Grafo grafo = grafoBuilderService.construirGrafo();
                        
                        Nodo central = null;
                        for (Nodo n : grafo.getNodos()) {
                            if (n.getTipo() == TipoNodo.CENTRAL) {
                                central = n;
                                break;
                            }
                        }
                        
                        if (central == null) {
                            Platform.runLater(() -> {
                                alertaEspera.close();
                                mostrarAlertaError("Error", "No se encontró una central");
                            });
                            return;
                        }
                        
                        if (DEBUG) System.out.println("\n=== EJECUTANDO ALGORITMO " + algoritmo + " ===");
                        final ResultadoRuta resultado = networkService.generarRed(grafo, central, algoritmo, usarSoloDistancia);
                        
                        if (resultado.getConexiones().isEmpty() && resultado.getCostoTotal() < 0) {
                            Platform.runLater(() -> {
                                alertaEspera.close();
                                mostrarAlertaError("Error", "No se pudo generar la red");
                            });
                            return;
                        }
                        
                        if (DEBUG) System.out.println("\n=== GUARDANDO CONEXIONES ===");
                        List<ConexionDTO> conexionesBatch = new ArrayList<>();
                        for (Arista arista : resultado.getConexiones()) {
                            ConexionDTO conexion = new ConexionDTO();
                            conexion.setOrigenId(arista.getOrigen().getId());
                            conexion.setDestinoId(arista.getDestino().getId());
                            conexion.setDistancia(arista.getDistancia());
                            conexionesBatch.add(conexion);
                        }
                        
                        if (!conexionesBatch.isEmpty()) {
                            conexionRepository.guardarBatch(conexionesBatch);
                        }
                        
                        if (DEBUG) System.out.println("\n=== ACTUALIZANDO CLIENTES ===");
                        actualizarClientesConectados(resultado.getIdsClientesConectados());
                        
                        if (DEBUG) System.out.println("\n=== ACTUALIZANDO POSTES ===");
                        actualizarContadoresPostes();
                        
                        final long tiempoEjecucion = System.currentTimeMillis() - tiempoInicio;
                        
                        Platform.runLater(() -> {
                            alertaEspera.close();
                            mostrarResultado(algoritmo, resultado, usarSoloDistancia, tiempoEjecucion);
                            refrescarMapa();
                        });
                        
                    } catch (final Exception e) {
                        System.err.println("=== ERROR GENERANDO RED ===");
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            alertaEspera.close();
                            mostrarAlertaError("Error al generar red", e.getMessage());
                        });
                    }
                }
            }).start();
            
        } catch (Exception e) {
            mostrarAlertaError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarResultado(String algoritmo, ResultadoRuta resultado, boolean usarSoloDistancia, long tiempoMs) {
        String modoTexto = usarSoloDistancia ? " (Modo: Solo distancia)" : " (Modo: Costo con penalización)";
        String tiempoTexto = (tiempoMs < 1000) ? String.format("%d ms", tiempoMs) : String.format("%.2f s", tiempoMs / 1000.0);
        
        String mensaje = String.format(
                "✅ Red generada exitosamente con %s%s\n\n" +
                "📊 Conexiones creadas: %d\n" +
                "📏 Distancia total: %.2f m\n" +
                "💰 Costo total: %.2f\n" +
                "🗼 Postes utilizados: %d\n" +
                "👥 Clientes conectados: %d\n" +
                "⏱️ Tiempo de ejecución: %s",
                algoritmo, modoTexto,
                resultado.getConexiones().size(),
                resultado.getDistanciaTotal(),
                resultado.getCostoTotal(),
                resultado.getCantidadPostes(),
                resultado.getCantidadClientesConectados(),
                tiempoTexto
        );
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Red Generada");
        alert.setHeaderText("Resultado de " + algoritmo);
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinWidth(550);
        alert.getDialogPane().setMinHeight(400);
        alert.showAndWait();
    }

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
                "│ Clientes: %d    │ Clientes: %d    │\n" +
                "└─────────────────┴─────────────────┘%s",
                prim != null ? prim.getCostoTotal() : -1,
                kruskal != null ? kruskal.getCostoTotal() : -1,
                prim != null ? prim.getDistanciaTotal() : -1,
                kruskal != null ? kruskal.getDistanciaTotal() : -1,
                prim != null ? prim.getCantidadPostes() : 0,
                kruskal != null ? kruskal.getCantidadPostes() : 0,
                prim != null ? prim.getCantidadClientesConectados() : 0,
                kruskal != null ? kruskal.getCantidadClientesConectados() : 0,
                ganador
        );
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Comparación de Algoritmos");
        alert.setHeaderText("PRIM vs KRUSKAL");
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinWidth(550);
        alert.getDialogPane().setMinHeight(400);
        alert.showAndWait();
    }

    public void refrescarMapa() {
        try {
            List<NodoDTO> nodos = nodoService.listarNodos();
            List<ConexionDTO> conexiones = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodos, conexiones);
                mapController.recargarDatos();
            }
        } catch (Exception e) {
            System.err.println("Error al refrescar mapa: " + e.getMessage());
        }
    }

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

    public void limpiarTodo() {
        try {
            conexionRepository.eliminarTodas();
            nodoService.eliminarTodosNodos();
            
            List<NodoDTO> nodosVacios = nodoService.listarNodos();
            List<ConexionDTO> conexionesVacias = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodosVacios, conexionesVacias);
                mapController.recargarDatos();
            }
            
            mostrarAlertaError("Éxito", "Se han eliminado todos los nodos y conexiones.");
        } catch (Exception e) {
            mostrarAlertaError("Error", "No se pudo limpiar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void borrarConexiones() {
        try {
            conexionRepository.eliminarTodas();
            nodoRepository.resetearContadoresPostes();
            nodoRepository.resetearClientes();
            
            List<NodoDTO> nodos = nodoService.listarNodos();
            List<ConexionDTO> conexionesVacias = conexionService.listarConexiones();
            
            if (mapController != null) {
                mapController.setDatosConDespliegue(nodos, conexionesVacias);
                mapController.recargarDatos();
            }
            
            mostrarAlertaError("Éxito", "Se han eliminado todas las conexiones.");
        } catch (Exception e) {
            mostrarAlertaError("Error", "No se pudo eliminar las conexiones: " + e.getMessage());
            e.printStackTrace();
        }
    }
}