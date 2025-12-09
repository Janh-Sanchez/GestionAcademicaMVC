package com.modelo.dominio;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "grado")
public class Grado{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grado")
    private Integer idGrado;

    @NotBlank
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9 ÁÉÍÓÚáéíóúñÑ-]+$", message = "Solo letras, números y espacios")
    @Column(nullable = false, length = 50)
    private String nombreGrado;

    @OneToMany(mappedBy = "grado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BibliotecaLogros> bibliotecaLogros;

    @OneToMany(mappedBy = "grado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Grupo> grupos;

    public Grado(Integer idGrado, String nombreGrado, Set<BibliotecaLogros> bibliotecaLogros, Set<Grupo> grupos) {
        this.idGrado = idGrado;
        this.nombreGrado = nombreGrado;
        this.bibliotecaLogros = bibliotecaLogros;
        this.grupos = grupos;
    }

    public Grado() {
        //TODO Auto-generated constructor stub
    }

    public Integer getIdGrado() {
        return idGrado;
    }

    public void setIdGrado(Integer idGrado) {
        this.idGrado = idGrado;
    }

    public String getNombreGrado() {
        return nombreGrado;
    }

    public void setNombreGrado(String nombreGrado) {
        this.nombreGrado = nombreGrado;
    }

    public Set<BibliotecaLogros> getBibliotecaLogros() {
        return bibliotecaLogros;
    }

    public void setBibliotecaLogros(Set<BibliotecaLogros> bibliotecaLogros) {
        this.bibliotecaLogros = bibliotecaLogros;
    }

    public Set<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(Set<Grupo> grupos) {
        this.grupos = grupos;
    }


    public void agregarGrupo(Grupo grupo){
        grupos.add(grupo);
    }

    public void eliminarGrupo(Grupo grupo){
        grupos.remove(grupo);
    }
}
