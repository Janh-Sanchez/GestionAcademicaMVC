package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class Citacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idCitacion;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaCitacion;

    @ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "citacion_acudiente",
		joinColumns = @JoinColumn(name = "idCitacion"),
		inverseJoinColumns = @JoinColumn (name = "idAcudiente")
	)
    private Set<Acudiente> acudientes;

    @OneToMany(mappedBy = "citacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Notificacion> notificaciones;

    public Citacion(){

    }

    public void agregarAcudiente(Acudiente acudiente){

    }

    public void generarNotificacion(Acudiente acudientes){

    }
}//end Citacion