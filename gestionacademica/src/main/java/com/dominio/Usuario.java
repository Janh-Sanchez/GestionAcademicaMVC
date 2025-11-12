package com.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idUsuario;

    @Basic
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerNombre;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = true, length = 30)
    private String segundoNombre;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerApellido;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = true, length = 30)
    private String segundoApellido;

    @Email
    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Min(18)
    @Max(80)
    @Column(nullable = false)
    private int edad;

    @Pattern(regexp = "^[0-9]{10}$", message = "Debe tener 10 dígitos numéricos")
    @Column(nullable = false, unique = true, length = 10)
    private String telefono;

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "tokenAccess", referencedColumnName = "idToken", nullable = true)
    private TokenUsuario tokenAccess;

    public Usuario() {}
}