package com.modelo.dominio;

import jakarta.persistence.*;

@Entity(name = "administrador")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Administrador extends Usuario{
}
