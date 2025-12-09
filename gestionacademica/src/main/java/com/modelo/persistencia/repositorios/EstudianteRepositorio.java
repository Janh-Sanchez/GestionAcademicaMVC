package com.modelo.persistencia.repositorios;

import com.modelo.dominio.Estudiante;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class EstudianteRepositorio extends RepositorioGenerico<Estudiante>{
    private final EntityManager entityManager;

    public EstudianteRepositorio(EntityManager entityManager) {
        super(entityManager, Estudiante.class);
        this.entityManager = entityManager;
    }

    public boolean existePorNuip(String nuip) {
        try {
            String jpql = "SELECT 1 FROM estudiante e WHERE e.nuip = :nuip";
            entityManager.createQuery(jpql, Integer.class)
                            .setParameter("nuip", nuip)
                            .setMaxResults(1)
                            .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
}
