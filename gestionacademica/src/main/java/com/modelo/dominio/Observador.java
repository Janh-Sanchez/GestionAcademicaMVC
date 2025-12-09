package com.modelo.dominio;

import java.util.HashSet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity(name = "observador")
public class Observador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_observador")
    private Integer idObservador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", unique = true)
    private Estudiante estudiante;

    @OneToMany(mappedBy = "observador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private HashSet<Observacion> observaciones;

    public Observador(Integer idObservador, Estudiante estudiante, HashSet<Observacion> observaciones){
        this.idObservador = idObservador;
        this.estudiante = estudiante;
        this.observaciones = observaciones;
    }

    public Observador(){
        this.observaciones = new HashSet<>();
    }

    public Integer getIdObservador() {
        return idObservador;
    }

    public void setIdObservador(Integer idObservador) {
        this.idObservador = idObservador;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public HashSet<Observacion> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(HashSet<Observacion> observaciones) {
        this.observaciones = observaciones;
    }

    public void agregarObservacion(Observacion observacion){
        observaciones.add(observacion);
    }
}