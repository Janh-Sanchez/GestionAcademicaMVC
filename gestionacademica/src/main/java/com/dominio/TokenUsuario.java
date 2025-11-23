package com.dominio;

public class TokenUsuario {
    private Integer idToken;
    private String nombreUsuario;
    private String contrasena;
    private boolean estado;
    private Rol rol;

    public TokenUsuario(String nombreUsuario, String contrasena, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = true;
    }

    public boolean esActivo() {
        return this.estado;
    }

    public Integer getIdToken() { return idToken; }
    public void setIdToken(Integer idToken) { this.idToken = idToken; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}