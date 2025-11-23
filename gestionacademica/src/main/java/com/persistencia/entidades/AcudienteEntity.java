package com.persistencia.entidades;

import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class AcudienteEntity {
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Estado estadoAprobacion = Estado.Pendiente;

	@OneToMany(mappedBy = "acudiente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Estudiante> estudiantes;

	public AcudienteEntity(){
	}
}
