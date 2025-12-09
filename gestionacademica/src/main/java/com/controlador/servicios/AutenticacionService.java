package com.controlador.servicios;

import java.util.Optional;
import javax.swing.JOptionPane;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.TokenUsuarioRepositorio;
import com.modelo.persistencia.repositorios.UsuarioRepositorio;

public class AutenticacionService {
    private final TokenUsuarioRepositorio tokenRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;

    public AutenticacionService(
            TokenUsuarioRepositorio tokenRepositorio,
            UsuarioRepositorio usuarioRepositorio) {
        
        this.tokenRepositorio = tokenRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        if (intentosFallidos >= MAX_INTENTOS) {
            throw new IllegalStateException("Límite de intentos alcanzado. La opción de inicio de sesión está temporalmente inhabilitada");
        }

        try {
            // 1. Buscar token en BD
            Optional<TokenUsuario> tokenEntityOpt = 
                tokenRepositorio.buscarPorNombreUsuario(nombreUsuario);
            
            if (tokenEntityOpt.isEmpty()) {
                intentosFallidos++;
                return Optional.empty();
            }

            // 2. Convertir a dominio y verificar credenciales
            TokenUsuario token = tokenEntityOpt.get();

            if (!token.verificarCredenciales(contrasena)) {
                intentosFallidos++;
                return Optional.empty();
            }

            // 3. Buscar usuario por token - JPA automáticamente carga la entidad específica
            Optional<Usuario> UsuarioOpt = usuarioRepositorio.buscarPorToken(token.getIdToken());
            
            if (UsuarioOpt.isEmpty()) {
                intentosFallidos++;
                return Optional.empty();
            }

            // 4. Convertir a dominio (JPA ya determina el tipo específico)
            Usuario usuario = convertirAUsuarioEspecifico(UsuarioOpt.get());

            if (usuario == null) {
                intentosFallidos++;
                return Optional.empty();
            }

            intentosFallidos = 0;
            return Optional.of(usuario);

        } catch (Exception e) {
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
    }

    private Usuario convertirAUsuarioEspecifico(Usuario Usuario) {
        if (Usuario == null) return null;
        
        switch (Usuario.getClass().getSimpleName()) {
            case "Administrador":
                return (Administrador) Usuario;
            case "Profesor":
                return (Profesor) Usuario;
            case "Directivo":
                return (Directivo) Usuario;
            case "Acudiente":
                return (Acudiente) Usuario;
            default:
                JOptionPane.showMessageDialog(null,
                    "Tipo de usuario no reconocido: " + Usuario.getClass().getSimpleName(),
                    "Error de autenticación",
                    JOptionPane.ERROR_MESSAGE);
                return null;
        }
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }
    
    public int getIntentosRestantes() {
        return MAX_INTENTOS - intentosFallidos;
    }
}