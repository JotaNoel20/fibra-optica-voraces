package com.fibra.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

/**
 * Controlador de la leyenda del mapa.
 * Gestiona la visualización de la guía de colores y tipos de infraestructura
 * de la red de fibra óptica desplegada.
 */
public class LegendController {

    @FXML private VBox contenedorLeyenda;
    @FXML private Label lblTitulo;

    @FXML
    public void initialize() {
        // Configuramos un fondo oscuro translúcido elegante para la cajita de la leyenda
        if (contenedorLeyenda != null) {
            contenedorLeyenda.setStyle(
                "-fx-background-color: rgba(30, 30, 30, 0.85);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #4A4A4A;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 15;"
            );
        }
        
        if (lblTitulo != null) {
            lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }
}