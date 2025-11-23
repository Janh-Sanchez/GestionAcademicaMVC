package com.dominio;

import java.util.Set;

import com.persistencia.entidades.Estado;
import com.persistencia.entidades.Estudiante;

public class Acudiente extends Usuario {

	private Estado estadoAprobacion = Estado.Pendiente;
	private Set<Estudiante> estudiantes;

	public Acudiente(Integer idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String correoElectronico, int edad, String telefono, TokenUsuario tokenAccess){
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, correoElectronico, edad, telefono, tokenAccess);
	}

	public void agregarEstudiante(Estudiante estudiante) throws Exception{
		if(this.estudiantes.size() >= 5){
			throw new Exception("Un acudiente no puede tener mas de 5 estudiantes");
		}
		if(estudiante == null){
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
}//end Acudiente