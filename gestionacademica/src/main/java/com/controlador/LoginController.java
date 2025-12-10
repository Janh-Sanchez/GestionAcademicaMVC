package com.controlador;

import java.util.Optional;

import javax.swing.JFrame;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.Acudiente;
import com.modelo.dominio.Administrador;
import com.modelo.dominio.Directivo;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;
import com.modelo.dominio.TokenUsuario;
import com.modelo.dominio.Usuario;
import com.modelo.persistencia.repositorios.TokenUsuarioRepositorio;
import com.modelo.persistencia.repositorios.UsuarioRepositorio;
import com.vista.presentacion.AcudienteFrame;
import com.vista.presentacion.AdministradorFrame;
import com.vista.presentacion.DirectivoFrame;
import com.vista.presentacion.PreinscripcionFrame;
import com.vista.presentacion.ProfesorFrame;

public class LoginController {
    private UsuarioRepositorio usuarioRepo;
    private TokenUsuarioRepositorio tokenRepo;
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;
    
    public LoginController() {
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.usuarioRepo = new UsuarioRepositorio(em);
        this.tokenRepo = new TokenUsuarioRepositorio(em);
    }

    public ResultadoOperacion autenticarConValidacion(String usuario, String contrasena) {
        // Primero validar los datos básicos
        if (usuario == null || usuario.trim().isEmpty()) {
            return ResultadoOperacion.errorValidacion(
                "nombreUsuario",
                "El nombre de usuario es obligatorio"
            );
        }
        
        if (contrasena == null || contrasena.isEmpty()) {
            return ResultadoOperacion.errorValidacion(
                "contrasena",
                "La contraseña es obligatoria"
            );
        }
        
        // Luego autenticar
        try {
            Usuario usuarioAutenticado = autenticar(usuario.trim(), contrasena);
            if (usuarioAutenticado != null) {
                return ResultadoOperacion.exitoConDatos("Autenticación exitosa", usuarioAutenticado);
            } else {
                String mensaje = estaBloqueado()
                    ? "Inicio de sesión bloqueado, inténtelo nuevamente mas tarde"
                    : String.format("Credenciales incorrectas. Intentos restantes: %d", getIntentosRestantes());
                return ResultadoOperacion.error(mensaje);
            }
        } catch (IllegalStateException e) {
            return ResultadoOperacion.error(e.getMessage());
        } catch (Exception e) {
            return ResultadoOperacion.error("Error en el sistema: " + e.getMessage());
        }
    }

    public Usuario autenticar(String usuario, String contrasena) {
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
            resetearIntentos();
            return usuarioOpt.get();
            
        } catch (Exception e) {
            // Error de base de datos
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
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

    public void navegarSegunRol(Usuario u) {
        JFrame next = null;

        if (u instanceof Administrador admin) {
            next = new AdministradorFrame(admin);
        }
        else if(u instanceof Profesor p){
            next = new ProfesorFrame(p);
        } 
        else if (u instanceof Directivo d) {
            next = new DirectivoFrame(d);
        } 
        else if (u instanceof Acudiente a) {
            next = new AcudienteFrame(a);
        }
        
        if (next != null) {
            next.setVisible(true);
        } else {
            throw new IllegalArgumentException("Tipo de usuario no soportado: " + 
                (u != null ? u.getClass().getSimpleName() : "null"));
        }
    }

    public void abrirFormularioPreinscripcion(JFrame padre){
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        PreinscripcionFrame preinscripcionDialog = new PreinscripcionFrame(padre, em);
        preinscripcionDialog.setModal(false);
        preinscripcionDialog.mostrarFormulario();
    }
}