package com.aplicacion;

import javax.swing.SwingUtilities;

import com.vista.presentacion.LoginFrame;

public class Main {
    public static void main(String[] args) {
        // Iniciar aplicación - MUCHO MÁS SIMPLE
        SwingUtilities.invokeLater(() -> {
            // Solo creamos el LoginFrame, él crea su controlador internamente
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // Agregar shutdown hook para cerrar recursos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            JPAUtil.shutdown();
        }));
    }
}