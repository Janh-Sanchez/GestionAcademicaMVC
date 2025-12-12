package com.controlador;

import javax.swing.SwingUtilities;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.TokenUsuarioRepositorio;
import com.modelo.persistencia.repositorios.UsuarioRepositorio;
import com.vista.presentacion.*;
import com.vista.presentacion.hojavida.DiligenciarHojaVidaDialog;

import jakarta.persistence.EntityManager;

/**
 * Controlador para el login
 * Responsabilidad: Gestionar la autenticación y navegación según rol
 */
public class LoginController {
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;
    private final EntityManager entityManager;
    private final TokenUsuarioRepositorio tokenUsuarioRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public LoginController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.tokenUsuarioRepositorio = new TokenUsuarioRepositorio(entityManager);
        this.usuarioRepositorio = new UsuarioRepositorio(entityManager);
    }

    /**
     * Autentica un usuario con validaciones previas
     */
    public ResultadoOperacion autenticarConValidacion(String nombreUsuario, String contrasena) {
        // Validar campos vacíos
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return ResultadoOperacion.errorValidacion("nombreUsuario", "El nombre de usuario es obligatorio");
        }
        
        if (contrasena == null || contrasena.isEmpty()) {
            return ResultadoOperacion.errorValidacion("contrasena", "La contraseña es obligatoria");
        }
        
        // Verificar si está bloqueado
        if (estaBloqueado()) {
            return ResultadoOperacion.error("Cuenta bloqueada por múltiples intentos fallidos");
        }
        
        // Intentar autenticar
        ResultadoOperacion resultado = autenticar(nombreUsuario, contrasena);
        
        if (!resultado.isExitoso()) {
            intentosFallidos++;
            
            if (estaBloqueado()) {
                return ResultadoOperacion.error("Cuenta bloqueada. Ha excedido el límite de intentos.");
            } else {
                int intentosRestantes = MAX_INTENTOS - intentosFallidos;
                return ResultadoOperacion.error("Credenciales incorrectas. Intentos restantes: " + intentosRestantes);
            }
        }
        
        // Login exitoso - resetear intentos
        intentosFallidos = 0;
        return resultado;
    }

    /**
     * Autentica un usuario en el sistema
     */
    private ResultadoOperacion autenticar(String nombreUsuario, String contrasena) {
        try {
            // Buscar usuario por nombre
            var tokenUsuarioOpt = tokenUsuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);
            
            if (tokenUsuarioOpt.isEmpty()) {
                return ResultadoOperacion.error("Usuario no encontrado");
            }

            Usuario usuario = usuarioRepositorio.buscarPorToken(tokenUsuarioOpt.get().getIdToken()).get();
            
            boolean credencialesCorrectas = usuario.getTokenAccess().verificarCredenciales(contrasena);
            
            if (!credencialesCorrectas) {
                return ResultadoOperacion.error("Contraseña incorrecta");
            }
            
            // Autenticación exitosa
            return ResultadoOperacion.exitoConDatos("Login exitoso", usuario);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error en el sistema: " + e.getMessage());
        }
    }

    /**
     * Navega a la pantalla correspondiente según el rol del usuario autenticado
     * IMPORTANTE: Maneja la validación de hojas de vida para acudientes
     */
    public void navegarSegunRol(Usuario usuario) {
        String nombreRol = usuario.getTokenAccess().getRol().getNombre();
        
        SwingUtilities.invokeLater(() -> {
            switch (nombreRol) {
                case "administrador":
                    AdministradorFrame administradorFrame = new AdministradorFrame((Administrador) usuario);
                    administradorFrame.setVisible(true);
                    break;
                    
                case "acudiente":
                    manejarLoginAcudiente((Acudiente) usuario);
                    break;
                    
                case "profesor":
                    ProfesorFrame profesorFrame = new ProfesorFrame((Profesor) usuario);
                    profesorFrame.setVisible(true);
                    break;
                    
                case "directivo":
                    DirectivoFrame directivoFrame = new DirectivoFrame((Directivo) usuario);
                    directivoFrame.setVisible(true);
                    break;
                    
                default:
                    System.err.println("Rol no reconocido: " + nombreRol);
            }
        });
    }

    /**
     * Maneja el login del acudiente verificando si debe completar hojas de vida
     */
    private void manejarLoginAcudiente(Acudiente acudiente) {
        // Verificar si tiene estudiantes y si las hojas de vida están completas
        var hojaVidaController = new GestionHojaVidaController();
        ResultadoOperacion resultado = hojaVidaController.verificarHojasVidaCompletas(acudiente);
        
        if (resultado.isExitoso()) {
            Boolean todasCompletas = (Boolean) resultado.getDatos();
            
            if (!todasCompletas) {
                // Mostrar diálogo OBLIGATORIO de hojas de vida
                AcudienteFrame frameTemp = new AcudienteFrame(acudiente);
                
                DiligenciarHojaVidaDialog dialogo = new DiligenciarHojaVidaDialog(
                    frameTemp, acudiente, hojaVidaController);
                dialogo.setVisible(true);
                
                // Verificar si completó las hojas de vida
                if (dialogo.seCompletaronTodasLasHojas()) {
                    // Permitir acceso al frame
                    AcudienteFrame acudienteFrame = new AcudienteFrame(acudiente);
                    acudienteFrame.setVisible(true);
                } else {
                    // Volver al login
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                }
                
                frameTemp.dispose();
                return;
            }
        }
        
        // Hojas completas o sin estudiantes - acceso normal
        AcudienteFrame acudienteFrame = new AcudienteFrame(acudiente);
        acudienteFrame.setVisible(true);
    }

    /**
     * Verifica si la cuenta está bloqueada
     */
    public boolean estaBloqueado() {
        return intentosFallidos >= MAX_INTENTOS;
    }

    /**
     * Resetea los intentos fallidos (útil para testing)
     */
    public void resetearIntentos() {
        intentosFallidos = 0;
    }
}