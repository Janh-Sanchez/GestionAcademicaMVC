package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

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

    private Set<Observacion> observaciones;

    public Observador(Integer idObservador, Estudiante estudiante, Set<Observacion> observaciones){
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

    public Set<Observacion> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(Set<Observacion> observaciones) {
        if (observaciones == null) {
            this.observaciones = new HashSet<>();
        } else {
            this.observaciones = observaciones;
        }
    }

    public void agregarObservacion(Observacion observacion) {
        observaciones.add(observacion);
    }
}