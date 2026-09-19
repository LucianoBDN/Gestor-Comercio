package com.tunegocio.app.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import jakarta.persistence.EntityManagerFactory;

import org.junit.Test;

public class PersistenceConfigTest {

    @Test
    public void shouldCreateEntityManagerFactory() {
        assertNotNull(PersistenceConfig.getEntityManagerFactory());
    }

    @Test
    public void shouldAlwaysReturnTheSameInstance(){
        EntityManagerFactory primera = PersistenceConfig.getEntityManagerFactory();
        EntityManagerFactory segunda = PersistenceConfig.getEntityManagerFactory();

        assertSame(primera, segunda);
    }
}
