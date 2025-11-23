package com.persistencia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idNotificacion;

    @NotBlank
    @Size(min = 10, max = 300)
    @Column(nullable = false, length = 300)
    private String asunto;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario", referencedColumnName = "idUsuario")
    private Acudiente destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citacion", referencedColumnName = "idCitacion")
    private Citacion citacion;

    public Notificacion(){

    }

    public void enviar(){

    }
}//end Notificacion