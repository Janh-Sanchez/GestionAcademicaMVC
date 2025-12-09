package com.modelo.dominio;

import java.util.Set;

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
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "estudiante")
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    @NotBlank
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerNombre;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(length = 30)
    private String segundoNombre;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerApellido;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(length = 30)
    private String segundoApellido;

    @Pattern(regexp = "^[0-9]{6,15}$", message = "Debe tener entre 6 y 15 dígitos numéricos")
    @Column(nullable = false, unique = true, length = 15)
    private String nuip;

    @Min(3)
    @Max(120)
    @Column(nullable = false)
    private int edad;

    @NotNull
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

    public Estudiante(Integer idEstudiante, String primerNombre, String segundoNombre, String primerApellido,
        String segundoApellido, String nuip, int edad, Estado estado, Acudiente acudiente,
        Grado gradoAspira, Grupo grupo, HojaVida hojaDeVida, Observador observador,
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
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
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
    public void setPreinscripcion(Preinscripcion preinscripcion) { this.preinscripcion = preinscripcion; }
    public Set<LogroEstudiante> getLogrosCalificados() { return logrosCalificados; }
    public void setLogrosCalificados(Set<LogroEstudiante> logrosCalificados) { this.logrosCalificados = logrosCalificados; }
    public Set<Boletin> getBoletines() { return boletines; }
    public void setBoletines(Set<Boletin> boletines) { this.boletines = boletines; }

    public void agregarBoletin(Boletin boletin){
        boletines.add(boletin);
    }

    public void agregarLogrosEstudiante(LogroEstudiante logroCalificado){
        logrosCalificados.add(logroCalificado);
    }
}