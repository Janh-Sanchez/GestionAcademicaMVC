package com.dominio;

import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @NotBlank
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
    @JoinColumn(name = "acudiente", nullable = false, referencedColumnName = "idUsuario")
    private Acudiente acudiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gradoAspira", referencedColumnName = "idGrado")
    private Grado gradoAspira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo", referencedColumnName = "idGrupo")
    private Grupo grupo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hojaDeVida", unique = true, referencedColumnName = "idHojaVida")
    private HojaVida hojaDeVida;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "observador", unique = true)
    private Observador observador;

	@OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<LogroEstudiante> logrosCalificados;

	@OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Boletin> boletines;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "preinscripcion_id", referencedColumnName = "idPreinscripcion")
	private Preinscripcion preinscripcion;

    public Estudiante(){

    }
}//end Estudiante