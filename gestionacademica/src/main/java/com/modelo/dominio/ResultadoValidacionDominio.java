package com.modelo.dominio;

public class ResultadoValidacionDominio {
    private final boolean valido;
    private final String campoInvalido;
    private final String mensajeError;
    
    private ResultadoValidacionDominio(boolean valido, String campo, String mensaje) {
        this.valido = valido;
        this.campoInvalido = campo;
        this.mensajeError = mensaje;
    }
    
    public static ResultadoValidacionDominio exito() {
        return new ResultadoValidacionDominio(true, null, null);
    }
    
    public static ResultadoValidacionDominio error(String campo, String mensaje) {
        return new ResultadoValidacionDominio(false, campo, mensaje);
    }
    
    public boolean isValido() { return valido; }
    public String getCampoInvalido() { return campoInvalido; }
    public String getMensajeError() { return mensajeError; }
}

