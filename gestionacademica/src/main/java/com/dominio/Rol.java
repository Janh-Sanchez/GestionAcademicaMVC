package com.dominio;

import java.util.HashSet;
import java.util.Set;
public class Rol {
    private Integer idRol;
    private String nombre;
    private Set<Permiso> permisos;

    public Rol(String nombre) {
        this.nombre = nombre;
        this.permisos = new HashSet<>();
    }

    public boolean agregarPermiso(Permiso permiso) {
        if (permiso != null && permiso.esValido() && !permisos.contains(permiso)) {
            return permisos.add(permiso);
        }
        return false;
    }

    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
}