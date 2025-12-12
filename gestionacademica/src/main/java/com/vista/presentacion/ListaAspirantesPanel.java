package com.vista.presentacion;

import com.controlador.GestionAspirantesController;
import com.modelo.dominio.ResultadoOperacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * Panel para mostrar la lista de aspirantes y aprobar/rechazar
 * VISTA en arquitectura MVC - se comunica únicamente con el Controlador
 */
public class ListaAspirantesPanel extends JFrame {
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    private final Color VERDE = new Color(76, 175, 80);
    private final Color ROJO = new Color(244, 67, 54);
    
    private GestionAspirantesController controlador;
    private JPanel panelContenido;
    private JScrollPane scrollPane;
    
    public ListaAspirantesPanel() {
        this.controlador = new GestionAspirantesController();
        inicializarComponentes();
        cargarAspirantes();
    }
    
    private void inicializarComponentes() {
        setTitle("Lista de Aspirantes - Aprobar/Rechazar");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);
        
        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        
        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(CF);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBackground(CF);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        
        add(panelPrincipal);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblVolver = new JLabel("← ");
        lblVolver.setFont(new Font("Arial", Font.BOLD, 20));
        lblVolver.setForeground(Color.WHITE);
        lblVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblVolver.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        panel.add(lblVolver, BorderLayout.WEST);
        
        JLabel lblTitulo = new JLabel("LISTA DE ASPIRANTES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Carga la lista de aspirantes usando el controlador MVC
     */
    private void cargarAspirantes() {
        panelContenido.removeAll();
        
        ResultadoOperacion resultado = controlador.obtenerListaAspirantes();
        
        if (!resultado.isExitoso()) {
            if ("VACIA".equals(resultado.getMensaje())) {
                mostrarListaVacia();
            } else {
                mostrarError(resultado.getMensaje());
            }
            return;
        }
        
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(Color.BLACK);
        panelEncabezado.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelEncabezado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblEncabezado = new JLabel("LISTA ACTUALIZADA DE ASPIRANTES 2025 - III");
        lblEncabezado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEncabezado.setForeground(Color.WHITE);
        
        panelEncabezado.add(lblEncabezado, BorderLayout.WEST);
        panelContenido.add(panelEncabezado);
        panelContenido.add(Box.createVerticalStrut(10));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> aspirantes = (List<Map<String, Object>>) resultado.getDatos();
        
        for (Map<String, Object> aspirante : aspirantes) {
            panelContenido.add(crearPanelAspirante(aspirante));
            panelContenido.add(Box.createVerticalStrut(10));
        }
        
        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    private JPanel crearPanelAspirante(Map<String, Object> datosAspirante) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        String nombreAcudiente = (String) datosAspirante.get("nombreAcudiente");
        
        JLabel lblAcudiente = new JLabel("Acudiente: " + nombreAcudiente);
        lblAcudiente.setFont(new Font("Arial", Font.BOLD, 13));
        lblAcudiente.setForeground(CT);
        lblAcudiente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblAcudiente);
        
        panel.add(Box.createVerticalStrut(8));
        
        JLabel lblEstudiantes = new JLabel("Estudiantes:");
        lblEstudiantes.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEstudiantes.setForeground(CT);
        lblEstudiantes.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEstudiantes);
        
        panel.add(Box.createVerticalStrut(5));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> estudiantes = (List<Map<String, Object>>) datosAspirante.get("estudiantes");
        
        for (Map<String, Object> estudiante : estudiantes) {
            panel.add(crearPanelEstudiante(estudiante));
            panel.add(Box.createVerticalStrut(5));
        }
        
