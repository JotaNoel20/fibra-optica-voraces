package com.fibra.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.geometry.Point2D;
import com.fibra.backend2.dto.CalleDTO;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.services.NodoService;
import com.fibra.backend2.services.ConexionService;
import com.fibra.backend2.exceptions.SpatialException;
import com.fibra.frontend.map.MapRenderer;
import com.fibra.frontend.map.NodoVisual;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class MapController {

    @FXML private MapRenderer mapRenderer;
    private NodoService nodoService;
    private ConexionService conexionService;
    private String tipoNodoActual = "POSTE_SECUNDARIO"; 

    private boolean modoNavegacionActivo = false;
    private final double[] anchorMano = new double[2];

    private List<CalleDTO> listaCalles;
    private List<NodoDTO> listaNodos;
    private List<ConexionDTO> listaConexiones;
    
    private boolean callesDibujadas = false;

    public void setNodoService(NodoService nodoService) {
        this.nodoService = nodoService;
    }
    
    public void setConexionService(ConexionService conexionService) {
        this.conexionService = conexionService;
    }

    public void setDatosIniciales(List<CalleDTO> calles, List<NodoDTO> nodos, List<ConexionDTO> conexiones) {
        this.listaCalles = calles;
        this.listaNodos = nodos;
        this.listaConexiones = conexiones;
        this.callesDibujadas = false;
        solicitarRedibujado();
    }

    @FXML
    public void initialize() {
        if (mapRenderer != null) {
            mapRenderer.setOnMousePressed(this::manejarMousePresionado);
            mapRenderer.setOnMouseDragged(this::manejarMouseArrastrado);
            mapRenderer.setOnMouseReleased(this::manejarMouseLiberado);
            mapRenderer.setOnMouseClicked(this::manejarClicEnMapa);
            
            mapRenderer.widthProperty().addListener((obs, oldVal, newVal) -> {
                mapRenderer.actualizarDimensiones(newVal.doubleValue(), mapRenderer.getHeight());
                callesDibujadas = false;
                solicitarRedibujado();
            });
            mapRenderer.heightProperty().addListener((obs, oldVal, newVal) -> {
                mapRenderer.actualizarDimensiones(mapRenderer.getWidth(), newVal.doubleValue());
                callesDibujadas = false;
                solicitarRedibujado();
            });

            mapRenderer.sceneProperty().addListener((obsScene, viejaScene, nuevaScene) -> {
                if (nuevaScene != null) {
                    nuevaScene.setOnKeyPressed(keyEvent -> {
                        double paso = 40.0;
                        switch (keyEvent.getCode()) {
                            case UP:
                                mapRenderer.getPanTransform().setY(mapRenderer.getPanTransform().getY() + paso);
                                keyEvent.consume();
                                break;
                            case DOWN:
                                mapRenderer.getPanTransform().setY(mapRenderer.getPanTransform().getY() - paso);
                                keyEvent.consume();
                                break;
                            case LEFT:
                                mapRenderer.getPanTransform().setX(mapRenderer.getPanTransform().getX() + paso);
                                keyEvent.consume();
                                break;
                            case RIGHT:
                                mapRenderer.getPanTransform().setX(mapRenderer.getPanTransform().getX() - paso);
                                keyEvent.consume();
                                break;
                            default:
                                break;
                        }
                    });
                }
            });
        }
    }
  
    public void setModoNavegacionActivo(boolean activo) {
        this.modoNavegacionActivo = activo;
        if (mapRenderer != null) {
            mapRenderer.setCursor(activo ? Cursor.OPEN_HAND : Cursor.DEFAULT);
        }
    }

    private void manejarMousePresionado(MouseEvent event) {
        if (mapRenderer == null) return;
        boolean esModoNavegacion = modoNavegacionActivo && event.getButton() == javafx.scene.input.MouseButton.PRIMARY;
        boolean esClicCentral = event.getButton() == javafx.scene.input.MouseButton.MIDDLE;
        if (esModoNavegacion || esClicCentral) {
            if (modoNavegacionActivo) mapRenderer.setCursor(Cursor.CLOSED_HAND);
            anchorMano[0] = event.getX();
            anchorMano[1] = event.getY();
        }
    }

    private void manejarMouseArrastrado(MouseEvent event) {
        if (mapRenderer == null) return;
        boolean esModoNavegacion = modoNavegacionActivo && event.getButton() == javafx.scene.input.MouseButton.PRIMARY;
        boolean esClicCentral = event.getButton() == javafx.scene.input.MouseButton.MIDDLE;
        if (esModoNavegacion || esClicCentral) {
            double deltaX = event.getX() - anchorMano[0];
            double deltaY = event.getY() - anchorMano[1];
            mapRenderer.getPanTransform().setX(mapRenderer.getPanTransform().getX() + deltaX);
            mapRenderer.getPanTransform().setY(mapRenderer.getPanTransform().getY() + deltaY);
            anchorMano[0] = event.getX();
            anchorMano[1] = event.getY();
        }
    }

    private void manejarMouseLiberado(MouseEvent event) {
        if (mapRenderer != null && modoNavegacionActivo) {
            mapRenderer.setCursor(Cursor.OPEN_HAND);
        }
    }

    private void manejarClicEnMapa(MouseEvent event) {
        if (mapRenderer == null) return;
        if (modoNavegacionActivo && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) return;

        if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
            if (event.getTarget() instanceof NodoVisual) {
                mostrarMenuContextual((NodoVisual) event.getTarget(), event.getScreenX(), event.getScreenY());
            }
            return;
        }

        if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
            if (event.getTarget() instanceof NodoVisual) {
                return;
            }
            
            Point2D puntoEnLienzo = mapRenderer.getContenedorLienzo()
                    .sceneToLocal(event.getSceneX(), event.getSceneY());
            
            double longitud = mapRenderer.revertirX(puntoEnLienzo.getX());
            double latitud = mapRenderer.revertirY(puntoEnLienzo.getY());
            
            registrarNuevoNodo(latitud, longitud);
        }
    }

    private void registrarNuevoNodo(double latitud, double longitud) {
        if (nodoService == null) {
            mostrarAlerta("Error", "Servicio no inicializado", AlertType.ERROR);
            return;
        }
        NodoDTO nuevoNodo = new NodoDTO();
        nuevoNodo.setNombre(tipoNodoActual + "_" + System.currentTimeMillis());
        nuevoNodo.setTipo(tipoNodoActual);
        nuevoNodo.setEstado("DISPONIBLE");
        nuevoNodo.setLatitud(latitud);
        nuevoNodo.setLongitud(longitud);
        nuevoNodo.setCapacidadMax("CLIENTE".equalsIgnoreCase(tipoNodoActual) ? 1 : 16);
        nuevoNodo.setClientesActuales(0);
        try {
            nodoService.crearNodo(nuevoNodo);
            recargarDatos();
        } catch (SpatialException e) {
            mostrarAlerta("Validación Geográfica", e.getMessage(), AlertType.WARNING);
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private void mostrarMenuContextual(NodoVisual nodoVisual, double screenX, double screenY) {
        NodoDTO nodo = nodoVisual.getNodoData();
        String tipo = nodo.getTipo() != null ? nodo.getTipo().toUpperCase() : "";
        ContextMenu menu = new ContextMenu();
        
        // NUEVO: Opción Información
        MenuItem itemInfo = new MenuItem("ℹ️ Información");
        MenuItem itemInactivo = new MenuItem("🟫 Dar de Baja (INACTIVO)");
        MenuItem itemDisponible = new MenuItem("🟢 Reactivar (DISPONIBLE)");
        MenuItem itemEliminar = new MenuItem("❌ Eliminar");
        
        itemInfo.setOnAction(e -> mostrarInformacionNodo(nodo));
        itemInactivo.setOnAction(e -> actualizarEstado(nodoVisual, "INACTIVO"));
        itemDisponible.setOnAction(e -> actualizarEstado(nodoVisual, "DISPONIBLE"));
        itemEliminar.setOnAction(e -> {
            try {
                nodoService.eliminarNodo(nodo.getId());
                recargarDatos();
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage(), AlertType.ERROR);
            }
        });
        
        // Agregar Información al inicio del menú
        menu.getItems().add(itemInfo);
        menu.getItems().add(new SeparatorMenuItem());
        
        if ("POSTE_PRINCIPAL".equals(tipo) || "POSTE_SECUNDARIO".equals(tipo)) {
            if ("INACTIVO".equalsIgnoreCase(nodo.getEstado())) {
                menu.getItems().add(itemDisponible);
            } else {
                menu.getItems().add(itemInactivo);
            }
            menu.getItems().addAll(new SeparatorMenuItem(), itemEliminar);
        } else if ("CENTRAL".equals(tipo) || "CLIENTE".equals(tipo)) {
            menu.getItems().add(itemEliminar);
        }
        menu.show(mapRenderer, screenX, screenY);
    }
    
    /**
     * Muestra un diálogo con toda la información del nodo
     */
    private void mostrarInformacionNodo(NodoDTO nodo) {
        // Contar conexiones del nodo
        int numConexiones = 0;
        if (conexionService != null && listaConexiones != null) {
            numConexiones = (int) listaConexiones.stream()
                .filter(c -> c.getOrigenId() == nodo.getId() || c.getDestinoId() == nodo.getId())
                .count();
        }
        
        String capacidadInfo = "";
        if ("CLIENTE".equalsIgnoreCase(nodo.getTipo())) {
            capacidadInfo = "Cliente (máx 1 conexión)";
        } else if ("CENTRAL".equalsIgnoreCase(nodo.getTipo())) {
            capacidadInfo = "Central - Capacidad: " + nodo.getCapacidadMax();
        } else {
            capacidadInfo = "Poste - Clientes: " + nodo.getClientesActuales() + "/" + nodo.getCapacidadMax();
        }
        
        String mensaje = String.format(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "         INFORMACIÓN DEL NODO\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "📌 ID: %d\n" +
            "📛 Nombre: %s\n" +
            "🏷️  Tipo: %s\n" +
            "🟢 Estado: %s\n" +
            "📊 Capacidad: %s\n" +
            "🔌 Conexiones activas: %d\n" +
            "📍 Latitud: %.6f\n" +
            "📍 Longitud: %.6f\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            nodo.getId(),
            nodo.getNombre(),
            nodo.getTipo(),
            nodo.getEstado(),
            capacidadInfo,
            numConexiones,
            nodo.getLatitud(),
            nodo.getLongitud()
        );
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Información del Nodo");
        alert.setHeaderText("Nodo " + nodo.getId() + " - " + nodo.getTipo());
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void actualizarEstado(NodoVisual nodoVisual, String nuevoEstado) {
        try {
            NodoDTO nodo = nodoVisual.getNodoData();
            nodoService.actualizarEstadoNodo(nodo.getId(), nuevoEstado);
            nodo.setEstado(nuevoEstado);
            nodoVisual.actualizarEstiloVisual();
            
            recargarDatos();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void recargarDatos() {
        try {
            if (nodoService != null) {
                this.listaNodos = nodoService.listarNodos();
            }
            if (conexionService != null) {
                this.listaConexiones = conexionService.listarConexiones();
            }
            solicitarRedibujado();
        } catch (Exception e) {
            System.err.println("Error recargando datos: " + e.getMessage());
            solicitarRedibujado();
        }
    }

    public void setTipoNodoActual(String tipoNodo) {
        this.tipoNodoActual = tipoNodo;
    }

    public void setDatosConDespliegue(List<NodoDTO> nodos, List<ConexionDTO> conexiones) {
        this.listaNodos = nodos;
        this.listaConexiones = conexiones;
        solicitarRedibujado();
    }

    private void solicitarRedibujado() {
        if (mapRenderer == null || listaCalles == null) return;
        
        if (!callesDibujadas) {
            mapRenderer.limpiarMapa();
            mapRenderer.dibujarCalles(listaCalles);
            callesDibujadas = true;
        } else {
            mapRenderer.limpiarNodosYConexiones();
        }
        
        if (listaNodos != null) mapRenderer.dibujarNodos(listaNodos);
        if (listaConexiones != null) mapRenderer.dibujarConexiones(listaConexiones, listaNodos);
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}