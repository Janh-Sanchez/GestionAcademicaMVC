package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Entity
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idRol;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "rol_permiso", // nombre de la tabla intermedia
		joinColumns = @JoinColumn(name = "idRol"), // FK hacia Rol
		inverseJoinColumns = @JoinColumn(name = "idPermiso") // FK hacia Permiso
	)
	private Set<Permiso> permisos;

    public Rol(){

    }

    public void agregarPermiso(Permiso permiso){

    }

    public boolean tienePermiso(Permiso permiso){
        return false;
    }
}//end Rol