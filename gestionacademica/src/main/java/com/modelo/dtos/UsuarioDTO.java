// Archivo: UsuarioDTO.java
package com.modelo.dtos;

/**
 * DTO para transferir datos de usuario entre Vista y Controlador
 * Usado en CU 2.2 (Crear usuario)
 */
public class UsuarioDTO {
    // Datos básicos del usuario
    public String nuip;
    public String primerNombre;
    public String segundoNombre;
    public String primerApellido;
    public String segundoApellido;
    public Integer edad;
    public String correoElectronico;
    public String telefono;
    
    // Rol para generar token automático y determinar tipo de usuario
    public String nombreRol;
    
    public UsuarioDTO() {
    }
    
    // Constructor completo
    public UsuarioDTO(String nuip, String primerNombre, String segundoNombre,
                     String primerApellido, String segundoApellido, Integer edad,
                     String correoElectronico, String telefono, String nombreRol) {
        this.nuip = nuip;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.edad = edad;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.nombreRol = nombreRol;
    }
}