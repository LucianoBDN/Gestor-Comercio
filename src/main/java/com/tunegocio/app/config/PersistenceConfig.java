package com.tunegocio.app.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceConfig {

    private static EntityManagerFactory emf;

    private PersistenceConfig() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("gestorComercioPU", loadCredentials());
        }
        return emf;
    }

    private static Map<String, String> loadCredentials() {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("db.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se encontró db.properties en la raíz del proyecto. "
                            + "Copiá db.properties.example a db.properties y completá tus credenciales locales.",
                    e);
        }

        Map<String, String> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.user", props.getProperty("db.user"));
        overrides.put("jakarta.persistence.jdbc.password", props.getProperty("db.password"));
        return overrides;
    }

    public static synchronized void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }
}
