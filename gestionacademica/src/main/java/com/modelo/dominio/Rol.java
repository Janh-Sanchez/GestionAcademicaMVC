package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_permiso",
        joinColumns = @JoinColumn(name = "id_rol"),
        inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    private Set<Permiso> permisos;

    public Rol(String nombre) {
        this.nombre = nombre;
        this.permisos = new HashSet<>();
    }

    public Rol() {
        this.permisos = new HashSet<>();
    }

    // Validar rol
    public boolean esValido() {
        return nombre != null && 
               !nombre.trim().isEmpty() && 
               nombre.length() >= 3 && 
               nombre.length() <= 50 &&
               !permisos.isEmpty();
    }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Permiso> getPermisos() { return new HashSet<>(permisos); }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
}