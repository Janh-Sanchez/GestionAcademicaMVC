package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Entity
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idGrupo;

    @NotBlank
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9 ÁÉÍÓÚáéíóúñÑ-]+$", message = "Solo letras, números y espacios")
    @Column(nullable = false, length = 50)
    private String nombreGrupo;

    @Column(nullable = false)
    private boolean estado;

    @Min(1)
    @Max(100)
    @Column(nullable = false)
    private int minEstudiantes = 5;

    @Min(1)
    @Max(100)
    @Column(nullable = false)
    private int maxEstudiantes = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado", referencedColumnName = "idGrado")
    private Grado grado;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "profesor", referencedColumnName = "idUsuario")
	private Profesor profesor;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    public Grupo(){

    }

    public void agregarEstudiante(Estudiante estudiante){

    }

    public boolean tieneEstudiantesSuficientes(){
        return false;
    }
}//end Grupo