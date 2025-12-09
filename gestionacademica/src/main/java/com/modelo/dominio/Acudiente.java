package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.NotNull;

@Entity(name = "acudiente")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Acudiente extends Usuario {
    // Constante de dominio - máximo de estudiantes por acudiente
    public static final int MAX_ESTUDIANTES = 5;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", nullable = false, length = 20)
    private Estado estadoAprobacion = Estado.Pendiente;
    
    @OneToMany(mappedBy = "acudiente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes;

    public Acudiente(Integer idUsuario, String nuipUsuario, String primerNombre, String segundoNombre, String primerApellido, 
        String segundoApellido, int edad, String correoElectronico, String telefono, 
        TokenUsuario tokenAccess, Estado estadoAprobacion, Set<Estudiante> estudiantes) {
        super(idUsuario, nuipUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, 
              edad, correoElectronico, telefono, tokenAccess);
        this.estadoAprobacion = estadoAprobacion;
        this.estudiantes = estudiantes != null ? estudiantes : new HashSet<>();
    }

    public Acudiente() {
        super();
        this.estudiantes = new HashSet<>(); // Inicializar aquí
    }

    public void agregarEstudiante(Estudiante estudiante) throws Exception {
        // Verificar que estudiantes no sea null
        if (this.estudiantes == null) {
            this.estudiantes = new HashSet<>();
        }
        
        if (this.estudiantes.size() >= MAX_ESTUDIANTES) {
            throw new Exception("Un acudiente no puede tener mas de 5 estudiantes");
        }
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante no puede ser nulo");
        }
        estudiantes.add(estudiante);
    }

	public Estado getEstadoAprobacion(){
		return estadoAprobacion;
	}

	public void setEstadoAprobacion(Estado estado){
		this.estadoAprobacion = estado;
	}

	public Set<Estudiante> getEstudiantes(){
		return estudiantes;
	}

	public void setEstudiantes(Set<Estudiante> estudiantes) {
		if (estudiantes == null) {
			this.estudiantes = new HashSet<>();
		} else {
			this.estudiantes = estudiantes;
		}
	}

	/**
     * Método para verificar si se pueden agregar más estudiantes
     */
    public boolean puedeAgregarMasEstudiantes() {
        if (estudiantes == null) {
            return true;
        }
        return estudiantes.size() < MAX_ESTUDIANTES;
    }
    
    /**
     * Obtiene el número de cupos restantes
     */
    public int obtenerCuposRestantes() {
        if (estudiantes == null) {
            return MAX_ESTUDIANTES;
        }
        return Math.max(0, MAX_ESTUDIANTES - estudiantes.size());
    }
}