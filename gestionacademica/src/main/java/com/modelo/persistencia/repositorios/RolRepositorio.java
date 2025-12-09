package com.modelo.persistencia.repositorios;

import java.util.Optional;

import com.modelo.dominio.Rol;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class RolRepositorio extends RepositorioGenerico<Rol>{
    private final EntityManager entityManager;

    public RolRepositorio(EntityManager entityManager) {
        super(entityManager, Rol.class);
        this.entityManager = entityManager;
    }

    public Optional<Rol> buscarPorNombreRol(String nombreRol) {
        String jpql = "SELECT t FROM Rol t WHERE t.nombre = :nombre";
        TypedQuery<Rol> query = entityManager.createQuery(jpql, Rol.class);
        query.setParameter("nombre", nombreRol);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
