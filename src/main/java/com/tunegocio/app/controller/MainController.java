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
        cargarVista("/fxml/IngresosView.fxml");
    }

    @FXML
    private void onEgresosClick() {
        cargarVista("/fxml/EgresosView.fxml");
    }

    private void cargarVista(String rutaFxml) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(rutaFxml));
            contentArea.getChildren().setAll(vista);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la pantalla: " + e.getMessage()).showAndWait();
        }
    }
}
