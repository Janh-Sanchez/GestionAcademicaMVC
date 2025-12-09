package com.modelo.persistencia.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

import com.modelo.dominio.TokenUsuario;

public class TokenUsuarioRepositorio extends RepositorioGenerico<TokenUsuario> {
    private final EntityManager entityManager;

    public TokenUsuarioRepositorio(EntityManager entityManager) {
        super(entityManager, TokenUsuario.class);
        this.entityManager = entityManager;
    }

    public Optional<TokenUsuario> buscarPorNombreUsuario(String nombreUsuario) {
        String jpql = "SELECT t FROM token_usuario t WHERE t.nombreUsuario = :nombreUsuario";
        TypedQuery<TokenUsuario> query = entityManager.createQuery(jpql, TokenUsuario.class);
        query.setParameter("nombreUsuario", nombreUsuario);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
