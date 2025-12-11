package com.vista.presentacion.observador;

import java.awt.*;
import javax.swing.*;

import com.controlador.GestionObservadorController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Diálogo para agregar observaciones al observador de un estudiante
 * Solo accesible por profesores
 */
public class ModificarObservadorDialog extends JDialog {
    private Estudiante estudiante;
    private GestionObservadorController controller;
    private Profesor profesor;
    
    private JTextArea txtDescripcion;
    private JLabel lblContador;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    private static final int MIN_CARACTERES = 10;
    private static final int MAX_CARACTERES = 200;

    public ModificarObservadorDialog(Frame parent, Estudiante estudiante, 
                                    GestionObservadorController controller, 
                                    Profesor profesor) {
        super(parent, "Asignar Observación", true);
        this.estudiante = estudiante;
        this.controller = controller;
        this.profesor = profesor;
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setSize(450, 450);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);

        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelFormulario(), BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Botón volver
        JLabel lblVolver = new JLabel("←");
        lblVolver.setFont(new Font("Arial", Font.BOLD, 28));
        lblVolver.setForeground(CT);
        lblVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblVolver.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblVolver.setForeground(CB);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblVolver.setForeground(CT);
            }
        });
        panel.add(lblVolver, BorderLayout.WEST);

        JLabel lblTitulo = new JLabel("ASIGNAR OBSERVACIÓN");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(CT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Selector de estudiante (solo muestra el actual, no es editable)
        JLabel lblEstudiante = new JLabel("Estudiante");
        lblEstudiante.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstudiante.setForeground(CT);
        lblEstudiante.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEstudiante);

        panel.add(Box.createVerticalStrut(5));

        JTextField txtEstudiante = new JTextField(estudiante.obtenerNombreCompleto());
        txtEstudiante.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEstudiante.setEditable(false);
        txtEstudiante.setBackground(new Color(240, 240, 240));
        txtEstudiante.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtEstudiante.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtEstudiante);

        panel.add(Box.createVerticalStrut(20));

        // Descripción
        JLabel lblDescripcion = new JLabel("DESCRIPCIÓN");
        lblDescripcion.setFont(new Font("Arial", Font.BOLD, 14));
        lblDescripcion.setForeground(CT);
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDescripcion);

        panel.add(Box.createVerticalStrut(5));

        txtDescripcion = new JTextArea(5, 20);
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Listener para el contador de caracteres
        txtDescripcion.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
        });

        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrollDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollDescripcion);

        panel.add(Box.createVerticalStrut(5));

        // Contador de caracteres
        lblContador = new JLabel("0/" + MAX_CARACTERES + " caracteres (mínimo " + MIN_CARACTERES + ")");
        lblContador.setFont(new Font("Arial", Font.PLAIN, 11));
        lblContador.setForeground(Color.GRAY);
        lblContador.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblContador);

        return panel;
    }

    private void actualizarContador() {
        int longitud = txtDescripcion.getText().length();
        lblContador.setText(longitud + "/" + MAX_CARACTERES + " caracteres (mínimo " + MIN_CARACTERES + ")");
        
        if (longitud < MIN_CARACTERES) {
            lblContador.setForeground(Color.RED);
        } else if (longitud > MAX_CARACTERES) {
            lblContador.setForeground(Color.RED);
        } else {
            lblContador.setForeground(new Color(0, 150, 0));
        }
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton btnGuardar = crearBoton("Guardar");
        btnGuardar.addActionListener(e -> guardarObservacion());
        panel.add(btnGuardar);

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

    private void guardarObservacion() {
        String descripcion = txtDescripcion.getText().trim();
        
        // Validar longitud
        if (descripcion.length() < MIN_CARACTERES) {
            JOptionPane.showMessageDialog(this,
                "La descripción debe tener al menos " + MIN_CARACTERES + " caracteres",
                "Error de validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (descripcion.length() > MAX_CARACTERES) {
            JOptionPane.showMessageDialog(this,
                "La descripción no puede exceder " + MAX_CARACTERES + " caracteres",
                "Error de validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Guardar observación
        ResultadoOperacion resultado = controller.agregarObservacion(
            estudiante.getIdEstudiante(), descripcion, profesor);
        
        if (resultado.isExitoso()) {
            JOptionPane.showMessageDialog(this,
                "Observación guardada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al guardar observación: " + resultado.getMensaje(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}