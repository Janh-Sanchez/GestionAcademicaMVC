package com.modelo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "token_usuario")
public class TokenUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    public TokenUsuario(Integer idToken, String nombreUsuario, String contrasena, Rol rol) {
        this.idToken = idToken;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public TokenUsuario() {
    }

    // Verificar credenciales
    public boolean verificarCredenciales(String contrasenaPrueba) {

        if (contrasenaPrueba == null || contrasenaPrueba.isEmpty()) {
            return false;
        }
        
        boolean credencialesCorrectas = this.contrasena.equals(contrasenaPrueba);
        return credencialesCorrectas;
    }

    // Getters y Setters
    public Integer getIdToken() { return idToken; }
    public void setIdToken(Integer idToken) { this.idToken = idToken; }
    
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

}