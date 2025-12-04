package com.servicios;

import com.dominio.*;
import com.persistencia.entidades.*;
import com.persistencia.mappers.DominioAPersistenciaMapper;
import com.persistencia.repositorios.RolRepositorio;
import com.persistencia.repositorios.TokenUsuarioRepositorio;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.Random;

/**
 * Servicio especializado para la generación y gestión de tokens de usuario
 * Responsabilidad única: Crear, asignar y gestionar tokens de autenticación
 */
public class TokenService {
    private final EntityManager entityManager;
    private final TokenUsuarioRepositorio tokenUsuarioRepo;
    private final RolRepositorio rolRepo;
    private final EmailService emailService;
    
    // Configuración de generación de tokens
    private static final String CARACTERES_CONTRASENA = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
    private static final int LONGITUD_CONTRASENA = 8;
    private final Random random;
    
    public TokenService(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.tokenUsuarioRepo = new TokenUsuarioRepositorio(entityManager);
        this.rolRepo = new RolRepositorio(entityManager);
        this.emailService = new EmailService();
        this.random = new Random();
    }
    
    /**
     * Genera un token completo para un usuario (en dominio) y un rol específico
     * Versión mejorada que no requiere entidad persistida
     */
    public TokenUsuario generarTokenParaUsuario(Usuario usuario, String nombreRol) {
        validarDatosUsuario(usuario);
        
        // Buscar el rol en la base de datos
        Optional<RolEntity> rolOpt = rolRepo.buscarPorNombreRol(nombreRol.toLowerCase());
        if (rolOpt.isEmpty()) {
            throw new IllegalArgumentException("Rol no encontrado: " + nombreRol);
        }
        
        // Crear el token con datos generados
        TokenUsuario token = new TokenUsuario();
        token.setNombreUsuario(generarNombreUsuario(usuario));
        token.setContrasena(generarContrasenaAleatoria());
        token.setRol(DominioAPersistenciaMapper.toDomain(rolOpt.get()));
        usuario.setTokenAccess(token);
        
        return token;
    }

    /**
     * Genera un TokenUsuarioEntity usando datos básicos del usuario (sin persistir)
     * Útil para casos donde el usuario aún no ha sido persistido
     */
    public TokenUsuarioEntity generarTokenEntityNoPersistido(Usuario usuario, String nombreRol) {
        validarDatosUsuario(usuario);
        
        // Buscar el rol en la base de datos
        Optional<RolEntity> rolOpt = rolRepo.buscarPorNombreRol(nombreRol.toLowerCase());
        if (rolOpt.isEmpty()) {
            throw new IllegalArgumentException("Rol no encontrado: " + nombreRol);
        }
        
        // Crear el token entity con datos generados
        TokenUsuarioEntity tokenEntity = new TokenUsuarioEntity();
        tokenEntity.setNombreUsuario(generarNombreUsuario(usuario));
        tokenEntity.setContrasena(generarContrasenaAleatoria());
        tokenEntity.setRol(rolOpt.get());
        
        return tokenEntity;
    }

    public TokenUsuarioEntity generarYPersistirToken(UsuarioEntity usuarioEntity, String nombreRol) {
        validarDatosUsuarioEntity(usuarioEntity);
        
        // Buscar el rol en la base de datos
        Optional<RolEntity> rolOpt = rolRepo.buscarPorNombreRol(nombreRol.toLowerCase());
        if (rolOpt.isEmpty()) {
            throw new IllegalArgumentException("Rol no encontrado: " + nombreRol);
        }
        
        // Generar nombre de usuario directamente desde la entidad
        String nombreUsuario = generarNombreUsuarioDesdeEntidad(usuarioEntity);
        String contrasena = generarContrasenaAleatoria();
        
        // Crear y guardar token
        TokenUsuarioEntity tokenEntity = new TokenUsuarioEntity();
        tokenEntity.setNombreUsuario(nombreUsuario);
        tokenEntity.setContrasena(contrasena);
        tokenEntity.setRol(rolOpt.get());
        
        TokenUsuarioEntity tokenGuardado = tokenUsuarioRepo.guardar(tokenEntity);
        
        usuarioEntity.setTokenAccess(tokenGuardado);

        return tokenGuardado;
    }

