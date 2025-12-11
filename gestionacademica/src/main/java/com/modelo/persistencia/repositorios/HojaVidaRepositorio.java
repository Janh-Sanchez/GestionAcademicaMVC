package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

import com.modelo.dominio.HojaVida;

/**
 * Repositorio para la entidad HojaVida
 * Responsabilidad: Gestionar persistencia de hojas de vida
 */
public class HojaVidaRepositorio extends RepositorioGenerico<HojaVida> {
    private final EntityManager entityManager;

    public HojaVidaRepositorio(EntityManager entityManager) {
        super(entityManager, HojaVida.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca la hoja de vida de un estudiante específico
     */
    public Optional<HojaVida> buscarPorEstudiante(Integer idEstudiante) {
        String jpql = "SELECT h FROM hoja_vida h WHERE h.estudiante.idEstudiante = :idEstudiante";
        TypedQuery<HojaVida> query = entityManager.createQuery(jpql, HojaVida.class);
        query.setParameter("idEstudiante", idEstudiante);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca la hoja de vida con el estudiante cargado
     */
    public Optional<HojaVida> buscarPorEstudianteConEstudiante(Integer idEstudiante) {
        String jpql = "SELECT h FROM hoja_vida h " +
                     "LEFT JOIN FETCH h.estudiante " +
                     "WHERE h.estudiante.idEstudiante = :idEstudiante";
        
        TypedQuery<HojaVida> query = entityManager.createQuery(jpql, HojaVida.class);
        query.setParameter("idEstudiante", idEstudiante);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Verifica si existe una hoja de vida para un estudiante
     */
    public boolean existePorEstudiante(Integer idEstudiante) {
        String jpql = "SELECT COUNT(h) FROM hoja_vida h WHERE h.estudiante.idEstudiante = :idEstudiante";
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("idEstudiante", idEstudiante)
            .getSingleResult();
        
        return count > 0;
    }

    /**
     * Actualiza una hoja de vida existente
     */
    public HojaVida actualizar(HojaVida hojaVida) {
        return entityManager.merge(hojaVida);
    }
}