package com.servicios;

import java.util.Optional;

import com.dominio.TokenUsuario;
import com.persistencia.entidades.TokenUsuarioEntity;
import com.persistencia.mappers.DominioAPersistenciaMapper;
import com.persistencia.repositorios.TokenUsuarioRepositorio;

public class AutenticacionService {
    private final TokenUsuarioRepositorio tokenRepositorio;
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;

    public AutenticacionService(TokenUsuarioRepositorio tokenRepositorio) {
        this.tokenRepositorio = tokenRepositorio;
    }

    public Optional<TokenUsuario> iniciarSesion(String nombreUsuario, String contrasena) {
        // Verificar intentos (esto SÍ es lógica de negocio)
        if (intentosFallidos >= MAX_INTENTOS) {
            throw new IllegalStateException("Límite de intentos alcanzado. La opción de inicio de sesión está temporalmente inhabilitada");
        }

        try {
            // Buscar token en BD
            Optional<TokenUsuarioEntity> tokenEntityOpt = 
                tokenRepositorio.buscarPorNombreUsuario(nombreUsuario);
            
            // Usuario no existe
            if (tokenEntityOpt.isEmpty()) {
                intentosFallidos++;
                return Optional.empty();
            }

            // Convertir a dominio
            TokenUsuario token = DominioAPersistenciaMapper.toDomain(tokenEntityOpt.get());

            // Verificar credenciales
            if (!token.verificarCredenciales(contrasena)) {
                intentosFallidos++;
                return Optional.empty();
            }

            intentosFallidos = 0;
            return Optional.of(token);

        } catch (Exception e) {
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }
    
    public int getIntentosRestantes() {
        return MAX_INTENTOS - intentosFallidos;
    }
}