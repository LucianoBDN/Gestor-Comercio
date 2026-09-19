package com.tunegocio.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tunegocio.app.domain.enums.MedioPago;

public class RegistrarIngresoCommand {

    private final LocalDate fecha;
    private final BigDecimal monto;
    private final MedioPago medioPago;
    private final String descripcion;

    public RegistrarIngresoCommand(LocalDate fecha, BigDecimal monto, MedioPago medioPago, String descripcion) {
        this.fecha = fecha;
        this.monto = monto;
        this.medioPago = medioPago;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
