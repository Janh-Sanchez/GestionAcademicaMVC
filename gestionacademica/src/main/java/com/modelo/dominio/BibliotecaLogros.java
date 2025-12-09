package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity(name = "biblioteca_logros")
public class BibliotecaLogros {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_biblioteca_logros")
    private Integer idBibliotecaLogros;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, length = 200)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado", referencedColumnName = "id_grado")
    private Grado grado;

    @OneToMany(mappedBy = "bibliotecaLogros", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Logro> logros;

    public BibliotecaLogros(Integer idBibliotecaLogros, String categoria, Grado grado, Set<Logro> logros) {
        this.idBibliotecaLogros = idBibliotecaLogros;
        this.categoria = categoria;
        this.grado = grado;
        this.logros = logros;
    }

    public BibliotecaLogros(){
        this.logros = new HashSet<>();
    }

    public Integer getIdBibliotecaLogros() {
        return idBibliotecaLogros;
    }

    public void setIdBibliotecaLogros(Integer idBibliotecaLogros) {
        this.idBibliotecaLogros = idBibliotecaLogros;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }

    public Set<Logro> getLogros() {
        return logros;
    }

    public void setLogros(Set<Logro> logros) {
        this.logros = logros;
    }


    public void añadirLogro(Logro logro){
        logros.add(logro);
    }
}
