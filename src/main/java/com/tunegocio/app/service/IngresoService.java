package com.tunegocio.app.service;

import com.tunegocio.app.repository.IngresoRepository;

public class IngresoService {

    private final IngresoRepository ingresoRepository;

    public IngresoService(IngresoRepository ingresoRepository) {
        this.ingresoRepository = ingresoRepository;
    }
}
