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

        verifyDatabaseConnection();
    }

    private void verifyDatabaseConnection() {
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
            String mensaje = isConnectionError(error)
                    ? "No se pudo conectar a la base de datos. Verificá que PostgreSQL esté corriendo."
                    : "Error al conectar a la base de datos: " + error.getMessage();
            new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
        });

        Thread hilo = new Thread(tareaConexion);
        hilo.setDaemon(true);
        hilo.start();
    }

    private boolean isConnectionError(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof java.net.ConnectException) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }

    @Override
    public void stop() {
        PersistenceConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
