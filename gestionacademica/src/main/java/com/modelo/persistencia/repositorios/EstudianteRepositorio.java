package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import com.modelo.dominio.Estado;
import com.modelo.dominio.Estudiante;

/**
 * Repositorio para la entidad Estudiante
 * Responsabilidad: Gestionar persistencia de estudiantes
 */
public class EstudianteRepositorio extends RepositorioGenerico<Estudiante> {
    private final EntityManager entityManager;

    public EstudianteRepositorio(EntityManager entityManager) {
        super(entityManager, Estudiante.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca un estudiante por NUIP
     */
    public Optional<Estudiante> buscarPorNuip(String nuip) {
        String jpql = "SELECT e FROM estudiante e WHERE e.nuip = :nuip";
        TypedQuery<Estudiante> query = entityManager.createQuery(jpql, Estudiante.class);
        query.setParameter("nuip", nuip);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca estudiantes por estado
     */
    public List<Estudiante> buscarPorEstado(Estado estado) {
        String jpql = "SELECT e FROM estudiante e WHERE e.estado = :estado";
        return entityManager.createQuery(jpql, Estudiante.class)
            .setParameter("estado", estado)
            .getResultList();
    }

    /**
     * Busca estudiantes por grupo
     */
    public List<Estudiante> buscarPorGrupo(Integer idGrupo) {
        String jpql = "SELECT e FROM estudiante e WHERE e.grupo.idGrupo = :idGrupo";
        return entityManager.createQuery(jpql, Estudiante.class)
            .setParameter("idGrupo", idGrupo)
            .getResultList();
    }

    /**
     * Busca un estudiante por ID con todas sus relaciones cargadas
     */
    public Optional<Estudiante> buscarPorIdConRelaciones(Integer idEstudiante) {
        String jpql = "SELECT e FROM estudiante e " +
                     "LEFT JOIN FETCH e.acudiente " +
                     "LEFT JOIN FETCH e.grupo " +
                     "LEFT JOIN FETCH e.gradoAspira " +
                     "LEFT JOIN FETCH e.observador " +
                     "WHERE e.idEstudiante = :idEstudiante";
        
        TypedQuery<Estudiante> query = entityManager.createQuery(jpql, Estudiante.class);
        query.setParameter("idEstudiante", idEstudiante);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Verifica si existe un estudiante con el NUIP dado
     */
    public boolean existePorNuip(String nuip) {
        String jpql = "SELECT COUNT(e) FROM estudiante e WHERE e.nuip = :nuip";
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("nuip", nuip)
            .getSingleResult();
        
        return count > 0;
    }
}