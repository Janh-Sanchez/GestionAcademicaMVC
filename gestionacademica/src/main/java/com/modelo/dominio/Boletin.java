package com.modelo.dominio;
import java.time.LocalDate;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity(name = "boletin")
public class Boletin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boletin")
    private Integer idBoletin;

    
    @NotBlank
    @Size(min = 3, max = 20)
    @Column(nullable = false, length = 20)
    private String periodo;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaGeneracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", referencedColumnName = "id_estudiante")
    private Estudiante estudiante;

    @OneToMany(mappedBy = "boletin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LogroEstudiante> logrosEstudiante;

    public Boletin(Integer idBoletin, String periodo, LocalDate fechaGeneracion, Estudiante estudiante, Set<LogroEstudiante> logrosEstudiante) {
        this.idBoletin = idBoletin;
        this.periodo = periodo;
        this.fechaGeneracion = fechaGeneracion;
        this.estudiante = estudiante;
        this.logrosEstudiante = logrosEstudiante;
    }

    public Boletin(){
        this.logrosEstudiante = new HashSet<>();
    }

    public Integer getIdBoletin() {
        return idBoletin;
    }

    public void setIdBoletin(Integer idBoletin) {
        this.idBoletin = idBoletin;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Set<LogroEstudiante> getLogrosEstudiante() {
        return logrosEstudiante;
    }

    public void setLogrosEstudiante(Set<LogroEstudiante> logrosEstudiante) {
        this.logrosEstudiante = logrosEstudiante;
    }

    public void añadirLogroEstudiante(LogroEstudiante logroEstudiante){
        if (logrosEstudiante != null) {
            logrosEstudiante.add(logroEstudiante);
        }
    }

    public void eliminarCalificacion(LogroEstudiante calificacion){
        if (logrosEstudiante != null) {
            logrosEstudiante.remove(calificacion);
        }
    }

    public void generarBoletin(){
        // Lógica de generación pendiente
    }
}