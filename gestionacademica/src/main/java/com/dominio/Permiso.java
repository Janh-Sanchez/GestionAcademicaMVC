package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idPermiso;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Size(max = 10)
    @Column(length = 200)
    private String descripcion;

    public Permiso(){

    }

    public void esValido(){

    }
}//end Permiso