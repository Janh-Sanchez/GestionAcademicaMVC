package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import com.modelo.dominio.Grupo;

/**
 * Repositorio para la entidad Grupo
 * Responsabilidad: Gestionar persistencia de grupos
 */
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
     * Busca todos los grupos ordenados por grado y nombre
     * Incluye información del grado y cantidad de estudiantes
     */

    public List<Grupo> buscarTodosOrdenadosConInfo() {
        String jpql = "SELECT DISTINCT g FROM grupo g " +
                    "LEFT JOIN FETCH g.grado grado " +
                    "LEFT JOIN FETCH g.profesor profesor " +
                    "LEFT JOIN FETCH g.estudiantes " +
                    "ORDER BY grado.nombreGrado, g.nombreGrupo";
        
        return entityManager.createQuery(jpql, Grupo.class)
            .setHint("org.hibernate.cacheMode", "IGNORE")
            .getResultList();
    }
    
    /**
     * Busca grupos listos (activos con >= 5 estudiantes) sin profesor asignado
     */
    public List<Grupo> buscarGruposListosSinProfesor() {
        String jpql = "SELECT g FROM grupo g " +
                     "WHERE g.estado = true " +
                     "AND g.profesor IS NULL " +
                     "AND SIZE(g.estudiantes) >= 5 " +
                     "ORDER BY g.grado.nombreGrado, g.nombreGrupo";
        
        return entityManager.createQuery(jpql, Grupo.class)
            .getResultList();
    }

    /**
     * Busca grupos en formación (< 5 estudiantes)
     */
    public List<Grupo> buscarGruposEnFormacion() {
        String jpql = "SELECT g FROM grupo g " +
                     "WHERE SIZE(g.estudiantes) < 5 " +
                     "ORDER BY g.grado.nombreGrado, g.nombreGrupo";
        
        return entityManager.createQuery(jpql, Grupo.class)
            .getResultList();
    }

    public List<Grupo> buscarGruposEnFormacionPorGrado(Integer idGrado) {
    String jpql = "SELECT g FROM grupo g " +
                 "WHERE g.grado.idGrado = :idGrado " +
                 "AND g.estado = false " +
                 "AND SIZE(g.estudiantes) < 5 " +
                 "ORDER BY SIZE(g.estudiantes) DESC";
    
    return entityManager.createQuery(jpql, Grupo.class)
        .setParameter("idGrado", idGrado)
        .getResultList();
    }

    /**
     * Busca grupos con menos de X estudiantes
     */
    public List<Grupo> buscarGruposConMenosDeXEstudiantes(Integer idGrado, int maxEstudiantes) {
        String jpql = "SELECT g FROM grupo g " +
                    "LEFT JOIN FETCH g.estudiantes " +
                    "WHERE g.grado.idGrado = :idGrado " +
                    "AND SIZE(g.estudiantes) < :maxEstudiantes " +
                    "ORDER BY SIZE(g.estudiantes) DESC";
        
        return entityManager.createQuery(jpql, Grupo.class)
            .setParameter("idGrado", idGrado)
            .setParameter("maxEstudiantes", maxEstudiantes)
            .getResultList();
    }

    /**
     * Busca un grupo por ID con todas sus relaciones cargadas
     */
    public Optional<Grupo> buscarPorIdConRelaciones(Integer idGrupo) {
        String jpql = "SELECT g FROM grupo g " +
                     "LEFT JOIN FETCH g.grado " +
                     "LEFT JOIN FETCH g.profesor " +
                     "LEFT JOIN FETCH g.estudiantes " +
                     "WHERE g.idGrupo = :idGrupo";
        
        TypedQuery<Grupo> query = entityManager.createQuery(jpql, Grupo.class);
        query.setParameter("idGrupo", idGrupo);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
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

    /**
     * Cuenta grupos listos sin profesor
     */
    public Long contarGruposListosSinProfesor() {
        String jpql = "SELECT COUNT(g) FROM grupo g " +
                     "WHERE g.estado = true " +
                     "AND g.profesor IS NULL " +
                     "AND SIZE(g.estudiantes) >= 5";
        
        return entityManager.createQuery(jpql, Long.class)
            .getSingleResult();
    }
}