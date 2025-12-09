package com.modelo.dominio;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity(name = "observacion")
public class Observacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_observacion")
    private Integer idObservacion;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Column(name = "fecha_observacion", nullable = false)
    private LocalDate fechaObservacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "observador", referencedColumnName = "id_observador")
    private Observador observador;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "profesor", referencedColumnName = "id_usuario")
    private Profesor profesor;

    public Observacion(Integer idObservacion, String descripcion, LocalDate fechaObservacion, Observador observador, Profesor profesor){
        this.idObservacion = idObservacion;
        this.descripcion = descripcion;
        this.fechaObservacion = fechaObservacion;
        this.observador = observador;
        this.profesor = profesor;
    }

    public Observacion(){
        
    }

    // getters y setters
    public Integer getIdObservacion() {
        return idObservacion;
    }

    public void setIdObservacion(Integer idObservacion) {
        this.idObservacion = idObservacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaObservacion() {
        return fechaObservacion;
    }

    public void setFechaObservacion(LocalDate fechaObservacion) {
        this.fechaObservacion = fechaObservacion;
    }

    public Observador getObservador() {
        return observador;
    }

    public void setObservador(Observador observador) {
        this.observador = observador;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public boolean esValida(){
        return (descripcion.length() >= 10 && descripcion.length() <= 200);
    }
}
