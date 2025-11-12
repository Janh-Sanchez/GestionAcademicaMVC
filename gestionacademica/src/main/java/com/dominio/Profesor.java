package com.dominio;

import jakarta.persistence.*;

@Entity
public class Profesor extends Usuario {

	@OneToOne(mappedBy = "profesor", fetch = FetchType.LAZY, optional = true)
	private Grupo grupoAsignado;

	public Profesor(){

	}
}//end Profesor