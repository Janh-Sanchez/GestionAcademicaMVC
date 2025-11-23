package com.persistencia.entidades;

import jakarta.persistence.*;

@Entity
public class Profesor extends UsuarioEntity {

	@OneToOne(mappedBy = "profesor", fetch = FetchType.LAZY, optional = true)
	private Grupo grupoAsignado;

	public Profesor(){

	}
}//end Profesor