package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

import com.modelo.dominio.Observador;

/**
 * Repositorio para la entidad Observador
 * Responsabilidad: Gestionar persistencia de observadores
 */
public class ObservadorRepositorio extends RepositorioGenerico<Observador> {
    private final EntityManager entityManager;

    public ObservadorRepositorio(EntityManager entityManager) {
        super(entityManager, Observador.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca un observador por ID del estudiante con todas sus observaciones cargadas
     */
    public Optional<Observador> buscarPorEstudianteConObservaciones(Integer idEstudiante) {
        String jpql = "SELECT o FROM observador o " +
                     "LEFT JOIN FETCH o.observaciones obs " +
                     "LEFT JOIN FETCH obs.profesor " +
                     "WHERE o.estudiante.idEstudiante = :idEstudiante " +
                     "ORDER BY obs.fechaObservacion DESC";
        
        TypedQuery<Observador> query = entityManager.createQuery(jpql, Observador.class);
        query.setParameter("idEstudiante", idEstudiante);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca un observador por ID del estudiante
     */
    public Optional<Observador> buscarPorEstudiante(Integer idEstudiante) {
        String jpql = "SELECT o FROM observador o WHERE o.estudiante.idEstudiante = :idEstudiante";
        
        TypedQuery<Observador> query = entityManager.createQuery(jpql, Observador.class);
        query.setParameter("idEstudiante", idEstudiante);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Verifica si existe un observador para un estudiante
     */
    public boolean existePorEstudiante(Integer idEstudiante) {
        String jpql = "SELECT COUNT(o) FROM observador o WHERE o.estudiante.idEstudiante = :idEstudiante";
        
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("idEstudiante", idEstudiante)
            .getSingleResult();
        
        return count > 0;
    }
}