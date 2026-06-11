package frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import java.util.Arrays;
import java.util.List;

public class ToolbarController {

    // Enum obligatorio exigido por el manual para controlar la colocación de elementos
    public enum TipoNodo {
        CENTRAL, POSTE_SECUNDARIO, CLIENTE, SUGERIDO
    }

    private TipoNodo modoActual;

    @FXML
    public void seleccionarCentral() {
        modoActual = TipoNodo.CENTRAL;
        System.out.println("Modo de edición: Colocar CENTRAL");
    }

    @FXML
    public void seleccionarPoste() {
        modoActual = TipoNodo.POSTE_SECUNDARIO;
        System.out.println("Modo de edición: Colocar POSTE SECUNDARIO");
    }

    @FXML
    public void seleccionarCliente() {
        modoActual = TipoNodo.CLIENTE;
        System.out.println("Modo de edición: Colocar CLIENTE");
    }

    @FXML
    public void generarRed() {
        // Despliega un cuadro de opción flotante para elegir el algoritmo voraz
        List<String> opciones = Arrays.asList("Prim", "Kruskal");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Prim", opciones);
        dialog.setTitle("Generación de Red Óptica");
        dialog.setHeaderText("Seleccione el algoritmo voraz para la optimización:");
        dialog.setContentText("Algoritmo:");

        dialog.showAndWait().ifPresent(algoritmo -> {
            System.out.println("Frontend enviando petición a networkService.generarRed() usando: " + algoritmo);
            // Integración futura: networkService.generarRed(grafo, central, algoritmo);
        });
    }

    @FXML
    public void compararAlgoritmos() {
        System.out.println("Frontend solicitando métricas de comparación al Backend...");
        
        // Muestra una ventana emergente con la comparativa de rendimiento
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Comparativa de Rendimiento");
        alert.setHeaderText("Métricas del Sistema: Prim vs Kruskal");
        alert.setContentText("Resultados calculados por el Backend:\n\n- Costo total de tendido\n- Distancia geométrica total\n- Cantidad de postes utilizados");
        alert.showAndWait();
    }

    public TipoNodo getModoActual() {
        return modoActual;
    }
}