package com.tunegocio.app.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import com.tunegocio.app.domain.entity.Ingreso;
import com.tunegocio.app.domain.enums.MedioPago;

import jakarta.persistence.EntityManager;

public class IngresoRepository {

    private final EntityManager em;

    public IngresoRepository(EntityManager em) {
        this.em = em;
    }

    // crear

    public Ingreso save(Ingreso ingreso) {
        try {
            em.getTransaction().begin();

            if (ingreso.getId() == null) {
                em.persist(ingreso);
            } else {
                ingreso = em.merge(ingreso);
            }

            em.getTransaction().commit();

            return ingreso;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;
        }
    }

    // buscar por ID

    public Optional<Ingreso> findById(Long id) {
        Ingreso ingreso = em.find(Ingreso.class, id);
        return Optional.ofNullable(ingreso);
    }

    public List<Ingreso> findByFechaBetween(LocalDate desde, LocalDate hasta) {
        return em.createQuery("""
                SELECT i
                FROM Ingreso i
                WHERE i.fecha BETWEEN :desde AND :hasta
                ORDER BY i.fecha DESC
                """, Ingreso.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }

    public List<Ingreso> findByFechaBetweenAndMedioPago(LocalDate desde, LocalDate hasta, MedioPago medioPago) {
        return em.createQuery("""
                SELECT i
                FROM Ingreso i
                WHERE i.fecha BETWEEN :desde AND :hasta AND i.medioPago = :medioPago
                ORDER BY i.fecha DESC
                """, Ingreso.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("medioPago", medioPago)
                .getResultList();
    }

    public BigDecimal sumMontoByFechaBetween(LocalDate desde, LocalDate hasta) {
        return em.createQuery("""
                SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.fecha BETWEEN :desde AND :hasta
                """, BigDecimal.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();

    }
}
