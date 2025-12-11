package com.modelo.dominio;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity(name = "preinscripcion")
public class Preinscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preinscripcion")
    private Integer idPreinscripcion;

    @NotNull
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.Pendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acudiente", referencedColumnName = "id_usuario")
    private Acudiente acudiente;

    @OneToMany(mappedBy = "preinscripcion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    // Constructor para JPA
    public Preinscripcion() {
        this.estudiantes = new HashSet<>();
    }

    // Constructor para creación de nuevas preinscripciones
    public Preinscripcion(Acudiente acudiente) {
        this();
        this.acudiente = acudiente;
        this.fechaRegistro = LocalDate.now();
        this.estado = Estado.Pendiente;
    }

    // Métodos de dominio

    /**
     * Agrega un estudiante a la preinscripción
     */
    public void agregarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo");
        }
        
        if (this.estudiantes == null) {
            this.estudiantes = new HashSet<>();
        }
        
        estudiante.setPreinscripcion(this);
        this.estudiantes.add(estudiante);
    }

    /**
     * Aprueba un estudiante específico dentro de la preinscripción
     */
    public ResultadoOperacion aprobarEstudiante(Integer idEstudiante, Rol rolAcudiente) 
        throws DomainException {
        
        // 1. Buscar estudiante
        Estudiante estudiante = buscarEstudiantePorId(idEstudiante);
        if (estudiante == null) {
            throw new DomainException("Estudiante no encontrado en la preinscripción");
        }
        
        // 2. Cambiar estado del estudiante
        estudiante.setEstado(Estado.Aprobada);
        
        // 3. Actualizar estado de la preinscripción
        actualizarEstadoPreinscripcion();
        
        // 4. Si la preinscripción está aprobada, generar token para acudiente
        if (this.estado == Estado.Aprobada && this.acudiente.getTokenAccess() == null) {
            // Usar el rol inyectado desde el controlador
            ResultadoOperacion resultadoToken = this.acudiente.generarTokenSiAprobado(rolAcudiente);
            if (!resultadoToken.isExitoso()) {
                throw new DomainException("Error generando token: " + resultadoToken.getMensaje());
            }
        }
        
        return ResultadoOperacion.exito("Estudiante aprobado exitosamente");
    }
    
    /**
     * Rechaza un estudiante específico dentro de la preinscripción
     */
    public ResultadoOperacion rechazarEstudiante(Integer idEstudiante) 
        throws DomainException {
        
        Estudiante estudiante = buscarEstudiantePorId(idEstudiante);
        if (estudiante == null) {
            throw new DomainException("Estudiante no encontrado en la preinscripción");
        }
        
        // 1. Rechazar el estudiante
        estudiante.setEstado(Estado.Rechazada);
        
        // 2. Actualizar estado de la preinscripción
        actualizarEstadoPreinscripcion();
        
        // 3. Si después de actualizar, TODOS están rechazados, asegurar que el acudiente también esté rechazado
        if (todosEstudiantesRechazados()) {
            this.acudiente.setEstadoAprobacion(Estado.Rechazada);
            // Asegurar que NO haya token si está rechazado
            if (this.acudiente.getTokenAccess() != null) {
                this.acudiente.setTokenAccess(null);
            }
        }
        
        return ResultadoOperacion.exito("Estudiante rechazado exitosamente");
    }

    /**
     * Actualiza el estado considerando si necesita generar token
     */
    public void actualizarEstadoPreinscripcionConRol(Rol rolAcudiente) throws DomainException {
        actualizarEstadoPreinscripcion();
        
        // Si ahora está aprobada y no tiene token, generarlo
        if (this.estado == Estado.Aprobada && 
            this.acudiente.getEstadoAprobacion() == Estado.Aprobada &&
            this.acudiente.getTokenAccess() == null) {
            
            ResultadoOperacion resultadoToken = this.acudiente.generarTokenSiAprobado(rolAcudiente);
            if (!resultadoToken.isExitoso()) {
                throw new DomainException("Error generando token: " + resultadoToken.getMensaje());
            }
        }
    }

    /**
     * Actualiza el estado de la preinscripción basado en el estado de sus estudiantes
     * Reglas de negocio CORREGIDAS:
     * 1. TODOS rechazados → Preinscripción Rechazada Y Acudiente Rechazado
     * 2. Al menos 1 aprobado y CERO pendientes → Preinscripción Aprobada Y Acudiente Aprobado
     * 3. Pendientes > 0 → Preinscripción Pendiente (a menos que ya esté aprobada)
     * 4. Si hay rechazados y aprobados pero CERO pendientes → Preinscripción Aprobada (al menos uno aprobado)
     */
    public void actualizarEstadoPreinscripcion() throws DomainException {
        if (this.estudiantes == null || this.estudiantes.isEmpty()) {
            throw new DomainException("La preinscripción no tiene estudiantes");
        }

        if (this.acudiente == null) {
            throw new DomainException("La preinscripción no tiene acudiente asociado");
        }
        
        // Contar estudiantes por estado
        long pendientes = contarEstudiantesPorEstado(Estado.Pendiente);
        long aprobados = contarEstudiantesPorEstado(Estado.Aprobada);
        long rechazados = contarEstudiantesPorEstado(Estado.Rechazada);
        long total = this.estudiantes.size();
        
        // Determinar nuevo estado
        Estado nuevoEstado = this.estado;
        Estado estadoAcudiente = this.acudiente.getEstadoAprobacion();
        
        if (rechazados == total) {
            // CASO 1: TODOS los estudiantes fueron rechazados
            nuevoEstado = Estado.Rechazada;
            estadoAcudiente = Estado.Rechazada;
            
        } 
        // CASO 2: Al menos un estudiante aprobado y NINGUNO pendiente
        // (puede haber rechazados también, pero al menos hay un aprobado)
        else if (aprobados > 0 && pendientes == 0) {
            nuevoEstado = Estado.Aprobada;
            estadoAcudiente = Estado.Aprobada;
            
        } 

        // CASO 3: Hay estudiantes pendientes
        else if (pendientes > 0) {
            nuevoEstado = Estado.Pendiente;
            
            // Solo poner acudiente como pendiente si no está ya aprobado
            if (estadoAcudiente != Estado.Aprobada) {
                estadoAcudiente = Estado.Pendiente;
            }
        } 
        else {
            // CASO 4: Situación inesperada - mantener estado actual
            // Esto no debería ocurrir si las reglas anteriores están bien definidas
            System.err.println("Situación no manejada en actualizarEstadoPreinscripcion: " +
                             "pendientes=" + pendientes + ", aprobados=" + aprobados + 
                             ", rechazados=" + rechazados);
        }
        
        // Actualizar estado de la preinscripción
        if (this.estado != nuevoEstado) {
            this.estado = nuevoEstado;
        }
        
        // Actualizar estado del acudiente
        if (this.acudiente.getEstadoAprobacion() != estadoAcudiente) {
            this.acudiente.setEstadoAprobacion(estadoAcudiente);
        }
    }

    /**
     * Obtiene solo los estudiantes pendientes
     */
    public Set<Estudiante> obtenerEstudiantesPendientes() {
        if (this.estudiantes == null) {
            return new HashSet<>();
        }
        
        return this.estudiantes.stream()
            .filter(e -> e.getEstado() == Estado.Pendiente)
            .collect(Collectors.toSet());
    }

    /**
     * Verifica si la preinscripción tiene estudiantes pendientes
     */
    public boolean tieneEstudiantesPendientes() {
        return contarEstudiantesPorEstado(Estado.Pendiente) > 0;
    }

    /**
     * Verifica si se puede procesar la preinscripción (tiene estudiantes)
     */
    public boolean esProcesable() {
        return this.estudiantes != null && !this.estudiantes.isEmpty();
    }

    /**
     * Verifica si TODOS los estudiantes fueron rechazados
     */
    public boolean todosEstudiantesRechazados() {
        if (this.estudiantes == null || this.estudiantes.isEmpty()) {
            return false;
        }
        
        return contarEstudiantesPorEstado(Estado.Rechazada) == this.estudiantes.size();
    }

    /**
     * Verifica si la preinscripción puede ser aprobada
     * (al menos un estudiante aprobado y ningún pendiente)
     */
    public boolean puedeSerAprobada() {
        if (this.estudiantes == null || this.estudiantes.isEmpty()) {
            return false;
        }
        
        long pendientes = contarEstudiantesPorEstado(Estado.Pendiente);
        long aprobados = contarEstudiantesPorEstado(Estado.Aprobada);
        
        return aprobados > 0 && pendientes == 0;
    }

    // Métodos auxiliares privados

    private Estudiante buscarEstudiantePorId(Integer idEstudiante) {
        if (this.estudiantes == null) {
            return null;
        }
        
        return this.estudiantes.stream()
            .filter(e -> e.getIdEstudiante().equals(idEstudiante))
            .findFirst()
            .orElse(null);
    }

    private long contarEstudiantesPorEstado(Estado estado) {
        if (this.estudiantes == null) {
            return 0;
        }
        
        return this.estudiantes.stream()
            .filter(e -> e.getEstado() == estado)
            .count();
    }

    // Métodos de validación

    /**
     * Valida que todos los datos de la preinscripción sean correctos
     */
    public ResultadoValidacionDominio validar() {
        if (this.acudiente == null) {
            return ResultadoValidacionDominio.error("acudiente", "El acudiente es obligatorio");
        }
        
        if (this.fechaRegistro == null) {
            return ResultadoValidacionDominio.error("fechaRegistro", "La fecha de registro es obligatoria");
        }
        
        if (this.fechaRegistro.isAfter(LocalDate.now())) {
            return ResultadoValidacionDominio.error("fechaRegistro", "La fecha de registro no puede ser futura");
        }
        
        if (this.estudiantes == null || this.estudiantes.isEmpty()) {
            return ResultadoValidacionDominio.error("estudiantes", "La preinscripción debe tener al menos un estudiante");
        }
        
        // Validar que todos los estudiantes sean válidos
        for (Estudiante estudiante : this.estudiantes) {
            ResultadoValidacionDominio validacionEstudiante = estudiante.validar();
            if (!validacionEstudiante.isValido()) {
                return validacionEstudiante;
            }
        }
        
        return ResultadoValidacionDominio.exito();
    }

    // Getters y Setters

    public Integer getIdPreinscripcion() {
        return idPreinscripcion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro){
        this.fechaRegistro = fechaRegistro;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado){
        this.estado = estado;
    }

    public Acudiente getAcudiente() {
        return acudiente;
    }

    public void setAcudiente(Acudiente acudiente){
        this.acudiente = acudiente;
    }

    public Set<Estudiante> getEstudiantes() {
        return estudiantes != null ? estudiantes : new HashSet<>();
    }

    public void setEstudiantes(HashSet<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    // Excepción de dominio para Preinscripcion
    public static class DomainException extends Exception {
        public DomainException(String mensaje) {
            super(mensaje);
        }
    }
}