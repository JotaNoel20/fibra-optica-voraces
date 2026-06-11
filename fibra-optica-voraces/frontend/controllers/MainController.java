package frontend.controllers;

import javafx.fxml.FXML;

public class MainController {

    // Estas variables se enlazarán automáticamente si usas <fx:include> en el FXML principal
    @FXML private ToolbarController toolbarController;
    @FXML private MapController mapController;
    @FXML private LegendController legendController;

    @FXML
    public void initialize() {
        // Se ejecuta automáticamente cuando JavaFX termina de cargar el FXML
        System.out.println("MainController: Inicializando vistas, herramientas y leyenda...");
        
        // Carga inicial obligatoria de la infraestructura base
        cargarProyecto();
    }

    @FXML
    public void cargarProyecto() {
        System.out.println("MainController: Solicitando datos de infraestructura al Backend...");
        try {
            // Aquí se conectarán los servicios del Backend que tú definiste:
            // calleService.obtenerCalles();
            // nodoService.listarNodos();
            // conexionService.listarConexiones();
            
            System.out.println("MainController: Infraestructura base cargada con éxito.");
        } catch (Exception e) {
            System.err.println("Error al conectar con los servicios de Backend: " + e.getMessage());
        }
    }
}