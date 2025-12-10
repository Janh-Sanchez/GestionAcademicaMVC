package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import com.modelo.dominio.Grupo;

public class GrupoRepositorio extends RepositorioGenerico<Grupo> {
    private final EntityManager entityManager;

    public GrupoRepositorio(EntityManager entityManager) {
        super(entityManager, Grupo.class);
        this.entityManager = entityManager;
    }

    /**
     * Busca un grupo por su nombre
     */
    public Optional<Grupo> buscarPorNombre(String nombreGrupo) {
        String jpql = "SELECT g FROM grupo g WHERE g.nombreGrupo = :nombreGrupo";
        TypedQuery<Grupo> query = entityManager.createQuery(jpql, Grupo.class);
        query.setParameter("nombreGrupo", nombreGrupo);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca todos los grupos de un grado específico
     */
    public List<Grupo> buscarPorGrado(Integer idGrado) {
        String jpql = "SELECT g FROM grupo g WHERE g.grado.idGrado = :idGrado";
        return entityManager.createQuery(jpql, Grupo.class)
            .setParameter("idGrado", idGrado)
            .getResultList();
    }

    /**
     * Busca grupos activos de un grado específico
     */
    public List<Grupo> buscarActivosPorGrado(Integer idGrado) {
        String jpql = "SELECT g FROM grupo g " +
                     "WHERE g.grado.idGrado = :idGrado AND g.estado = true";
        return entityManager.createQuery(jpql, Grupo.class)
            .setParameter("idGrado", idGrado)
            .getResultList();
    }

    /**
     * Busca grupos activos de un grado ordenados por cantidad de estudiantes
     */
    public List<Grupo> buscarActivosPorGradoOrdenadosPorEstudiantes(Integer idGrado) {
        String jpql = "SELECT g FROM grupo g " +
                     "WHERE g.grado.idGrado = :idGrado " +
                     "AND g.estado = true " +
                     "ORDER BY SIZE(g.estudiantes) ASC";
        
        return entityManager.createQuery(jpql, Grupo.class)
            .setParameter("idGrado", idGrado)
            .getResultList();
    }

    /**
     * Cuenta la cantidad de grupos existentes para un grado
     */
    public Long contarGruposPorGrado(Integer idGrado) {
        String jpql = "SELECT COUNT(g) FROM grupo g WHERE g.grado.idGrado = :idGrado";
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("idGrado", idGrado)
            .getSingleResult();
    }
}