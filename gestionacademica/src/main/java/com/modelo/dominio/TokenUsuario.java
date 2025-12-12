package com.modelo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Random;

@Entity(name = "token_usuario")
public class TokenUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    public TokenUsuario(Integer idToken, String nombreUsuario, String contrasena, Rol rol) {
        this.idToken = idToken;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public TokenUsuario() {
    }

    // ============================================
    // MÉTODOS DE NEGOCIO (LÓGICA DE DOMINIO)
    // ============================================

    public static TokenUsuario generarTokenDesdeUsuario(
            String primerNombre, 
            String segundoNombre,
            String primerApellido, 
            String segundoApellido,
            Rol rol) {
        
        // Validar campos obligatorios
        if (primerNombre == null || primerNombre.isEmpty()) {
            throw new IllegalArgumentException("El primer nombre es obligatorio para generar el token");
        }
        if (primerApellido == null || primerApellido.isEmpty()) {
            throw new IllegalArgumentException("El primer apellido es obligatorio para generar el token");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio para generar el token");
        }
        
        // Construir nombre de usuario según reglas de negocio
        String nombreUsuario = construirNombreUsuario(
            primerNombre, segundoNombre, primerApellido, segundoApellido);
        
        // Generar contraseña aleatoria
        String contrasena = generarContrasenaAleatoria();
        
        // Crear y retornar el token
        TokenUsuario token = new TokenUsuario();
        token.setNombreUsuario(nombreUsuario);
        token.setContrasena(contrasena);
        token.setRol(rol);
        
        return token;
    }
    
    /**
     * Construye el nombre de usuario según las reglas de negocio:
     * - Primera letra del primer nombre
     * - Primera letra del segundo nombre (si existe)
     * - Primer apellido completo (sin espacios)
     * - Primera letra del segundo apellido (si existe)
     * - Todo en minúsculas y sin tildes
     */
    private static String construirNombreUsuario(
            String primerNombre,
            String segundoNombre, 
            String primerApellido,
            String segundoApellido) {
        
        StringBuilder nombreUsuarioBuilder = new StringBuilder();
        
        // Primera letra del primer nombre
        if (!primerNombre.isEmpty()) {
            nombreUsuarioBuilder.append(primerNombre.charAt(0));
        }
        
        // Primera letra del segundo nombre (si existe)
        if (segundoNombre != null && !segundoNombre.isEmpty()) {
            nombreUsuarioBuilder.append(segundoNombre.charAt(0));
        }
        
        // Apellido completo (sin espacios)
        nombreUsuarioBuilder.append(primerApellido.toLowerCase().replaceAll("\\s+", ""));
        
        // Primera letra del segundo apellido (si existe)
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            nombreUsuarioBuilder.append(segundoApellido.toLowerCase().charAt(0));
        }
        
        // Normalizar (eliminar tildes y caracteres especiales)
        return normalizarTexto(nombreUsuarioBuilder.toString());
    }
    
    /**
     * Normaliza el texto eliminando tildes y caracteres especiales
     * Mantiene solo letras y números
     */
    private static String normalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        
        // Convertir a minúsculas
        String normalizado = texto.toLowerCase();
        
        // Reemplazar vocales con tildes
        normalizado = normalizado
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
            .replace('ñ', 'n');
        
        // Eliminar caracteres especiales, mantener solo letras y números
        normalizado = normalizado.replaceAll("[^a-z0-9]", "");
        
        return normalizado;
    }
    
    /**
     * Genera una contraseña aleatoria de 8 caracteres
     * Incluye mayúsculas, minúsculas, números y caracteres especiales
     */
    private static String generarContrasenaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        
        for (int i = 0; i < 8; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        
        return sb.toString();
    }

    // ============================================
    // VALIDACIONES DE DOMINIO
    // ============================================
    
    /**
     * Verifica las credenciales del usuario
     */
    public boolean verificarCredenciales(String contrasenaPrueba) {
        if (contrasenaPrueba == null || contrasenaPrueba.isEmpty()) {
            return false;
        }
        
        boolean credencialesCorrectas = this.contrasena.equals(contrasenaPrueba);
        return credencialesCorrectas;
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================
    
    public Integer getIdToken() { return idToken; }
    public void setIdToken(Integer idToken) { this.idToken = idToken; }
    
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}