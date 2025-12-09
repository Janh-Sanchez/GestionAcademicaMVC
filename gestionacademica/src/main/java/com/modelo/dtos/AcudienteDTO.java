package com.modelo.dtos;

public class AcudienteDTO {
    // DTOs para pasar informacion (Quizas podria crear una clase aparte para cada uno)
    public String nuip;
    public String primerNombre;
    public String segundoNombre;
    public String primerApellido;
    public String segundoApellido;
    public Integer edad;
    public String correoElectronico;
    public String telefono;
    
    public AcudienteDTO(String nuip, String primerNombre, String segundoNombre,
                                String primerApellido, String segundoApellido, Integer edad,
                                String correoElectronico, String telefono) {
        this.nuip = nuip;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.edad = edad;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
    }
}
