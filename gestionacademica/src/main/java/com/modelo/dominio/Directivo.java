package com.modelo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity(name = "directivo")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Directivo extends Usuario{
    public Directivo(Integer idUsuario, String nuipUsuario, String primerNombre, String segundoNombre, 
                    String primerApellido, String segundoApellido, int edad, 
                    String correoElectronico, String telefono, TokenUsuario tokenAccess) {
        super(idUsuario, nuipUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, 
              edad, correoElectronico, telefono, tokenAccess);
    }

    public Directivo() {
    }

    @Override
    public boolean requiereTokenAutomatico() {
        return true;
    }
}
