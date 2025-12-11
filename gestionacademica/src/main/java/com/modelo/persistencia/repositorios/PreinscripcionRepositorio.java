package com.modelo.persistencia.repositorios;

import com.modelo.dominio.Estado;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Preinscripcion;

import jakarta.persistence.EntityManager;

import java.util.List;

public class PreinscripcionRepositorio extends RepositorioGenerico<Preinscripcion> {
    private final EntityManager entityManager;

    public PreinscripcionRepositorio(EntityManager entityManager) {
        super(entityManager, Preinscripcion.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca preinscripciones por estado
     */
    public List<Preinscripcion> buscarPorEstado(Estado estado) {
        String jpql = "SELECT p FROM preinscripcion p " +
                     "WHERE p.estado = :estado " +
                     "ORDER BY p.fechaRegistro ASC";
        
        return entityManager.createQuery(jpql, Preinscripcion.class)
            .setParameter("estado", estado)
            .getResultList();
    }

    /**
     * Busca preinscripciones pendientes
     */
    public List<Preinscripcion> buscarPendientes() {
        return buscarPorEstado(Estado.Pendiente);
    }

    /**
     * Busca preinscripciones por acudiente
     */
    public List<Preinscripcion> buscarPorAcudiente(Integer idAcudiente) {
        String jpql = "SELECT p FROM preinscripcion p " +
                     "WHERE p.acudiente.idUsuario = :idAcudiente";
        
        return entityManager.createQuery(jpql, Preinscripcion.class)
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

    public Preinscripcion obtenerPreinscripcionPorEstudiante(Estudiante estudiante) {
        try {
            String jpql = "SELECT p FROM preinscripcion p JOIN p.estudiantes e WHERE e.idEstudiante = :idEstudiante";
            return entityManager.createQuery(jpql, Preinscripcion.class)
                .setParameter("idEstudiante", estudiante.getIdEstudiante())
                .setMaxResults(1)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // En PreinscripcionRepositorio.java agregar:
    public List<Preinscripcion> buscarTodos() {
        String jpql = "SELECT p FROM preinscripcion p " +
                    "ORDER BY p.fechaRegistro ASC";
        
        return entityManager.createQuery(jpql, Preinscripcion.class)
            .getResultList();
    }
}