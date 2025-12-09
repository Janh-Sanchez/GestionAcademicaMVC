package com.modelo.dtos;

public class EstudianteDTO {
    /**
     * DTO para transportar datos del formulario de estudiante
     */
    public String primerNombre;
    public String segundoNombre;
    public String primerApellido;
    public String segundoApellido;
    public Integer edad;
    public String nuip;
    public String nombreGrado;
    
    public EstudianteDTO(String primerNombre, String segundoNombre,
                                String primerApellido, String segundoApellido,
                                Integer edad, String nuip, String nombreGrado) {
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.edad = edad;
        this.nuip = nuip;
        this.nombreGrado = nombreGrado;
    }
}
