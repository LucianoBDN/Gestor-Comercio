package com.tunegocio.app;

import java.io.IOException;

import com.tunegocio.app.config.PersistenceConfig;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Gestor Comercio");
        stage.setScene(scene);
        stage.show();

        verificarConexionABase();
    }

    private void verificarConexionABase() {
        Task<Void> tareaConexion = new Task<>() {
            @Override
            protected Void call() {
                PersistenceConfig.getEntityManagerFactory();
                return null;
            }
        };

        tareaConexion.setOnSucceeded(event ->
                System.out.println("Conexión a la base de datos exitosa"));

        tareaConexion.setOnFailed(event -> {
            Throwable error = tareaConexion.getException();
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo conectar a la base de datos: " + error.getMessage())
                    .showAndWait();
        });

        Thread hilo = new Thread(tareaConexion);
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void stop() {
        PersistenceConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
