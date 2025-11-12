package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Map;
import java.util.Set;
@Entity
public class Grado {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idGrado;

    @NotBlank
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9 ÁÉÍÓÚáéíóúñÑ-]+$", message = "Solo letras, números y espacios")
    @Column(nullable = false, length = 50)
    private String nombreGrado;

	@OneToMany
    private Set<BibliotecaLogros> bibliotecaLogros;

    @OneToMany(mappedBy = "grado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Grupo> grupos;

    public Grado(){

    }

    public void agregarGrupo(Grupo grupo){

    }

    public void eliminarGrupo(Grupo grupo){

    }

    public Map<String, Grupo> obtenerGrupos(){
        return null;
    }
}//end Grado