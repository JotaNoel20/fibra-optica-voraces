package com.fibra.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckBox;

import java.util.Optional;

import javafx.event.ActionEvent;

/**
 * Controlador de la barra de herramientas.
 * Permite al usuario seleccionar qué tipo de componente de fibra óptica 
 * desea dibujar o si prefiere navegar/arrastrar el mapa con la mano.
 */
public class ToolbarController {

    @FXML private ToggleButton btnNavegar;
    @FXML private ToggleButton btnCentral;
    @FXML private ToggleButton btnPostePrincipal;
    @FXML private ToggleButton btnPosteSecundario;
    @FXML private ToggleButton btnCliente;
    @FXML private Button btnImplementarRed;
    @FXML private Button btnComparar;
    @FXML private Button btnLimpiarTodo;
    @FXML private Button btnBorrarConexiones;
    @FXML private CheckBox chkConectarTodosPostes;  // NUEVO: CheckBox para modo todos los postes

    private final ToggleGroup grupoHerramientas = new ToggleGroup();
    private MapController mapController;
    private MainController mainController;

    @FXML
    public void initialize() {
        btnNavegar.setToggleGroup(grupoHerramientas);
        btnCentral.setToggleGroup(grupoHerramientas);
        btnPostePrincipal.setToggleGroup(grupoHerramientas);
        btnPosteSecundario.setToggleGroup(grupoHerramientas);
        btnCliente.setToggleGroup(grupoHerramientas);
        
        marcarBotonesComoDesactivados();
        btnPosteSecundario.setSelected(true);
        btnPosteSecundario.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Configurar CheckBox por defecto (false = modo normal)
        chkConectarTodosPostes.setSelected(false);
        chkConectarTodosPostes.setText("🔌 Conectar todos los postes");
        chkConectarTodosPostes.setStyle("-fx-text-fill: #E0E0E0;");
    }

    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void seleccionarHerramienta(ActionEvent event) {
        if (mapController == null) return;

        ToggleButton botonPresionado = (ToggleButton) event.getSource();

        if (!botonPresionado.isSelected()) {
            botonPresionado.setSelected(true);
            return;
        }

        marcarBotonesComoDesactivados();
        botonPresionado.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-font-weight: bold;");

        if (botonPresionado == btnNavegar) {
            mapController.setModoNavegacionActivo(true);
        } else {
            mapController.setModoNavegacionActivo(false);

            if (botonPresionado == btnCentral) {
                mapController.setTipoNodoActual("CENTRAL");
            } else if (botonPresionado == btnPostePrincipal) {
                mapController.setTipoNodoActual("POSTE_PRINCIPAL");
            } else if (botonPresionado == btnPosteSecundario) {
                mapController.setTipoNodoActual("POSTE_SECUNDARIO");
            } else if (botonPresionado == btnCliente) {
                mapController.setTipoNodoActual("CLIENTE");
            }
        }
    }

    @FXML
    private void implementarRed(ActionEvent event) {
        if (mainController != null) {
            boolean conectarTodos = chkConectarTodosPostes.isSelected();
            mainController.implementarRed(conectarTodos);
        }
    }

    @FXML
    private void compararAlgoritmos(ActionEvent event) {
        if (mainController != null) {
            mainController.compararAlgoritmos();
        }
    }

    @FXML
    private void limpiarTodo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("Eliminar todos los nodos");
        confirmacion.setContentText("¿Estás seguro? Esta acción eliminará TODOS los nodos y conexiones. No se puede deshacer.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (mainController != null) {
                mainController.limpiarTodo();
            }
        }
    }

    @FXML
    private void borrarConexiones() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("Eliminar todas las conexiones");
        confirmacion.setContentText("¿Estás seguro? Esta acción eliminará TODAS las conexiones, pero conservará los nodos.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (mainController != null) {
                mainController.borrarConexiones();
            }
        }
    }

    private void marcarBotonesComoDesactivados() {
        String estiloBase = "-fx-background-color: #2D2D2D; -fx-text-fill: #B0B0B0;";
        btnNavegar.setStyle(estiloBase);
        btnCentral.setStyle(estiloBase);
        btnPostePrincipal.setStyle(estiloBase);
        btnPosteSecundario.setStyle(estiloBase);
        btnCliente.setStyle(estiloBase);
    }
}