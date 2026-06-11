package com.fibra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Localiza el archivo FXML directamente en tu carpeta personalizada de frontend
        File fxmlFile = new File("frontend/views/MainView.fxml");
        if (!fxmlFile.exists()) {
            System.err.println("¡Error Crítico! No se encontró el archivo MainView.fxml en: " + fxmlFile.getAbsolutePath());
            return;
        }

        // Convierte el archivo a URL para que FXMLLoader lo procese correctamente
        URL fxmlUrl = fxmlFile.toURI().toURL();
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        
        // Intenta cargar tu archivo de estilos CSS personalizados si existe
        File cssFile = new File("frontend/styles/application.css");
        if (cssFile.exists()) {
            scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
        }
        
        // Configura la ventana principal del sistema
        primaryStage.setTitle("Sistema de Optimización de Redes de Fibra Óptica - Frontend");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Arranca el ciclo de vida de la interfaz gráfica
        launch(args);
    }
}