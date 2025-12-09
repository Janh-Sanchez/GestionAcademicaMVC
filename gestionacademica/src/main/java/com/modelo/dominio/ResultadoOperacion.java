package com.modelo.dominio;

public class ResultadoOperacion {
    private final boolean exitoso;
    private final String mensaje;
    private final String campoError; // Para errores de validación
    private final Object datos; // Datos adicionales opcionales
    
    private ResultadoOperacion(boolean exitoso, String mensaje, String campoError, Object datos) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.campoError = campoError;
        this.datos = datos;
    }
    
    public static ResultadoOperacion exito(String mensaje) {
        return new ResultadoOperacion(true, mensaje, null, null);
    }
    
    public static ResultadoOperacion exitoConDatos(String mensaje, Object datos) {
        return new ResultadoOperacion(true, mensaje, null, datos);
    }
    
    public static ResultadoOperacion error(String mensaje) {
        return new ResultadoOperacion(false, mensaje, null, null);
    }
    
    public static ResultadoOperacion errorValidacion(String campo, String mensaje) {
        return new ResultadoOperacion(false, mensaje, campo, null);
    }
    
    public boolean isExitoso() { return exitoso; }
    public String getMensaje() { return mensaje; }
    public String getCampoError() { return campoError; }
    public Object getDatos() { return datos; }
}
