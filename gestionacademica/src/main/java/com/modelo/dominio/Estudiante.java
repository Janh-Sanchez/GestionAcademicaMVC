package com.modelo.dominio;

import jakarta.persistence.*;
import java.util.Set;
import java.util.regex.Pattern;

@Entity(name = "estudiante")
public class Estudiante {
    private static final int MIN_EDAD_ESTUDIANTE = 3;
    private static final int MAX_EDAD_ESTUDIANTE = 10;
    private static final int MIN_LONGITUD_NOMBRE = 2;
    private static final int MAX_LONGITUD_NOMBRE = 30;
    
    // Patrones de validación
    private static final Pattern PATTERN_NOMBRE = 
        Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$");
    private static final Pattern PATTERN_NUIP = 
        Pattern.compile("^[0-9]{6,15}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    @Column(nullable = false, length = 30)
    private String primerNombre;

    @Column(length = 30)
    private String segundoNombre;

    @Column(nullable = false, length = 30)
    private String primerApellido;

    @Column(length = 30)
    private String segundoApellido;

    @Column(nullable = false, unique = true, length = 15)
    private String nuip;

    @Column(nullable = false)
    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.Pendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acudiente", nullable = false, referencedColumnName = "id_usuario")
    private Acudiente acudiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grad_aspira", referencedColumnName = "id_grado")
    private Grado gradoAspira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo", referencedColumnName = "id_grupo")
    private Grupo grupo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hoja_de_vida", unique = true, referencedColumnName = "id_hoja_vida")
    private HojaVida hojaDeVida;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "observador", unique = true)
    private Observador observador;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LogroEstudiante> logrosCalificados;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Boletin> boletines;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_preinscripcion", referencedColumnName = "id_preinscripcion")
    private Preinscripcion preinscripcion;

    public Estudiante() {
    }

    public Estudiante(Integer idEstudiante, String primerNombre, String segundoNombre, 
                     String primerApellido, String segundoApellido, String nuip, 
                     Integer edad, Estado estado, Acudiente acudiente, Grado gradoAspira, 
                     Grupo grupo, HojaVida hojaDeVida, Observador observador, 
                     Set<LogroEstudiante> logrosCalificados, Set<Boletin> boletines) {
        this.idEstudiante = idEstudiante;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.nuip = nuip;
        this.edad = edad;
        this.estado = estado;
        this.acudiente = acudiente;
        this.gradoAspira = gradoAspira;
        this.grupo = grupo;
        this.hojaDeVida = hojaDeVida;
        this.observador = observador;
        this.logrosCalificados = logrosCalificados;
        this.boletines = boletines;
    }

    // Validaciones de dominio
    
    /**
     * Valida un nombre del estudiante
     */
    public static ResultadoValidacionDominio validarNombre(
            String nombre, String nombreCampo, boolean esObligatorio) {
        
        if (nombre == null || nombre.trim().isEmpty()) {
            if (esObligatorio) {
                return ResultadoValidacionDominio.error(nombreCampo, "Campo obligatorio");
            }
            return ResultadoValidacionDominio.exito();
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
     * Valida la edad del estudiante
     */
    public static ResultadoValidacionDominio validarEdad(Integer edad) {
        if (edad == null) {
            return ResultadoValidacionDominio.error("edad", "La edad es obligatoria");
        }
        
        if (edad < MIN_EDAD_ESTUDIANTE) {
            return ResultadoValidacionDominio.error("edad", 
                "El estudiante debe tener al menos " + MIN_EDAD_ESTUDIANTE + " años");
        }
        
        if (edad > MAX_EDAD_ESTUDIANTE) {
            return ResultadoValidacionDominio.error("edad", 
                "La edad máxima permitida es " + MAX_EDAD_ESTUDIANTE + " años");
        }
        
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida el NUIP del estudiante
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
     * Valida que el grado esté asignado
     */
    public static ResultadoValidacionDominio validarGradoAspira(Grado grado) {
        if (grado == null) {
            return ResultadoValidacionDominio.error("gradoAspira", 
                "Debe seleccionar un grado al que aspira");
        }
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida todos los datos del estudiante
     */
    public ResultadoValidacionDominio validar() {
        // Validar primer nombre
        ResultadoValidacionDominio resultado = validarNombre(this.primerNombre, "primerNombre", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar segundo nombre (opcional)
        resultado = validarNombre(this.segundoNombre, "segundoNombre", false);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar primer apellido
        resultado = validarNombre(this.primerApellido, "primerApellido", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar segundo apellido (opcional)
        resultado = validarNombre(this.segundoApellido, "segundoApellido", false);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar edad
        resultado = validarEdad(this.edad);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar NUIP
        resultado = validarNuip(this.nuip);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar grado
        resultado = validarGradoAspira(this.gradoAspira);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        return ResultadoValidacionDominio.exito();
    }


    public void agregarBoletin(Boletin boletin) {
        boletines.add(boletin);
    }

    public void agregarLogrosEstudiante(LogroEstudiante logroCalificado) {
        logrosCalificados.add(logroCalificado);
    }

    public Integer getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }
    
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    
    public String getNuip() { return nuip; }
    public void setNuip(String nuip) { this.nuip = nuip; }
    
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente acudiente) { this.acudiente = acudiente; }
    
    public Grado getGradoAspira() { return gradoAspira; }
    public void setGradoAspira(Grado gradoAspira) { this.gradoAspira = gradoAspira; }
    
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
    
    public HojaVida getHojaDeVida() { return hojaDeVida; }
    public void setHojaDeVida(HojaVida hojaDeVida) { this.hojaDeVida = hojaDeVida; }
    
    public Observador getObservador() { return observador; }
    public void setObservador(Observador observador) { this.observador = observador; }
    
    public Preinscripcion getPreinscripcion() { return preinscripcion; }
    public void setPreinscripcion(Preinscripcion preinscripcion) { 
        this.preinscripcion = preinscripcion; 
    }
    
    public Set<LogroEstudiante> getLogrosCalificados() { return logrosCalificados; }
    public void setLogrosCalificados(Set<LogroEstudiante> logrosCalificados) { 
        this.logrosCalificados = logrosCalificados; 
    }
    
    public Set<Boletin> getBoletines() { return boletines; }
    public void setBoletines(Set<Boletin> boletines) { this.boletines = boletines; }
}