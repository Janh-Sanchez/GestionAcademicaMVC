package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;

import java.util.List;

import com.modelo.dominio.Observacion;

/**
 * Repositorio para la entidad Observacion
 * Responsabilidad: Gestionar persistencia de observaciones
 */
public class ObservacionRepositorio extends RepositorioGenerico<Observacion> {
    private final EntityManager entityManager;

    public ObservacionRepositorio(EntityManager entityManager) {
        super(entityManager, Observacion.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca todas las observaciones de un observador ordenadas por fecha (más recientes primero)
     */
    public List<Observacion> buscarPorObservadorOrdenadas(Integer idObservador) {
        String jpql = "SELECT o FROM observacion o " +
                     "LEFT JOIN FETCH o.profesor " +
                     "WHERE o.observador.idObservador = :idObservador " +
                     "ORDER BY o.fechaObservacion DESC";
        
        return entityManager.createQuery(jpql, Observacion.class)
            .setParameter("idObservador", idObservador)
            .getResultList();
    }

    /**
     * Cuenta las observaciones de un observador
     */
    public Long contarPorObservador(Integer idObservador) {
        String jpql = "SELECT COUNT(o) FROM observacion o WHERE o.observador.idObservador = :idObservador";
        
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("idObservador", idObservador)
            .getSingleResult();
    }
}