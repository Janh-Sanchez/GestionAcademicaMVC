package com.vista.presentacion.hojavida;

import java.awt.*;
import javax.swing.*;

import com.controlador.GestionHojaVidaController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.HojaVida;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Diálogo para consultar la hoja de vida de un estudiante
 * Reutilizable para Profesor, Acudiente y Directivo
 */
public class ConsultarHojaVidaDialog extends JDialog {
    private Estudiante estudiante;
    private GestionHojaVidaController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public ConsultarHojaVidaDialog(Frame parent, Estudiante estudiante, 
                                   GestionHojaVidaController controller) {
        super(parent, "Consultar Hoja de Vida", true);
        this.estudiante = estudiante;
        this.controller = controller;
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setSize(550, 650);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);

        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelContenido(), BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("Hoja de Vida");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);

        panel.add(Box.createVerticalStrut(10));

        JLabel lblEstudiante = new JLabel(estudiante.obtenerNombreCompleto());
        lblEstudiante.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEstudiante.setForeground(Color.WHITE);
        lblEstudiante.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblEstudiante);

        if (estudiante.getGrupo() != null) {
            JLabel lblGrado = new JLabel("Grado: " + estudiante.getGrupo().getGrado().getNombreGrado() + 
                                        " - " + estudiante.getGrupo().getNombreGrupo());
            lblGrado.setFont(new Font("Arial", Font.PLAIN, 12));
            lblGrado.setForeground(Color.WHITE);
            lblGrado.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblGrado);
        }

        return panel;
    }

    private JScrollPane crearPanelContenido() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Obtener hoja de vida
        ResultadoOperacion resultado = controller.obtenerHojaVidaDeEstudiante(estudiante.getIdEstudiante());

        if (!resultado.isExitoso()) {
            JLabel lblSinHoja = new JLabel("No se ha registrado la hoja de vida");
            lblSinHoja.setFont(new Font("Arial", Font.ITALIC, 14));
            lblSinHoja.setForeground(Color.GRAY);
            lblSinHoja.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(50));
            panel.add(lblSinHoja);
        } else {
            HojaVida hojaVida = (HojaVida) resultado.getDatos();
            
            // ENFERMEDADES
            panel.add(crearSeccion("ENFERMEDADES", hojaVida.getEnfermedades()));
            panel.add(Box.createVerticalStrut(20));

            // ASPECTOS RELEVANTES
            panel.add(crearSeccion("ASPECTOS RELEVANTES DE APRENDIZAJE", hojaVida.getAspectosRelevantes()));
            panel.add(Box.createVerticalStrut(20));

            // ALERGIAS
            panel.add(crearSeccion("ALERGIAS", hojaVida.getAlergias()));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        return scrollPane;
    }

    private JPanel crearSeccion(String titulo, String contenido) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Título
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitulo.setForeground(CT);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitulo);

        panel.add(Box.createVerticalStrut(8));

        // Contenido
        JTextArea txtContenido = new JTextArea(contenido != null ? contenido : "No especificado");
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 12));
        txtContenido.setForeground(CT);
        txtContenido.setBackground(Color.WHITE);
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setEditable(false);
        txtContenido.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtContenido);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton btnVolver = crearBoton("VOLVER");
        btnVolver.addActionListener(e -> dispose());
        panel.add(btnVolver);

        return panel;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setBackground(CB);
        boton.setForeground(CT);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(120, 35));
        
        return boton;
    }
}