package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "grupo")
public class Grupo {
    private final int MINESTUDIANTES = 5;
    private final int MAXESTUDIANTES = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    @NotBlank
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9 ÁÉÍÓÚáéíóúñÑ-]+$", message = "Solo letras, números y espacios")
    @Column(name = "nombre_grupo", nullable = false, length = 50)
    private String nombreGrupo;

    @Column(nullable = false)
    private boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado", referencedColumnName = "id_grado")
    private Grado grado;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "profesor", referencedColumnName = "id_usuario")
	private Profesor profesor;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    public Grupo(Integer idGrupo, String nombreGrupo, boolean estado, Grado grado, Profesor profesor, Set<Estudiante> estudiantes) {
        this.idGrupo = idGrupo;
        this.nombreGrupo = nombreGrupo;
        this.estado = estado;
        this.grado = grado;
        this.profesor = profesor;
        this.estudiantes = estudiantes;
    }

    public Grupo(){
        this.estudiantes = new HashSet<>();
    }

    public int getMINESTUDIANTES() {
        return MINESTUDIANTES;
    }

    public int getMAXESTUDIANTES() {
        return MAXESTUDIANTES;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public Set<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(Set<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public boolean tieneEstudiantesSuficientes(){
        return (estudiantes.size() >= MINESTUDIANTES);
    }

    public boolean tieneDisponibilidad(){
        return (estudiantes.size() < MAXESTUDIANTES);
    }
}