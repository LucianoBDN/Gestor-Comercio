package com.tunegocio.app.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void onIngresosClick() {
        loadView("/fxml/IngresosView.fxml");
    }

    @FXML
    private void onEgresosClick() {
        loadView("/fxml/EgresosView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la pantalla: " + e.getMessage()).showAndWait();
        }
    }
}
