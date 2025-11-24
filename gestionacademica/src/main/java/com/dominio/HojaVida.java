package com.dominio;

public class HojaVida {
    // Quizas agregar acudiente
    private Integer idHojaVida;
    private Estudiante estudiante;
    private String alergias;
    private String aspectosRelevantes;
    private String enfermedades;

    public HojaVida() {
    }

    public HojaVida(Integer idHojaVida, Estudiante estudiante, String alergias, String aspectosRelevantes, String enfermedades) {
        this.idHojaVida = idHojaVida;
        this.estudiante = estudiante;
        this.alergias = alergias;
        this.aspectosRelevantes = aspectosRelevantes;
        this.enfermedades = enfermedades;
    }

    public Integer getIdHojaVida() {
        return idHojaVida;
    }

    public void setIdHojaVida(Integer idHojaVida) {
        this.idHojaVida = idHojaVida;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getAspectosRelevantes() {
        return aspectosRelevantes;
    }

    public void setAspectosRelevantes(String aspectosRelevantes) {
        this.aspectosRelevantes = aspectosRelevantes;
    }

    public String getEnfermedades() {
        return enfermedades;
    }

    public void setEnfermedades(String enfermedades) {
        this.enfermedades = enfermedades;
    }

    public void generarHojaDeVida() {
        // Método de negocio pendiente de implementación
    }
}