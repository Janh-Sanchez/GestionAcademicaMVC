package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity(name = "acudiente")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Acudiente extends Usuario {
    public static final int MAX_ESTUDIANTES = 5;
    
    // Patrones de validación (parte del dominio)
    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PATTERN_TELEFONO = Pattern.compile("^[0-9]{10}$");

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", nullable = false, length = 20)
    private Estado estadoAprobacion = Estado.Pendiente;
    
    @OneToMany(mappedBy = "acudiente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    // Necesario para JPA
    public Acudiente() {
        super();
        this.estudiantes = new HashSet<>();
    }

    public Acudiente(Integer idUsuario, String nuipUsuario, String primerNombre, 
                     String segundoNombre, String primerApellido, String segundoApellido, 
                     int edad, String correoElectronico, String telefono, 
                     TokenUsuario tokenAccess, Estado estadoAprobacion) {
        super(idUsuario, nuipUsuario, primerNombre, segundoNombre, primerApellido, 
              segundoApellido, edad, correoElectronico, telefono, tokenAccess);
        this.estadoAprobacion = estadoAprobacion;
        this.estudiantes = new HashSet<>();
    }
    
    /**
     * Valida formato del correo electrónico
     */
    public static ResultadoValidacionDominio validarCorreoElectronico(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return ResultadoValidacionDominio.error("correoElectronico", 
                "El correo electrónico es obligatorio");
        }
        if (!PATTERN_EMAIL.matcher(correo).matches()) {
            return ResultadoValidacionDominio.error("correoElectronico", 
                "Formato de correo electrónico inválido");
        }
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida formato del teléfono
     */
    public static ResultadoValidacionDominio validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return ResultadoValidacionDominio.error("telefono", 
                "El teléfono es obligatorio");
        }
        if (!PATTERN_TELEFONO.matcher(telefono).matches()) {
            return ResultadoValidacionDominio.error("telefono", 
                "El teléfono debe tener exactamente 10 dígitos");
        }
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida todos los datos del acudiente
     */
    public ResultadoValidacionDominio validar() {
        // Validar datos heredados de Usuario
        ResultadoValidacionDominio validacion = super.validarDatosBasicos();
        if (!validacion.isValido()) {
            return validacion;
        }
        
        // Validar edad específica de acudiente
        validacion = validarEdad(this.getEdad());
        if (!validacion.isValido()) {
            return validacion;
        }
        
        // Validar correo
        validacion = validarCorreoElectronico(this.getCorreoElectronico());
        if (!validacion.isValido()) {
            return validacion;
        }
        
        // Validar teléfono
        validacion = validarTelefono(this.getTelefono());
        if (!validacion.isValido()) {
            return validacion;
        }
        
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Lógica de gestión de estudiantes
     * Agrega un estudiante verificando las reglas de negocio
     */
    public void agregarEstudiante(Estudiante estudiante) throws DomainException {
        if (this.estudiantes == null) {
            this.estudiantes = new HashSet<>();
        }
        
        if (this.estudiantes.size() >= MAX_ESTUDIANTES) {
            throw new DomainException(
                "No se pueden agregar más estudiantes. Límite máximo: " + MAX_ESTUDIANTES);
        }
        
        if (estudiante == null) {
            throw new DomainException("El estudiante no puede ser nulo");
        }
        
        estudiantes.add(estudiante);
        estudiante.setAcudiente(this);
    }

    /**
     * Verifica si se pueden agregar más estudiantes
     */
    public boolean puedeAgregarMasEstudiantes() {
        if (estudiantes == null) {
            return true;
        }
        return estudiantes.size() < MAX_ESTUDIANTES;
    }
    
    /**
     * Obtiene el número de cupos restantes
     */
    public int obtenerCuposRestantes() {
        if (estudiantes == null) {
            return MAX_ESTUDIANTES;
        }
        return Math.max(0, MAX_ESTUDIANTES - estudiantes.size());
    }

    public Estado getEstadoAprobacion() {
        return estadoAprobacion;
    }

    public void setEstadoAprobacion(Estado estado) {
        this.estadoAprobacion = estado;
    }

    public Set<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(Set<Estudiante> estudiantes) {
        if (estudiantes == null) {
            this.estudiantes = new HashSet<>();
        } else {
            this.estudiantes = estudiantes;
        }
    }

    public static class DomainException extends Exception {
        public DomainException(String mensaje) {
            super(mensaje);
        }
    }
}