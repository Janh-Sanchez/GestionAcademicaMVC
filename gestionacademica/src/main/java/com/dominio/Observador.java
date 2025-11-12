package com.dominio;

import jakarta.persistence.*;
import java.util.SortedSet;

import org.hibernate.annotations.SortNatural;

@Entity
public class Observador {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idObservador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", unique = true)
    private Estudiante estudiante;

    @OneToMany(mappedBy = "observador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@SortNatural
    private SortedSet<Observacion> observaciones;

    public Observador(){

    }

    public void agregarObservacion(Observacion observacion){

    }

    public void eliminarObservacion(Observacion observacion){

    }
}//end Observador