    /**
     * Genera token específicamente para acudientes (caso especial de aprobación)
     * @param acudienteEntity Acudiente que necesita token
     * @return TokenUsuarioEntity persistido
     */
    public TokenUsuarioEntity generarTokenParaAcudiente(AcudienteEntity acudienteEntity) {
        return generarYPersistirToken(acudienteEntity, "acudiente");
    }
    
    /**
     * Valida si un token tiene credenciales válidas
     * @param tokenEntity Token a validar
     * @param contrasenaPrueba Contraseña a verificar
     * @return true si las credenciales son válidas
     */
    public boolean validarCredenciales(TokenUsuarioEntity tokenEntity, String contrasenaPrueba) {
        if (tokenEntity == null || contrasenaPrueba == null) {
            return false;
        }
        
        // Convertir a dominio para usar la lógica de validación
        TokenUsuario token = DominioAPersistenciaMapper.toDomain(tokenEntity);
        return token.verificarCredenciales(contrasenaPrueba);
    }
    
    /**
     * Envía las credenciales por email al usuario
     * @param usuarioEntity Usuario al que enviar las credenciales
     * @param tokenEntity Token con las credenciales
     */
    public void enviarCredencialesPorEmail(UsuarioEntity usuarioEntity, TokenUsuarioEntity tokenEntity) {
        if (usuarioEntity == null || tokenEntity == null) {
            return;
        }
        
        String correo = usuarioEntity.getCorreoElectronico();
        if (correo == null || correo.trim().isEmpty()) {
            System.err.println("No se puede enviar email: usuario sin correo electrónico");
            return;
        }
        
        String nombreCompleto = construirNombreCompleto(
            usuarioEntity.getPrimerNombre(),
            usuarioEntity.getSegundoNombre(),
            usuarioEntity.getPrimerApellido(),
            usuarioEntity.getSegundoApellido()
        );
        
        // Convertir a dominio para el email
        TokenUsuario token = DominioAPersistenciaMapper.toDomain(tokenEntity);
        
        emailService.enviarCredenciales(correo, token, nombreCompleto);
    }
    
    /**
     * Genera un nombre de usuario basado en los datos del usuario
     */
    private String generarNombreUsuario(Usuario usuario) {
        if (usuario.getPrimerNombre() == null || usuario.getPrimerApellido() == null) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios para generar usuario");
        }
        
        StringBuilder nombreUsuarioBuilder = new StringBuilder();
        
        // Primera letra del primer nombre
        if (!usuario.getPrimerNombre().isEmpty()) {
            nombreUsuarioBuilder.append(usuario.getPrimerNombre().charAt(0));
        }
        
        // Primera letra del segundo nombre (si existe)
        if (usuario.getSegundoNombre() != null && !usuario.getSegundoNombre().isEmpty()) {
            nombreUsuarioBuilder.append(usuario.getSegundoNombre().charAt(0));
        }
        
        // Apellido completo en minúsculas, sin espacios
        nombreUsuarioBuilder.append(usuario.getPrimerApellido().toLowerCase().replaceAll("\\s+", ""));
        
        // Primera letra del segundo apellido (si existe)
        if (usuario.getSegundoApellido() != null && !usuario.getSegundoApellido().isEmpty()) {
            nombreUsuarioBuilder.append(usuario.getSegundoApellido().toLowerCase().charAt(0));
        }
        
