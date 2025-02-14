package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el archivo FXML
        VBox root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));

        // Crear la escena y mostrarla
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Menú Interactivo con JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}