        return panel;
    }
    
    private JPanel crearPanelEstudiante(Map<String, Object> datosEstudiante) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        String nombreCompleto = (String) datosEstudiante.get("nombreCompleto");
        String nombreGrado = (String) datosEstudiante.get("nombreGrado");
        Integer idEstudiante = (Integer) datosEstudiante.get("idEstudiante");
        
        JLabel lblInfo = new JLabel("• " + nombreCompleto + " - " + nombreGrado);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo.setForeground(CT);
        panel.add(lblInfo, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotones.setBackground(Color.WHITE);
        
        JButton btnAprobar = crearBotonAccion("Aprobar", CB, CBH);
        btnAprobar.addActionListener(e -> aprobarEstudiante(idEstudiante, nombreCompleto));
        panelBotones.add(btnAprobar);
        
        JButton btnRechazar = crearBotonAccion("Rechazar", CB, CBH);
        btnRechazar.addActionListener(e -> rechazarEstudiante(idEstudiante, nombreCompleto));
        panelBotones.add(btnRechazar);
        
        panel.add(panelBotones, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton crearBotonAccion(String texto, Color colorNormal, Color colorHover) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setForeground(CT);
        boton.setBackground(colorNormal);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(85, 28));
        
        boton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(colorHover);
            }
            public void mouseExited(MouseEvent e) {
                boton.setBackground(colorNormal);
            }
        });
        
        return boton;
    }
    
    /**
     * Aprueba un estudiante a través del controlador MVC
     */
    private void aprobarEstudiante(Integer idEstudiante, String nombreEstudiante) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea aprobar a " + nombreEstudiante + "?",
            "Confirmar aprobación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            ResultadoOperacion resultado = controlador.aprobarEstudiante(idEstudiante);
            
            if (resultado.isExitoso()) {
                mostrarMensajeExito(resultado.getMensaje());
                cargarAspirantes();
            } else {
                mostrarError(resultado.getMensaje());
            }
        }
    }
    
    /**
     * Rechaza un estudiante a través del controlador MVC
     */
    private void rechazarEstudiante(Integer idEstudiante, String nombreEstudiante) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea rechazar a " + nombreEstudiante + "?",
            "Confirmar rechazo",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            ResultadoOperacion resultado = controlador.rechazarEstudiante(idEstudiante);
            
            if (resultado.isExitoso()) {
                mostrarMensajeExito(resultado.getMensaje());
                cargarAspirantes();
            } else {
                mostrarError(resultado.getMensaje());
            }
        }
    }
    
    private void mostrarListaVacia() {
        panelContenido.removeAll();
        
        JPanel panelMensaje = new JPanel();
        panelMensaje.setLayout(new BoxLayout(panelMensaje, BoxLayout.Y_AXIS));
        panelMensaje.setBackground(CF);
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        JLabel lblMensaje = new JLabel(
            "<html><center>En este momento no hay registros de<br>" +
            "preinscripciones, vuelva más tarde</center></html>");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMensaje.setForeground(CT);
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMensaje.add(lblMensaje);
        
        panelMensaje.add(Box.createVerticalStrut(20));
        
        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setBackground(CB);
        btnVolver.setForeground(CT);
        btnVolver.setFocusPainted(false);
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> dispose());
        panelMensaje.add(btnVolver);
        
        panelContenido.add(panelMensaje);
        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    private void mostrarError(String mensaje) {
        JDialog dialog = new JDialog(this, "Error", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(255, 235, 230));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        
        JLabel lblIcono = new JLabel("✗");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblIcono);
        
        panelContenido.add(Box.createVerticalStrut(15));
        
        JLabel lblTitulo = new JLabel("ERROR");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(ROJO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblTitulo);
        
        panelContenido.add(Box.createVerticalStrut(10));
        
        JLabel lblMensajeLabel = new JLabel("<html><center>" + mensaje + "</center></html>");
        lblMensajeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMensajeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblMensajeLabel);
        
        panelContenido.add(Box.createVerticalStrut(20));
        
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAceptar.setBackground(CB);
        btnAceptar.setForeground(CT);
        btnAceptar.setFocusPainted(false);
        btnAceptar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAceptar.addActionListener(e -> dialog.dispose());
        panelContenido.add(btnAceptar);
        
        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void mostrarMensajeExito(String mensaje) {
        JDialog dialog = new JDialog(this, "Éxito", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(230, 255, 235));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        
        JLabel lblIcono = new JLabel("✓");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        lblIcono.setForeground(VERDE);
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblIcono);
        
        panelContenido.add(Box.createVerticalStrut(15));
        
        JLabel lblTitulo = new JLabel("¡Listo!");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(VERDE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblTitulo);
        
        panelContenido.add(Box.createVerticalStrut(10));
        
        JLabel lblMensajeLabel = new JLabel("<html><center>" + mensaje + "</center></html>");
        lblMensajeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMensajeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblMensajeLabel);
        
        panelContenido.add(Box.createVerticalStrut(20));
        
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAceptar.setBackground(CB);
        btnAceptar.setForeground(CT);
        btnAceptar.setFocusPainted(false);
        btnAceptar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAceptar.addActionListener(e -> dialog.dispose());
        panelContenido.add(btnAceptar);
        
        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}