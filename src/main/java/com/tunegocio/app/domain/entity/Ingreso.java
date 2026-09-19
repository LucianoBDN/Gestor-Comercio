package com.tunegocio.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

import com.tunegocio.app.domain.enums.MedioPago;

import java.math.BigDecimal;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "ingresos", indexes = {
        @Index(name = "idx_ingresos_fecha", columnList = "fecha"),
})
public class Ingreso {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false, name = "medio_pago")
    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    @Column(length = 255, nullable = true)
    private String descripcion;

    public Ingreso() {
    }

    public Ingreso(LocalDate fecha, BigDecimal monto, MedioPago medioPago, String descripcion) {
        this.fecha = fecha;
        this.monto = monto;
        this.medioPago = medioPago;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(MedioPago medioPago) {
        this.medioPago = medioPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
