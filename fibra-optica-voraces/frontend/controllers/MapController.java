package frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MapController {

    @FXML
    public void onMapClick(double latitud, double longitud) {
        System.out.println("Interacción en mapa detectada en coordenadas: X=" + latitud + ", Y=" + longitud);
        
        try {
            // REGLA: El Frontend NO realiza validaciones espaciales. Solo dispara la petición.
            System.out.println("Enviando coordenadas al Backend mediante nodoService.crearNodo()...");
            // Integración futura: nodoService.crearNodo(dto);
            
        } catch (Exception e) {
            // Captura de errores espaciales (ej. SpatialException de PostGIS) devueltos por el Backend
            mostrarAlertaError("Fallo de Validación Espacial", "El nodo no cumple con las reglas topológicas permitidas.");
        }
    }

    public void mostrarPanelInformacion(Object nodoDTO) {
        // REGLA: Los datos provienen estrictamente limpios del DTO, el Frontend jamás calcula nada.
        System.out.println("--- Mostrando Detalles Técnicos del Nodo ---");
        // Ejemplo de despliegue en labels:
        // txtNombre.setText(nodoDTO.getNombre());
        // txtCapacidad.setText(nodoDTO.getCapacidadDisponible());
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}