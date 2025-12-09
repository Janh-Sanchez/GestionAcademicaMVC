package com.modelo.dominio;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity(name = "preinscripcion")
public class Preinscripcion{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preinscripcion")
    private Integer idPreinscripcion;

    @NotNull
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.Pendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acudiente", referencedColumnName = "id_usuario")
    private Acudiente acudiente;

    @OneToMany(mappedBy = "preinscripcion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Estudiante> estudiantes;

    public Preinscripcion(Integer idPreinscripcion, LocalDate fechaRegistro, Estado estado, Acudiente acudiente, Set<Estudiante> estudiantes) {
        this.idPreinscripcion = idPreinscripcion;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.acudiente = acudiente;
        this.estudiantes = estudiantes;
    }

    public Preinscripcion() {
        //TODO Auto-generated constructor stub
    }

    public Integer getIdPreinscripcion() {
        return idPreinscripcion;
    }

    public void setIdPreinscripcion(Integer idPreinscripcion) {
        this.idPreinscripcion = idPreinscripcion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Acudiente getAcudiente() {
        return acudiente;
    }

    public void setAcudiente(Acudiente acudiente) {
        this.acudiente = acudiente;
    }

    public Set<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(Set<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public void cambiarEstado(Estado nuevoEstado){
        
    }

    public boolean validarDatos(){
        return false;
    }
}