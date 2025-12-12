package com.vista.presentacion.grupo;

import javax.swing.*;

import com.vista.presentacion.AsignarProfesoresPanel;

import java.awt.*;

/**
 * Panel intermedio para que el directivo elija entre consultar grupos
 * o administrar la asignación de profesores
 */
public class MenuGruposDirectivoPanel extends JFrame {
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    public MenuGruposDirectivoPanel() {
        inicializarComponentes();
    }
    
    private void inicializarComponentes() {
        setTitle("Administrar Grupos - Sistema de Gestión Académica");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(CF);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Panel superior con título
        panelPrincipal.add(crearPanelTitulo(), BorderLayout.NORTH);
        
        // Panel central con opciones
        panelPrincipal.add(crearPanelOpciones(), BorderLayout.CENTER);
        
        // Panel inferior con botón volver
        panelPrincipal.add(crearPanelInferior(), BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitulo = new JLabel("ADMINISTRAR GRUPOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(CT);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel lblSubtitulo = new JLabel("Seleccione la acción que desea realizar");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setForeground(CT);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblSubtitulo);
        
        return panel;
    }
    
    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 20, 20));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Botón para Asignar Profesores
        JButton btnAsignarProfesores = crearBotonOpcion(
            "ASIGNAR PROFESORES A GRUPOS",
            "👥",
            "Asignar o cambiar profesores responsables de cada grupo",
            e -> abrirAsignarProfesores()
        );
        panel.add(btnAsignarProfesores);
        
        // Botón para Consultar Grupos
        JButton btnConsultarGrupos = crearBotonOpcion(
            "CONSULTAR GRUPOS",
            "📋",
            "Consultar la lista de estudiantes de cada grupo",
            e -> abrirConsultarGrupos()
        );
        panel.add(btnConsultarGrupos);
        
        return panel;
    }
    
    private JButton crearBotonOpcion(String titulo, String icono, String descripcion, 
                                    java.awt.event.ActionListener accion) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(10, 10));
        boton.setBackground(CB);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Panel superior con icono y título
        JPanel panelSuperior = new JPanel(new BorderLayout(15, 0));
        panelSuperior.setBackground(CB);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        
        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblIcono.setForeground(CT);
        panelSuperior.add(lblIcono, BorderLayout.WEST);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(CT);
        panelSuperior.add(lblTitulo, BorderLayout.CENTER);
        
        boton.add(panelSuperior, BorderLayout.NORTH);
        
        // Descripción
        JLabel lblDescripcion = new JLabel("<html><center>" + descripcion + "</center></html>");
        lblDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDescripcion.setForeground(CT);
        lblDescripcion.setHorizontalAlignment(SwingConstants.CENTER);
        lblDescripcion.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        boton.add(lblDescripcion, BorderLayout.CENTER);
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(CBH);
                panelSuperior.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(CB);
                panelSuperior.setBackground(CB);
            }
        });
        
        boton.addActionListener(accion);
        
        return boton;
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton btnVolver = crearBoton("Volver al Menú Principal", "🔙");
        btnVolver.addActionListener(e -> dispose());
        panel.add(btnVolver);
        
        return panel;
    }
    
    private JButton crearBoton(String texto, String icono) {
        JButton boton = new JButton(icono + " " + texto);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setBackground(CB);
        boton.setForeground(CT);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(200, 40));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(CB);
            }
        });
        
        return boton;
    }
    
    private void abrirAsignarProfesores() {
        // Abrir panel para asignar profesores (funcionalidad original)
        AsignarProfesoresPanel panel = new AsignarProfesoresPanel();
        panel.setVisible(true);
        dispose(); // Cerrar este menú intermedio
    }
    
    private void abrirConsultarGrupos() {
        // Abrir panel para consultar grupos (nueva funcionalidad)
        SeleccionarGrupoDirectivoPanel panel = new SeleccionarGrupoDirectivoPanel();
        panel.setVisible(true);
        dispose(); // Cerrar este menú intermedio
    }
}