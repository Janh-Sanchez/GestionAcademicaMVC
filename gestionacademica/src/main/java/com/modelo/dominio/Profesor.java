package com.modelo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity(name = "profesor")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Profesor extends Usuario{
    @OneToOne(mappedBy = "profesor", fetch = FetchType.LAZY, optional = true)
    private Grupo grupoAsignado;

	public Profesor(Integer idUsuario, String nuipUsuario, String primerNombre, String segundoNombre, String primerApellido, 
        String segundoApellido, int edad, String correoElectronico, String telefono, TokenUsuario tokenAccess,
        Grupo grupoAsignado){
		super(idUsuario, nuipUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, edad, correoElectronico, telefono, tokenAccess);
        this.grupoAsignado = grupoAsignado;
	}

    public Profesor(){
        super();
    }

    public Grupo getGrupo(){
        return grupoAsignado;
    }

    public boolean tieneGrupoAsignado(Grupo grupo){
        if(grupoAsignado != null){
            return true;
        }
        return false;
    }

    public void setGrupo(Grupo grupo){
        if(tieneGrupoAsignado(grupo)){
            System.out.println("El profesor ya tiene un grupo asignado");
        }
        else{
            this.grupoAsignado = grupo;
        }
    }
}
