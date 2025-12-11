package com.modelo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity(name = "hoja_vida")
public class HojaVida {
    
    private static final int MIN_LONGITUD_CAMPO = 5;
    private static final int MAX_LONGITUD_CAMPO = 500;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hoja_vida")
    private Integer idHojaVida;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", unique = true)
    private Estudiante estudiante;

    @Column(name = "alergias", length = 500)
    private String alergias;
    
    @Column(name = "aspectos_relevantes", length = 500)
    private String aspectosRelevantes;
    
    @Column(name = "enfermedades", length = 500)
    private String enfermedades;

    public HojaVida() {
    }

    public HojaVida(Integer idHojaVida, Estudiante estudiante, String alergias, 
                    String aspectosRelevantes, String enfermedades) {
        this.idHojaVida = idHojaVida;
        this.estudiante = estudiante;
        this.alergias = alergias;
        this.aspectosRelevantes = aspectosRelevantes;
        this.enfermedades = enfermedades;
    }

    // ============================================
    // VALIDACIONES DE DOMINIO
    // ============================================
    
    /**
     * Valida un campo de texto de la hoja de vida
     * Los campos pueden estar vacíos (indicando "No aplica")
     * Si tienen contenido, deben cumplir con longitud mínima
     */
    public static ResultadoValidacionDominio validarCampo(
            String valor, String nombreCampo, boolean esObligatorio) {
        
        // Si es null o vacío
        if (valor == null || valor.trim().isEmpty() || valor.equalsIgnoreCase("No aplica")) {
            if (esObligatorio) {
                return ResultadoValidacionDominio.error(nombreCampo, 
                    "Campo obligatorio. Si no aplica, escriba 'No aplica'");
            }
            return ResultadoValidacionDominio.exito(); // Opcional y vacío está bien
        }
        
        // Si tiene contenido, validar longitud
        String valorTrim = valor.trim();
        
        if (valorTrim.length() < MIN_LONGITUD_CAMPO) {
            return ResultadoValidacionDominio.error(nombreCampo, 
                "Debe tener al menos " + MIN_LONGITUD_CAMPO + 
                " caracteres o escribir 'No aplica'");
        }
        
        if (valorTrim.length() > MAX_LONGITUD_CAMPO) {
            return ResultadoValidacionDominio.error(nombreCampo, 
                "No puede exceder " + MAX_LONGITUD_CAMPO + " caracteres");
        }
        
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Valida toda la hoja de vida
     */
    public ResultadoValidacionDominio validar() {
        // Validar alergias (obligatorio)
        ResultadoValidacionDominio resultado = validarCampo(
            this.alergias, "alergias", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar aspectos relevantes (obligatorio)
        resultado = validarCampo(
            this.aspectosRelevantes, "aspectosRelevantes", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        // Validar enfermedades (obligatorio)
        resultado = validarCampo(
            this.enfermedades, "enfermedades", true);
        if (!resultado.isValido()) {
            return resultado;
        }
        
        return ResultadoValidacionDominio.exito();
    }
    
    /**
     * Verifica si la hoja de vida está completa (todos los campos diligenciados)
     */
    public boolean estaCompleta() {
        return alergias != null && !alergias.trim().isEmpty() &&
               aspectosRelevantes != null && !aspectosRelevantes.trim().isEmpty() &&
               enfermedades != null && !enfermedades.trim().isEmpty();
    }

    /**
     * Normaliza los campos "No aplica" a formato estándar
     */
    private void normalizarCampos() {
        if (alergias != null && alergias.trim().isEmpty()) {
            alergias = "No aplica";
        }
        if (aspectosRelevantes != null && aspectosRelevantes.trim().isEmpty()) {
            aspectosRelevantes = "No aplica";
        }
        if (enfermedades != null && enfermedades.trim().isEmpty()) {
            enfermedades = "No aplica";
        }
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================

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
        normalizarCampos();
    }

    public String getAspectosRelevantes() {
        return aspectosRelevantes;
    }

    public void setAspectosRelevantes(String aspectosRelevantes) {
        this.aspectosRelevantes = aspectosRelevantes;
        normalizarCampos();
    }

    public String getEnfermedades() {
        return enfermedades;
    }

    public void setEnfermedades(String enfermedades) {
        this.enfermedades = enfermedades;
        normalizarCampos();
    }
}