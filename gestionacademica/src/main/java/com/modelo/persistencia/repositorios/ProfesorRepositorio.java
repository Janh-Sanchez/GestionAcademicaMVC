package com.modelo.persistencia.repositorios;

import com.modelo.dominio.Profesor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Profesor
 * Responsabilidad: Gestionar persistencia de profesores
 */
public class ProfesorRepositorio extends RepositorioGenerico<Profesor> {
    private final EntityManager entityManager;

    public ProfesorRepositorio(EntityManager entityManager) {
        super(entityManager, Profesor.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca todos los profesores que NO tienen grupo asignado
     * Estos profesores están disponibles para ser asignados a un grupo
     */
    public List<Profesor> buscarProfesoresSinGrupo() {
        String jpql = "SELECT p FROM profesor p " +
                     "WHERE p.grupoAsignado IS NULL " +
                     "ORDER BY p.primerNombre, p.primerApellido";
        
        return entityManager.createQuery(jpql, Profesor.class)
            .getResultList();
    }

    /**
     * Busca todos los profesores que tienen grupo asignado
     */
    public List<Profesor> buscarProfesoresConGrupo() {
        String jpql = "SELECT p FROM profesor p " +
                     "WHERE p.grupoAsignado IS NOT NULL " +
                     "ORDER BY p.primerNombre, p.primerApellido";
        
        return entityManager.createQuery(jpql, Profesor.class)
            .getResultList();
    }

    /**
     * Busca un profesor por ID con su grupo asignado (eager)
     */
    public Optional<Profesor> buscarPorIdConGrupo(Integer idProfesor) {
        String jpql = "SELECT p FROM profesor p " +
                     "LEFT JOIN FETCH p.grupoAsignado " +
                     "WHERE p.idUsuario = :idProfesor";
        
        TypedQuery<Profesor> query = entityManager.createQuery(jpql, Profesor.class);
        query.setParameter("idProfesor", idProfesor);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Verifica si un profesor tiene grupo asignado
     */
    public boolean tieneGrupoAsignado(Integer idProfesor) {
        String jpql = "SELECT COUNT(p) FROM profesor p " +
                     "WHERE p.idUsuario = :idProfesor " +
                     "AND p.grupoAsignado IS NOT NULL";
        
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("idProfesor", idProfesor)
            .getSingleResult();
        
        return count > 0;
    }

    /**
     * Cuenta la cantidad de profesores sin grupo asignado
     */
    public Long contarProfesoresSinGrupo() {
        String jpql = "SELECT COUNT(p) FROM profesor p WHERE p.grupoAsignado IS NULL";
        return entityManager.createQuery(jpql, Long.class)
            .getSingleResult();
    }

    /**
     * Busca todos los profesores ordenados alfabéticamente
     */
    public List<Profesor> buscarTodosOrdenados() {
        String jpql = "SELECT p FROM profesor p " +
                     "ORDER BY p.primerNombre, p.primerApellido";
        
        return entityManager.createQuery(jpql, Profesor.class)
            .getResultList();
    }
}