        // Normalizar texto (quitar tildes y caracteres especiales)
        return normalizarTexto(nombreUsuarioBuilder.toString());
    }
    
    /**
     * Genera una contraseña aleatoria segura
     */
    private String generarContrasenaAleatoria() {
        StringBuilder sb = new StringBuilder(LONGITUD_CONTRASENA);
        
        for (int i = 0; i < LONGITUD_CONTRASENA; i++) {
            int indice = random.nextInt(CARACTERES_CONTRASENA.length());
            sb.append(CARACTERES_CONTRASENA.charAt(indice));
        }
        
        return sb.toString();
    }
    
    /**
     * Normaliza texto quitando tildes y caracteres especiales
     */
    private String normalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        
        String normalizado = texto.toLowerCase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
            .replace('ñ', 'n');
        
        // Mantener solo letras y números
        return normalizado.replaceAll("[^a-z0-9]", "");
    }
    
    /**
     * Construye el nombre completo del usuario
     */
    private String construirNombreCompleto(String primer, String segundo, 
                                         String primerAp, String segundoAp) {
        StringBuilder nombre = new StringBuilder();
        
        if (primer != null && !primer.trim().isEmpty()) {
            nombre.append(primer.trim());
        }
        
        if (segundo != null && !segundo.trim().isEmpty()) {
            nombre.append(" ").append(segundo.trim());
        }
        
        if (primerAp != null && !primerAp.trim().isEmpty()) {
            nombre.append(" ").append(primerAp.trim());
        }
        
        if (segundoAp != null && !segundoAp.trim().isEmpty()) {
            nombre.append(" ").append(segundoAp.trim());
        }
        
        return nombre.toString().trim();
    }
    
    /**
     * Valida los datos mínimos del usuario para generar token
     */
    private void validarDatosUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        
        if (usuario.getPrimerNombre() == null || usuario.getPrimerNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Primer nombre es obligatorio");
        }
        
        if (usuario.getPrimerApellido() == null || usuario.getPrimerApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("Primer apellido es obligatorio");
        }
    }
    
    /**
     * Genera nombre de usuario directamente desde la entidad
     */
    private String generarNombreUsuarioDesdeEntidad(UsuarioEntity usuarioEntity) {
        if (usuarioEntity.getPrimerNombre() == null || usuarioEntity.getPrimerApellido() == null) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios");
        }
        
        StringBuilder nombreUsuarioBuilder = new StringBuilder();
        
        // Primera letra del primer nombre
        if (!usuarioEntity.getPrimerNombre().isEmpty()) {
            nombreUsuarioBuilder.append(usuarioEntity.getPrimerNombre().charAt(0));
        }
        
        // Primera letra del segundo nombre (si existe)
        if (usuarioEntity.getSegundoNombre() != null && !usuarioEntity.getSegundoNombre().isEmpty()) {
            nombreUsuarioBuilder.append(usuarioEntity.getSegundoNombre().charAt(0));
        }
        
        // Apellido completo en minúsculas, sin espacios
        nombreUsuarioBuilder.append(usuarioEntity.getPrimerApellido().toLowerCase().replaceAll("\\s+", ""));
        
        // Primera letra del segundo apellido (si existe)
        if (usuarioEntity.getSegundoApellido() != null && !usuarioEntity.getSegundoApellido().isEmpty()) {
            nombreUsuarioBuilder.append(usuarioEntity.getSegundoApellido().toLowerCase().charAt(0));
        }
        
        return normalizarTexto(nombreUsuarioBuilder.toString());
    }
    
    /**
     * Valida los datos mínimos de la entidad usuario
     */
    private void validarDatosUsuarioEntity(UsuarioEntity usuarioEntity) {
        if (usuarioEntity == null) {
            throw new IllegalArgumentException("UsuarioEntity no puede ser nulo");
        }
        
        if (usuarioEntity.getPrimerNombre() == null || usuarioEntity.getPrimerNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Primer nombre es obligatorio");
        }
        
        if (usuarioEntity.getPrimerApellido() == null || usuarioEntity.getPrimerApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("Primer apellido es obligatorio");
        }
    }
    
    /**
     * Verifica si un usuario ya tiene token asignado
     */
    public boolean tieneTokenAsignado(UsuarioEntity usuarioEntity) {
        return usuarioEntity != null && usuarioEntity.getTokenAccess() != null;
    }
}