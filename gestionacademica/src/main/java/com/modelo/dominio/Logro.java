package com.modelo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity(name = "logro")
public class Logro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro")
    private Integer idLogro;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biblioteca_logros", referencedColumnName = "id_biblioteca_logros")
    private BibliotecaLogros bibliotecaLogros;

    public Logro(Integer idLogro, String descripcion, BibliotecaLogros bibliotecaLogros) {
        this.idLogro = idLogro;
        this.descripcion = descripcion;
        this.bibliotecaLogros = bibliotecaLogros;
    }

    public Logro(){}

    public Integer getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(Integer idLogro) {
        this.idLogro = idLogro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BibliotecaLogros getBibliotecaLogros() {
        return bibliotecaLogros;
    }

    public void setBibliotecaLogros(BibliotecaLogros bibliotecaLogros) {
        this.bibliotecaLogros = bibliotecaLogros;
    }

    public boolean esValida(){
        return (descripcion.length() >= 10 && descripcion.length() <= 200);
    }
}