package com.modelo.dominio;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "permiso")
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    // No utilizar los roles ya que no hace falta que los permisos sepan que roles los usan
    @ManyToMany(mappedBy = "permisos")
    private Set<Rol> roles = new HashSet<>();

    public Permiso(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Permiso() {
    }

    public boolean esValido() {
        return nombre != null && !nombre.trim().isEmpty() 
            && nombre.length() >= 3 && nombre.length() <= 50
            && descripcion != null && !descripcion.trim().isEmpty()
            && descripcion.length() >= 10 && descripcion.length() <= 200;
    }

    public Integer getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Integer idPermiso) { this.idPermiso = idPermiso; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}