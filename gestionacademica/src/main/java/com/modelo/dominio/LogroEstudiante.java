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
import jakarta.validation.constraints.NotNull;

@Entity(name = "logros_estudiante")
public class LogroEstudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro_estudiante")
    private Integer idLogroEstudiante;

    @NotNull
    @Column(name = "fecha_calificacion", nullable = false)
    private LocalDate fechaCalificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", referencedColumnName = "id_estudiante")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boletin", referencedColumnName = "id_boletin")
    private Boletin boletin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logro", referencedColumnName = "id_logro")
    private Logro logro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor", referencedColumnName = "id_usuario")
    private Profesor profesor;

    public LogroEstudiante(Integer idLogroEstudiante, LocalDate fechaCalificacion, Estudiante estudiante,
            Boletin boletin, Logro logro, Profesor profesor) {
        this.idLogroEstudiante = idLogroEstudiante;
        this.fechaCalificacion = fechaCalificacion;
        this.estudiante = estudiante;
        this.boletin = boletin;
        this.logro = logro;
        this.profesor = profesor;
    }

    public LogroEstudiante(){}

    public Integer getIdLogroEstudiante() {
        return idLogroEstudiante;
    }

    public void setIdLogroEstudiante(Integer idLogroEstudiante) {
        this.idLogroEstudiante = idLogroEstudiante;
    }

    public LocalDate getFechaCalificacion() {
        return fechaCalificacion;
    }

    public void setFechaCalificacion(LocalDate fechaCalificacion) {
        this.fechaCalificacion = fechaCalificacion;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Boletin getBoletin() {
        return boletin;
    }

    public void setBoletin(Boletin boletin) {
        this.boletin = boletin;
    }

    public Logro getLogro() {
        return logro;
    }

    public void setLogro(Logro logro) {
        this.logro = logro;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }
}