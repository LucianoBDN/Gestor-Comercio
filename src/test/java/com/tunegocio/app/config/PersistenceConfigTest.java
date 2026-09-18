package com.tunegocio.app.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PersistenceConfigTest {

    @Test
    public void deberiaCrearElEntityManagerFactory() {
        assertNotNull(PersistenceConfig.getEntityManagerFactory());
    }
}
