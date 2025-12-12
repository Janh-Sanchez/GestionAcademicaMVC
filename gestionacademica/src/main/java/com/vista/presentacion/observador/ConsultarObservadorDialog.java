package com.vista.presentacion.observador;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

import com.controlador.GestionObservadorController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Observacion;
import com.modelo.dominio.Observador;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Diálogo para consultar el observador de un estudiante
 * Reutilizable para Profesor, Acudiente y Directivo
 */
public class ConsultarObservadorDialog extends JDialog {
    private Estudiante estudiante;
    private GestionObservadorController controller;
    private boolean puedeModificar;
    private Profesor profesorActual; // Solo se usa si puede modificar
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public ConsultarObservadorDialog(Frame parent, Estudiante estudiante, 
                                    GestionObservadorController controller,
                                    boolean puedeModificar, Profesor profesorActual) {
        super(parent, "Consultar Observador", true);
        this.estudiante = estudiante;
        this.controller = controller;
        this.puedeModificar = puedeModificar;
        this.profesorActual = profesorActual;
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);

        // Panel superior con encabezado
        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);

        // Panel central con observaciones
        panelPrincipal.add(crearPanelObservaciones(), BorderLayout.CENTER);

        // Panel inferior con botones
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("Observador");
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

    private JScrollPane crearPanelObservaciones() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setBackground(CF);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Obtener observador
        ResultadoOperacion resultado = controller.obtenerObservadorDeEstudiante(estudiante.getIdEstudiante());

        if (!resultado.isExitoso()) {
            // No tiene observaciones
            JLabel lblSinObservaciones = new JLabel("Sin observaciones por el momento");
            lblSinObservaciones.setFont(new Font("Arial", Font.ITALIC, 14));
            lblSinObservaciones.setForeground(Color.GRAY);
            lblSinObservaciones.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenedor.add(Box.createVerticalStrut(50));
            panelContenedor.add(lblSinObservaciones);
        } else {
            Observador observador = (Observador) resultado.getDatos();
            
            if (observador.getObservaciones().isEmpty()) {
                JLabel lblSinObservaciones = new JLabel("Sin observaciones por el momento");
                lblSinObservaciones.setFont(new Font("Arial", Font.ITALIC, 14));
                lblSinObservaciones.setForeground(Color.GRAY);
                lblSinObservaciones.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelContenedor.add(Box.createVerticalStrut(50));
                panelContenedor.add(lblSinObservaciones);
            } else {
                // Mostrar observaciones
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                for (Observacion obs : observador.getObservaciones()) {
                    panelContenedor.add(crearPanelObservacion(obs, formatter));
                    panelContenedor.add(Box.createVerticalStrut(10));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(panelContenedor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        return scrollPane;
    }

    private JPanel crearPanelObservacion(Observacion obs, DateTimeFormatter formatter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));

        // Fecha
        JLabel lblFecha = new JLabel("Observación " + obs.getFechaObservacion().format(formatter));
        lblFecha.setFont(new Font("Arial", Font.BOLD, 12));
        lblFecha.setForeground(CT);
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblFecha);

        panel.add(Box.createVerticalStrut(8));

        // Descripción
        JTextArea txtDescripcion = new JTextArea(obs.getDescripcion());
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDescripcion.setForeground(CT);
        txtDescripcion.setBackground(Color.WHITE);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtDescripcion);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        if (puedeModificar) {
            JButton btnModificar = crearBoton("MODIFICAR OBSERVADOR");
            btnModificar.addActionListener(e -> abrirModificarObservador());
            panel.add(btnModificar);
        }

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
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(CB);
            }
        });
        
        return boton;
    }

    private void abrirModificarObservador() {
        ModificarObservadorDialog dialogo = new ModificarObservadorDialog(
            (Frame) getParent(), estudiante, controller, profesorActual);
        dialogo.setVisible(true);
        
        // Recargar este diálogo después de modificar
        dispose();
        ConsultarObservadorDialog nuevoDialogo = new ConsultarObservadorDialog(
            (Frame) getParent(), estudiante, controller, puedeModificar, profesorActual);
        nuevoDialogo.setVisible(true);
    }
}