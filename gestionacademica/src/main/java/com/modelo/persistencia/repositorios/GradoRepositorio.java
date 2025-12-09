package com.modelo.persistencia.repositorios;

import java.util.Optional;

import com.modelo.dominio.Grado;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class GradoRepositorio extends RepositorioGenerico<Grado>{
    private final EntityManager entityManager;

    public GradoRepositorio(EntityManager entityManager) {
        super(entityManager, Grado.class);
        this.entityManager = entityManager;
    }

    public Optional<Grado> buscarPornombreGrado(String nombreGrado) {
        String jpql = "SELECT t FROM grado t WHERE t.nombreGrado = :nombreGrado";
        TypedQuery<Grado> query = entityManager.createQuery(jpql, Grado.class);
        query.setParameter("nombreGrado", nombreGrado);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
