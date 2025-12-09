package com.modelo.dominio;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nuip_usuario", nullable = false, unique = true)
    private String nuipUsuario;

    @Basic
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerNombre;

    @Basic
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = true, length = 30)
    private String segundoNombre;

    @Basic
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = false, length = 30)
    private String primerApellido;

    @Basic
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ]+$", message = "El nombre no debe contener números")
    @Column(nullable = true, length = 30)
    private String segundoApellido;

    @Min(18)
    @Max(80)
    @Column(nullable = false)
    private Integer edad;

    @Email
    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Pattern(regexp = "^[0-9]{10}$", message = "Debe tener 10 dígitos numéricos")
    @Column(nullable = false, unique = true, length = 10)
    private String telefono;

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "token_access", referencedColumnName = "id_token", nullable = true)
    private TokenUsuario tokenAccess;

    public Usuario(Integer idUsuario, String nuipUsuario, String primerNombre, String segundoNombre, String primerApellido,
            String segundoApellido, int edad, String correoElectronico, String telefono,
            TokenUsuario tokenAccess){
        this.idUsuario = idUsuario;
        this.nuipUsuario = nuipUsuario;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.edad = edad;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.tokenAccess = tokenAccess;
    }

    public Usuario() {
    }

	public String obtenerNombreCompleto() {
        StringBuilder nombre = new StringBuilder(primerNombre);
        if (segundoNombre != null && !segundoNombre.isEmpty()) {
            nombre.append(" ").append(segundoNombre);
        }
        nombre.append(" ").append(primerApellido);
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            nombre.append(" ").append(segundoApellido);
        }
        return nombre.toString();
    }

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNuipUsuario() { return nuipUsuario; }
    public void setNuipUsuario(String nuipUsuario) { this.nuipUsuario = nuipUsuario; }
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public TokenUsuario getTokenAccess() { return tokenAccess; }
    public void setTokenAccess(TokenUsuario tokenAccess) { this.tokenAccess = tokenAccess; }
}
