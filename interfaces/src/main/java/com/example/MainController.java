package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MainController {

    @FXML
    private void handleButton1() {
        showAlert("Botón 1 presionado");
    }

    @FXML
    private void handleButton2() {
        showAlert("Botón 2 presionado");
    }

    @FXML
    private void handleButton3() {
        showAlert("Botón 3 presionado");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alerta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
