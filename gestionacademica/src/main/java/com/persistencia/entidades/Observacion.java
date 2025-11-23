package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idObservacion;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaObservacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "observador", referencedColumnName = "idObservador")
    private Observador observador;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "profesor", referencedColumnName = "idUsuario")
    private Profesor profesor;

    public Observacion(){

    }
}//end Observacion