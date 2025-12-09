package com.controlador;

import java.util.Optional;
import com.aplicacion.JPAUtil;
import com.modelo.dominio.TokenUsuario;
import com.modelo.dominio.Usuario;
import com.modelo.persistencia.repositorios.TokenUsuarioRepositorio;
import com.modelo.persistencia.repositorios.UsuarioRepositorio;

public class LoginController {
    private UsuarioRepositorio usuarioRepo;
    private TokenUsuarioRepositorio tokenRepo;
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;
    
    public LoginController() {
        // Crear EntityManager dentro del controlador
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.usuarioRepo = new UsuarioRepositorio(em);
        this.tokenRepo = new TokenUsuarioRepositorio(em);
    }
    
    public Usuario autenticar(String usuario, String contrasena) {
        // Validar límite de intentos
        if (intentosFallidos >= MAX_INTENTOS) {
            throw new IllegalStateException(
                "Límite de intentos alcanzado. La opción de inicio de sesión está temporalmente inhabilitada"
            );
        }
        
        try {
            // Buscar token en BD
            Optional<TokenUsuario> tokenOpt = tokenRepo.buscarPorNombreUsuario(usuario);
            
            if (tokenOpt.isEmpty()) {
                intentosFallidos++;
                return null;
            }
            
            TokenUsuario token = tokenOpt.get();
            
            // Verificar credenciales
            if (!token.verificarCredenciales(contrasena)) {
                intentosFallidos++;
                return null;
            }
            
            // Buscar usuario por token
            Optional<Usuario> usuarioOpt = usuarioRepo.buscarPorToken(token.getIdToken());
            
            if (usuarioOpt.isEmpty()) {
                intentosFallidos++;
                return null;
            }
            
            // Éxito - resetear intentos
            intentosFallidos = 0;
            return usuarioOpt.get();
            
        } catch (Exception e) {
            // Error de base de datos
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
    }
    
    public int getIntentosFallidos() {
        return intentosFallidos;
    }
    
    public int getIntentosRestantes() {
        return MAX_INTENTOS - intentosFallidos;
    }
    
    public boolean estaBloqueado() {
        return intentosFallidos >= MAX_INTENTOS;
    }
    
    public void resetearIntentos() {
        intentosFallidos = 0;
    }
}