package com.fibra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // CORRECCIÓN EXACTA: Apunta a donde están tus archivos según tu árbol de VS Code
        URL fxmlUrl = getClass().getResource("/com/fibra/frontend/views/MainView.fxml");
        
        if (fxmlUrl == null) {
            System.err.println("¡Error Crítico! No se encontró el archivo MainView.fxml.");
            System.err.println("Ruta intentada: /com/fibra/frontend/views/MainView.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        
        // CORRECCIÓN EXACTA CSS: Apunta a tu carpeta styles dentro del paquete
        URL cssUrl = getClass().getResource("/com/fibra/frontend/styles/application.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Nota: No se encontró 'application.css' en /com/fibra/frontend/styles/, usando estilos por defecto.");
        }
        
        // Configuración de la ventana principal
        primaryStage.setTitle("Sistema de Optimización de Redes de Fibra Óptica - Frontend");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}