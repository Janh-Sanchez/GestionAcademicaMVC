package com.persistencia.repositorios;

import com.dominio.Estado;
import com.persistencia.entidades.EstudianteEntity;
import com.persistencia.entidades.PreinscripcionEntity;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PreinscripcionRepositorio extends RepositorioGenerico<PreinscripcionEntity> {
    private final EntityManager entityManager;

    public PreinscripcionRepositorio(EntityManager entityManager) {
        super(entityManager, PreinscripcionEntity.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca preinscripciones por estado
     */
    public List<PreinscripcionEntity> buscarPorEstado(com.dominio.Estado estado) {
        String jpql = "SELECT p FROM preinscripcion p " +
                     "WHERE p.estado = :estado " +
                     "ORDER BY p.fechaRegistro ASC";
        
        return entityManager.createQuery(jpql, PreinscripcionEntity.class)
            .setParameter("estado", estado)
            .getResultList();
    }

    /**
     * Busca preinscripciones pendientes
     */
    public List<PreinscripcionEntity> buscarPendientes() {
        return buscarPorEstado(com.dominio.Estado.Pendiente);
    }

    /**
     * Busca preinscripciones por acudiente
     */
    public List<PreinscripcionEntity> buscarPorAcudiente(Integer idAcudiente) {
        String jpql = "SELECT p FROM preinscripcion p " +
                     "WHERE p.acudiente.idUsuario = :idAcudiente";
        
        return entityManager.createQuery(jpql, PreinscripcionEntity.class)
            .setParameter("idAcudiente", idAcudiente)
            .getResultList();
    }

    /**
     * Cuenta preinscripciones por estado
     */
    public Long contarPorEstado(Estado estado) {
        String jpql = "SELECT COUNT(p) FROM preinscripcion p " +
                     "WHERE p.estado = :estado";
        
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("estado", estado)
            .getSingleResult();
    }

    public PreinscripcionEntity obtenerPreinscripcionPorEstudiante(EstudianteEntity estudiante) {
        try {
            String jpql = "SELECT p FROM preinscripcion p JOIN p.estudiantes e WHERE e.idEstudiante = :idEstudiante";
            return entityManager.createQuery(jpql, PreinscripcionEntity.class)
                .setParameter("idEstudiante", estudiante.getIdEstudiante())
                .setMaxResults(1)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}