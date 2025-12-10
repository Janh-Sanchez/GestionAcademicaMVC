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
    @Pattern(regexp = "^[A-Za-z0-9 ÁÉÍÓÚáéíóúñÑ'-]+$", message = "Solo letras, números y espacios")
    @Column(name = "nombre_grupo", nullable = false, length = 50)
    private String nombreGrupo;

    @Column(nullable = false)
    private boolean estado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado", referencedColumnName = "id_grado")
    private Grado grado;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor", referencedColumnName = "id_usuario")
    private Profesor profesor;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    public Grupo(Integer idGrupo, String nombreGrupo, boolean estado, Grado grado, 
                Profesor profesor, Set<Estudiante> estudiantes) {
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

    /**
     * Verifica si el grupo tiene suficientes estudiantes (>= 5)
     */
    public boolean tieneEstudiantesSuficientes(){
        return (estudiantes.size() >= MINESTUDIANTES);
    }

    /**
     * Verifica si el grupo tiene disponibilidad para más estudiantes (< 10)
     */
    public boolean tieneDisponibilidad(){
        return (estudiantes.size() < MAXESTUDIANTES);
    }

    /**
     * Verifica si el grupo está listo (activo y con estudiantes suficientes)
     * Un grupo está listo cuando tiene >= 5 estudiantes
     */
    /**
     * Verifica si el grupo está listo (activo y con estudiantes suficientes)
     * Solo verifica, no modifica el estado
     */
    public boolean estaListo() {
        return tieneEstudiantesSuficientes() && estado;
    }

    /**
     * Verifica si el grupo puede activarse (tiene suficientes estudiantes)
     * Esto es diferente de si ya está activo
     */
    public boolean puedeActivar() {
        return tieneEstudiantesSuficientes();
    }

    /**
     * Activa el grupo si tiene suficientes estudiantes
     * @return true si se pudo activar, false si no cumple los requisitos
     */
    public boolean activar() {
        if (tieneEstudiantesSuficientes() && this.estado == false) {
            this.estado = true;
            return true;
        }
        return false;
    }

    /**
     * Verifica si el grupo está en formación
     * Un grupo está en formación cuando tiene < 5 estudiantes
     */
    public boolean estaEnFormacion(){
        return !tieneEstudiantesSuficientes();
    }

    /**
     * Verifica si el grupo tiene un profesor asignado
     */
    public boolean tieneProfesorAsignado(){
        return profesor != null;
    }

    /**
     * Obtiene la cantidad actual de estudiantes
     */
    public int getCantidadEstudiantes(){
        return estudiantes != null ? estudiantes.size() : 0;
    }

    /**
    * Método para agregar estudiante y verificar si se activa automáticamente
     * @param estudiante el estudiante a agregar
     * @return true si al agregar este estudiante el grupo se activa
     */
    public boolean agregarEstudiante(Estudiante estudiante) {
        if (estudiantes == null) {
            estudiantes = new HashSet<>();
        }
        
        if (!tieneDisponibilidad()) {
            return false; // No hay disponibilidad
        }
        
        estudiantes.add(estudiante);
        estudiante.setGrupo(this); // Asegurar la relación bidireccional
        
        activar();
        
        return true;
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
}