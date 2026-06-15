package com.fibra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/com/fibra/frontend/views/MainView.fxml");
        
        if (fxmlUrl == null) {
            System.err.println("¡Error Crítico! No se encontró el archivo MainView.fxml.");
            System.err.println("Ruta intentada: /com/fibra/frontend/views/MainView.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        
        URL cssUrl = getClass().getResource("/com/fibra/frontend/styles/application.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Nota: No se encontró 'application.css'");
        }
        
        primaryStage.setTitle("Sistema de Optimización de Redes de Fibra Óptica - Frontend");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}