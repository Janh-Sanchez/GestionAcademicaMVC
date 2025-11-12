package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Entity
public class BibliotecaLogros {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idBibliotecaLogros;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, length = 200)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado", referencedColumnName = "idGrado")
    private Grado grado;

    @OneToMany(mappedBy = "bibliotecaLogros", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Logro> logros;

    public BibliotecaLogros(){

    }

    public void añadirLogro(Logro logro){

    }

    public void eliminarLogro(Logro logro){

    }
}//end BibliotecaLogros