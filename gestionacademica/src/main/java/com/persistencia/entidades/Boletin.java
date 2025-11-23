package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class Boletin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idBoletin;

    @NotBlank
    @Size(min = 3, max = 20)
    @Column(nullable = false, length = 20)
    private String periodo;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaGeneracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", referencedColumnName = "idEstudiante")
    private Estudiante estudiante;

	@OneToMany(mappedBy = "boletin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<LogroEstudiante> logrosEstudiante;

    public Boletin(){

    }

    public void añadirLogroEstudiante(LogroEstudiante logroEstudiante){

    }

    public void eliminarCalificacion(LogroEstudiante calificacion){

    }

    public void generarBoletin(){

    }
}//end Boletin