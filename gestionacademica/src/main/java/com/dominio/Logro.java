package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idLogro;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bibliotecaLogros", referencedColumnName = "idBibliotecaLogros")
    private BibliotecaLogros bibliotecaLogros;

    public Logro(){

    }
}//end Logro