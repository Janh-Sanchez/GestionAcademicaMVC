package com.modelo;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Servicio para envío de correos electrónicos
 * Utiliza Java Mail API para enviar credenciales a usuarios
 */
public class ServicioCorreo {
    
    // Configuración del servidor SMTP (Gmail como ejemplo)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "prueba"; // CAMBIAR
    private static final String EMAIL_PASSWORD = "pruebaContraseña"; // CAMBIAR (usar contraseña de aplicación)
    
    /**
     * Envía credenciales de acceso por correo electrónico
     * 
     * @param destinatario Correo electrónico del destinatario
     * @param nombreCompleto Nombre completo del usuario
     * @param nombreUsuario Usuario generado
     * @param contrasena Contraseña generada
     * @param tipoUsuario Tipo de usuario (Profesor, Directivo, Acudiente)
     * @return true si el correo se envió exitosamente
     */
    public static boolean enviarCredenciales(
            String destinatario, 
            String nombreCompleto,
            String nombreUsuario, 
            String contrasena,
            String tipoUsuario) {
        
        try {
            // Configurar propiedades del servidor SMTP
            Properties props = configurarPropiedades();
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            // Crear mensaje
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(EMAIL_FROM, "Sistema Académico"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("Credenciales de Acceso - Sistema Académico");
            
            // Contenido del correo en HTML
            String contenidoHTML = construirContenidoHTML(nombreCompleto, nombreUsuario, contrasena, tipoUsuario);
            mensaje.setContent(contenidoHTML, "text/html; charset=utf-8");
            
            // Enviar correo
            Transport.send(mensaje);
            
            System.out.println("✓ Correo enviado exitosamente a: " + destinatario);
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Error al enviar correo a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Configura las propiedades del servidor SMTP
     */
    private static Properties configurarPropiedades() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }
    
    /**
     * Construye el contenido HTML del correo
     */
    private static String construirContenidoHTML(
            String nombreCompleto, 
            String nombreUsuario, 
            String contrasena,
            String tipoUsuario) {
        
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               ".header { background-color: #FFD4A0; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
               ".content { background-color: #fff; padding: 30px; border: 1px solid #ddd; }" +
               ".credentials { background-color: #f9f9f9; padding: 20px; margin: 20px 0; border-left: 4px solid #FFD4A0; }" +
               ".credentials-item { margin: 10px 0; }" +
               ".credentials-label { font-weight: bold; color: #555; }" +
               ".credentials-value { font-family: 'Courier New', monospace; font-size: 16px; color: #000; padding: 5px 10px; background-color: #fff; border: 1px solid #ddd; display: inline-block; margin-left: 10px; }" +
               ".warning { background-color: #fff3cd; padding: 15px; margin: 20px 0; border-left: 4px solid #ffc107; }" +
               ".footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               
               "<div class='header'>" +
               "<h1 style='margin:0; color: #3A2E2E;'>Sistema Académico</h1>" +
               "<p style='margin:5px 0 0 0; color: #3A2E2E;'>Credenciales de Acceso</p>" +
               "</div>" +
               
               "<div class='content'>" +
               "<p>Estimado/a <strong>" + nombreCompleto + "</strong>,</p>" +
               
               "<p>¡Bienvenido/a al Sistema de Gestión Académica!</p>" +
               
               "<p>Su cuenta de <strong>" + tipoUsuario + "</strong> ha sido creada exitosamente. " +
               "A continuación encontrará sus credenciales de acceso:</p>" +
               
               "<div class='credentials'>" +
               "<div class='credentials-item'>" +
               "<span class='credentials-label'>👤 Usuario:</span>" +
               "<span class='credentials-value'>" + nombreUsuario + "</span>" +
               "</div>" +
               "<div class='credentials-item'>" +
               "<span class='credentials-label'>🔒 Contraseña:</span>" +
               "<span class='credentials-value'>" + contrasena + "</span>" +
               "</div>" +
               "</div>" +
               
               "<div class='warning'>" +
               "<strong>⚠️ Importante:</strong>" +
               "<ul style='margin: 10px 0;'>" +
               "<li>Guarde estas credenciales en un lugar seguro</li>" +
               "<li>No comparta su contraseña con nadie</li>" +
               "<li>Se recomienda cambiar su contraseña después del primer ingreso</li>" +
               "</ul>" +
               "</div>" +
               
               "<p>Puede acceder al sistema utilizando estas credenciales.</p>" +
               
               "<p>Si tiene alguna duda o problema para acceder, por favor contacte al administrador del sistema.</p>" +
               
               "<p>Saludos cordiales,<br><strong>Equipo de Administración</strong></p>" +
               "</div>" +
               
               "<div class='footer'>" +
               "<p>Este es un correo automático, por favor no responder.</p>" +
               "<p>© 2025 Sistema de Gestión Académica. Todos los derechos reservados.</p>" +
               "</div>" +
               
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Método para probar la configuración del servicio de correo
     */
    public static boolean probarConfiguracion() {
        try {
            Properties props = configurarPropiedades();
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            // Intenta conectarse al servidor
            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, EMAIL_FROM, EMAIL_PASSWORD);
            transport.close();
            
            System.out.println("✓ Configuración de correo válida");
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Error en configuración de correo: " + e.getMessage());
            return false;
        }
    }
}