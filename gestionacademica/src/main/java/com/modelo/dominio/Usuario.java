package com.modelo.dominio;

import jakarta.persistence.*;
import java.util.regex.Pattern;

@Entity(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {
    
    private static final Pattern PATTERN_NOMBRE = 
        Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$");
    private static final Pattern PATTERN_NUIP = 
        Pattern.compile("^[0-9]{6,15}$");
    
    // Constantes de validación
    private static final int MIN_LONGITUD_NOMBRE = 2;
    private static final int MAX_LONGITUD_NOMBRE = 30;
    private static final int MIN_EDAD_USUARIO = 18;
    private static final int MAX_EDAD_USUARIO = 80;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nuip_usuario", nullable = false, unique = true)
    private String nuipUsuario;

    @Column(nullable = false, length = 30)
    private String primerNombre;

    @Column(nullable = true, length = 30)
    private String segundoNombre;

    @Column(nullable = false, length = 30)
    private String primerApellido;

    @Column(nullable = true, length = 30)
    private String segundoApellido;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Column(nullable = false, unique = true, length = 10)
    private String telefono;

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "token_access", referencedColumnName = "id_token", nullable = true)
    protected TokenUsuario tokenAccess;
    
    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nuipUsuario, String primerNombre, 
                   String segundoNombre, String primerApellido, String segundoApellido, 
                   int edad, String correoElectronico, String telefono, 
                   TokenUsuario tokenAccess) {
        this.idUsuario = idUsuario;
        this.nuipUsuario = nuipUsuario;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.edad = edad;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.tokenAccess = tokenAccess;
    }

    // ============================================
    // VALIDACIONES DE DOMINIO
    // ============================================
    
    /**
     * Valida un nombre (primer o segundo nombre, apellido)
     */
    public static ResultadoValidacionDominio validarNombre(
            String nombre, String nombreCampo, boolean esObligatorio) {
        
        if (nombre == null || nombre.trim().isEmpty()) {
            if (esObligatorio) {
                return ResultadoValidacionDominio.error(nombreCampo, 
                    "Campo obligatorio");
            }
            return ResultadoValidacionDominio.exito(); // Opcional y vacío está bien
        }
        
        if (nombre.length() < MIN_LONGITUD_NOMBRE || nombre.length() > MAX_LONGITUD_NOMBRE) {
            return ResultadoValidacionDominio.error(nombreCampo, 
                "Debe tener entre " + MIN_LONGITUD_NOMBRE + " y " + 
                MAX_LONGITUD_NOMBRE + " caracteres");
        }
        
        if (!PATTERN_NOMBRE.matcher(nombre).matches()) {
            return ResultadoValidacionDominio.error(nombreCampo, 
                "Solo se permiten letras, sin números ni caracteres especiales");
        }
        
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida el NUIP
     */
    public static ResultadoValidacionDominio validarNuip(String nuip) {
        if (nuip == null || nuip.trim().isEmpty()) {
            return ResultadoValidacionDominio.error("nuip", "El NUIP es obligatorio");
        }
        
        if (!PATTERN_NUIP.matcher(nuip).matches()) {
            return ResultadoValidacionDominio.error("nuip", 
                "El NUIP debe tener entre 6 y 15 dígitos numéricos");
        }
        
        return ResultadoValidacionDominio.exito();
    }

    /**
     * Valida que la edad del acudiente cumpla las reglas de negocio
     */
    public static ResultadoValidacionDominio validarEdad(Integer edad) {
        if (edad == null) {
            return ResultadoValidacionDominio.error("edad", "La edad es obligatoria");
        }
        if (edad < MIN_EDAD_USUARIO) {
            return ResultadoValidacionDominio.error("edad", 
                "El usuario debe ser mayor de " + MIN_EDAD_USUARIO + " años");
        }
        if (edad > MAX_EDAD_USUARIO) {
            return ResultadoValidacionDominio.error("edad", 
                "Edad máxima permitida: " + MAX_EDAD_USUARIO + " años");
        }
        return ResultadoValidacionDominio.exito();
    }
    
    
    /**
     * Valida todos los datos básicos del usuario
     * Método protegido para que las subclases lo usen
     */
    public ResultadoValidacionDominio validarDatosBasicos() {
        // Validar NUIP
        ResultadoValidacionDominio resultado = validarNuip(this.nuipUsuario);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar primer nombre (obligatorio)
        resultado = validarNombre(this.primerNombre, "primerNombre", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar segundo nombre (opcional)
        resultado = validarNombre(this.segundoNombre, "segundoNombre", false);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar primer apellido (obligatorio)
        resultado = validarNombre(this.primerApellido, "primerApellido", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar segundo apellido (opcional)
        resultado = validarNombre(this.segundoApellido, "segundoApellido", false);
        if (!resultado.isValido()) {
            return resultado;
        }

        resultado = validarEdad(this.edad);
        if (!resultado.isValido()){
            return resultado;
        }
        
        return ResultadoValidacionDominio.exito();
    }

    /**
     * Genera y asigna token usando la lógica del dominio TokenUsuario
     */
    public ResultadoOperacion generarTokenAutomatico(Rol rol) {
        // Validar si ya tiene token
        if (this.tokenAccess != null) {
            return ResultadoOperacion.error("El usuario ya tiene token asignado");
        }
        
        // Validar rol
        if (rol == null) {
            return ResultadoOperacion.error("El rol es obligatorio para generar token");
        }
        
        if (!rol.esValido()) {
            return ResultadoOperacion.error("El rol no es válido o no tiene permisos asignados");
        }
        
        try {
            // Usar la fábrica de TokenUsuario (lógica de dominio)
            TokenUsuario token = TokenUsuario.generarTokenDesdeUsuario(
                this.primerNombre,
                this.segundoNombre,
                this.primerApellido,
                this.segundoApellido,
                rol
            );
            
            this.tokenAccess = token;
            return ResultadoOperacion.exitoConDatos("Token generado exitosamente", token);
            
        } catch (IllegalArgumentException e) {
            return ResultadoOperacion.error("Datos insuficientes para generar token: " + e.getMessage());
        }
    }

    public abstract boolean requiereTokenAutomatico();

    
    /**
     * Método unificado de creación con validaciones
     */
    public ResultadoValidacionDominio crearUsuarioCompleto(Rol rol) {
        // 1. Validar datos básicos
        ResultadoValidacionDominio validacion = this.validarDatosBasicos();
        if (!validacion.isValido()) {
            return validacion;
        }
        
        // 2. Generar token si es requerido
        if (this.requiereTokenAutomatico()) {
            ResultadoOperacion resultadoToken = this.generarTokenAutomatico(rol);
            if (!resultadoToken.isExitoso()) {
                return ResultadoValidacionDominio.error("token", resultadoToken.getMensaje());
            }
        }
        
        return ResultadoValidacionDominio.exito();
    }

    // ============================================
    // MÉTODOS DE NEGOCIO
    // ============================================

    public String obtenerNombreCompleto() {
        StringBuilder nombre = new StringBuilder(primerNombre);
        if (segundoNombre != null && !segundoNombre.isEmpty()) {
            nombre.append(" ").append(segundoNombre);
        }
        nombre.append(" ").append(primerApellido);
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            nombre.append(" ").append(segundoApellido);
        }
        return nombre.toString();
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    
    public String getNuipUsuario() { return nuipUsuario; }
    public void setNuipUsuario(String nuipUsuario) { this.nuipUsuario = nuipUsuario; }
    
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { 
        this.correoElectronico = correoElectronico; 
    }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public TokenUsuario getTokenAccess() { return tokenAccess; }
    public void setTokenAccess(TokenUsuario tokenAccess) { this.tokenAccess = tokenAccess; }
}