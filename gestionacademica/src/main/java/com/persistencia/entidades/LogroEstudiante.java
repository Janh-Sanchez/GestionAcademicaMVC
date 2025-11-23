package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class LogroEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idLogroEstudiante;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaCalificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", referencedColumnName = "idEstudiante")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boletin", referencedColumnName = "idBoletin")
    private Boletin boletin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logro", referencedColumnName = "idLogro")
    private Logro logro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor", referencedColumnName = "idUsuario")
    private Profesor profesor;

    public LogroEstudiante(){

    }

    public void agregarLogro(Logro logro){

    }
}//end LogroEstudiante