package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.SortedSet;

@Entity
public class Preinscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idPreinscripcion;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.Pendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acudiente", referencedColumnName = "idUsuario")
    private Acudiente acudiente;

	@OneToMany(mappedBy = "preinscripcion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private SortedSet<Estudiante> estudiantes;

    public Preinscripcion(){

    }

    public void cambiarEstado(Estado nuevoEstado){

    }

    public boolean validarDatos(){
        return false;
    }
}//end Preinscripcion