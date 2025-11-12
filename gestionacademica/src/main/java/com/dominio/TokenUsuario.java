package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class TokenUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idToken;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @NotBlank
    @Size(min = 8, max = 60)
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false)
    private boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol")
    private Rol rol;

    @OneToOne(mappedBy = "tokenAccess", fetch = FetchType.LAZY)
    private Usuario usuario;

    public TokenUsuario(){

    }
}//end TokenUsuario