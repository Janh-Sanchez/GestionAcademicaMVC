package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Entity
public class Acudiente extends Usuario {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Estado estadoAprobacion = Estado.Pendiente;

	@OneToMany(mappedBy = "acudiente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Estudiante> estudiantes;

	public Acudiente(){

	}

	public void agregarEstudiante(){

	}
}//end Acudiente