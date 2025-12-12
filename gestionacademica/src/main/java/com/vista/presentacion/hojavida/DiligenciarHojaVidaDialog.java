package com.vista.presentacion.hojavida;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.controlador.GestionHojaVidaController;
import com.modelo.dominio.Acudiente;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.HojaVida;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Diálogo OBLIGATORIO para que el acudiente diligencie las hojas de vida
 * de todos sus estudiantes al iniciar sesión por primera vez
 */
public class DiligenciarHojaVidaDialog extends JDialog {
    private GestionHojaVidaController controller;
    private List<Estudiante> estudiantes;
    private int estudianteActualIndex = 0;
    private boolean todasDiligenciadas = false;
    
    // Componentes del formulario
    private JTextArea txtEnfermedades;
    private JTextArea txtAspectosRelevantes;
    private JTextArea txtAlergias;
    private JLabel lblEstudianteActual;
    private JLabel lblProgreso;
    private JButton btnSiguiente;
    private JButton btnSalir;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    private static final int MAX_CARACTERES = 500;
    private static final int MIN_CARACTERES = 5;

    public DiligenciarHojaVidaDialog(Frame parent, Acudiente acudiente, 
                                    GestionHojaVidaController controller) {
        super(parent, "Completar Hojas de Vida - Obligatorio", true);
        this.controller = controller;
        this.estudiantes = new ArrayList<>(acudiente.getEstudiantes());
        
        // Hacer que no se pueda cerrar sin completar
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        inicializarComponentes();
        cargarDatosEstudianteActual();
    }

    private void inicializarComponentes() {
        setSize(550, 700);
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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("¡Gracias por entrar a nuestra institución!");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);

        panel.add(Box.createVerticalStrut(10));

        JLabel lblSubtitulo = new JLabel("Completa los siguientes datos para la hoja de vida del estudiante.");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.WHITE);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblSubtitulo);

        JLabel lblNota = new JLabel("En caso de que no aplique por favor escribe \"No aplica\"");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 11));
        lblNota.setForeground(new Color(255, 200, 200));
        lblNota.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblNota);

        panel.add(Box.createVerticalStrut(10));

        // Nombre del estudiante actual
        lblEstudianteActual = new JLabel();
        lblEstudianteActual.setFont(new Font("Arial", Font.BOLD, 16));
        lblEstudianteActual.setForeground(CB);
        lblEstudianteActual.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblEstudianteActual);

        // Progreso
        lblProgreso = new JLabel();
        lblProgreso.setFont(new Font("Arial", Font.PLAIN, 12));
        lblProgreso.setForeground(Color.WHITE);
        lblProgreso.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblProgreso);

        return panel;
    }

    private JScrollPane crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ENFERMEDADES
        panel.add(crearLabelCampo("ENFERMEDADES *"));
        panel.add(Box.createVerticalStrut(5));
        
        txtEnfermedades = crearAreaTexto();
        JScrollPane scrollEnfermedades = new JScrollPane(txtEnfermedades);
        scrollEnfermedades.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollEnfermedades.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollEnfermedades);
        
        panel.add(Box.createVerticalStrut(5));
        panel.add(crearLabelContador(txtEnfermedades, "contador-enfermedades"));
        panel.add(Box.createVerticalStrut(20));

        // ASPECTOS RELEVANTES DE APRENDIZAJE
        panel.add(crearLabelCampo("ASPECTOS RELEVANTES DE APRENDIZAJE *"));
        panel.add(Box.createVerticalStrut(5));
        
        txtAspectosRelevantes = crearAreaTexto();
        JScrollPane scrollAspectos = new JScrollPane(txtAspectosRelevantes);
        scrollAspectos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollAspectos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollAspectos);
        
        panel.add(Box.createVerticalStrut(5));
        panel.add(crearLabelContador(txtAspectosRelevantes, "contador-aspectos"));
        panel.add(Box.createVerticalStrut(20));

        // ALERGIAS
        panel.add(crearLabelCampo("ALERGIAS *"));
        panel.add(Box.createVerticalStrut(5));
        
        txtAlergias = crearAreaTexto();
        JScrollPane scrollAlergias = new JScrollPane(txtAlergias);
        scrollAlergias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollAlergias.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollAlergias);
        
        panel.add(Box.createVerticalStrut(5));
        panel.add(crearLabelContador(txtAlergias, "contador-alergias"));
        panel.add(Box.createVerticalStrut(15));

        // Campos obligatorios
        JLabel lblObligatorio = new JLabel("(*) Campos Obligatorios");
        lblObligatorio.setFont(new Font("Arial", Font.ITALIC, 11));
        lblObligatorio.setForeground(Color.RED);
        lblObligatorio.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblObligatorio);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        return scrollPane;
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(CT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextArea crearAreaTexto() {
        JTextArea textArea = new JTextArea(4, 20);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return textArea;
    }

    private JLabel crearLabelContador(JTextArea textArea, String nombre) {
        JLabel lblContador = new JLabel("0/" + MAX_CARACTERES + " caracteres (mínimo " + MIN_CARACTERES + ")");
        lblContador.setFont(new Font("Arial", Font.PLAIN, 11));
        lblContador.setForeground(Color.GRAY);
        lblContador.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblContador.setName(nombre);
        
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            
            private void actualizar() {
                int longitud = textArea.getText().length();
                lblContador.setText(longitud + "/" + MAX_CARACTERES + " caracteres (mínimo " + MIN_CARACTERES + ")");
                
                if (longitud < MIN_CARACTERES) {
                    lblContador.setForeground(Color.RED);
                } else if (longitud > MAX_CARACTERES) {
                    lblContador.setForeground(Color.RED);
                } else {
                    lblContador.setForeground(new Color(0, 150, 0));
                }
            }
        });
        
        return lblContador;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        btnSalir = crearBoton("Salir");
        btnSalir.setBackground(new Color(200, 200, 200));
        btnSalir.addActionListener(e -> intentarSalir());
        panel.add(btnSalir);

        btnSiguiente = crearBoton("Siguiente");
        btnSiguiente.addActionListener(e -> guardarYContinuar());
        panel.add(btnSiguiente);

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
                if (boton.isEnabled()) {
                    Color bgActual = boton.getBackground();
                    if (bgActual.equals(CB)) {
                        boton.setBackground(CBH);
                    }
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton.isEnabled()) {
                    Color bgActual = boton.getBackground();
                    if (bgActual.equals(CBH)) {
                        boton.setBackground(CB);
                    }
                }
            }
        });
        
        return boton;
    }

    private void cargarDatosEstudianteActual() {
        if (estudianteActualIndex >= estudiantes.size()) {
            return;
        }

        Estudiante estudiante = estudiantes.get(estudianteActualIndex);
        lblEstudianteActual.setText(estudiante.obtenerNombreCompleto());
        lblProgreso.setText("Estudiante " + (estudianteActualIndex + 1) + " de " + estudiantes.size());

        // Limpiar campos
        txtEnfermedades.setText("");
        txtAspectosRelevantes.setText("");
        txtAlergias.setText("");

        // Cargar datos si ya existen
        ResultadoOperacion resultado = controller.obtenerHojaVidaDeEstudiante(estudiante.getIdEstudiante());
        if (resultado.isExitoso()) {
            HojaVida hojaVida = (HojaVida) resultado.getDatos();
            txtEnfermedades.setText(hojaVida.getEnfermedades());
            txtAspectosRelevantes.setText(hojaVida.getAspectosRelevantes());
            txtAlergias.setText(hojaVida.getAlergias());
        }

        // Actualizar texto del botón
        if (estudianteActualIndex == estudiantes.size() - 1) {
            btnSiguiente.setText("Enviar");
        } else {
            btnSiguiente.setText("Siguiente");
        }
    }

    private void guardarYContinuar() {
        Estudiante estudiante = estudiantes.get(estudianteActualIndex);
        
        String enfermedades = txtEnfermedades.getText().trim();
        String aspectos = txtAspectosRelevantes.getText().trim();
        String alergias = txtAlergias.getText().trim();

        // Guardar hoja de vida
        ResultadoOperacion resultado = controller.guardarHojaVida(
            estudiante.getIdEstudiante(), alergias, aspectos, enfermedades);

        if (!resultado.isExitoso()) {
            mostrarError(resultado.getMensaje());
            return;
        }

        // Avanzar al siguiente estudiante
        estudianteActualIndex++;

        if (estudianteActualIndex >= estudiantes.size()) {
            // Ya terminamos con todos
            mostrarExito();
            todasDiligenciadas = true;
            dispose();
        } else {
            // Cargar siguiente estudiante
            cargarDatosEstudianteActual();
        }
    }

    private void intentarSalir() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¡Espera!\n\nLos datos adicionales de la hoja de vida no se guardarán si sales ahora\n\n" +
            "¿Seguir diligenciando formulario?",
            "⚠️",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.NO_OPTION) {
            // No se completó el proceso
            todasDiligenciadas = false;
            dispose();
        }
        // Si elige YES, no hace nada (continúa en el diálogo)
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error de validación",
            JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarExito() {
        JDialog dialogo = new JDialog(this, "Éxito", false);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.setUndecorated(true);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.setSize(400, 250);
        dialogo.setLocationRelativeTo(this);

        JPanel panelContenido = new JPanel(new BorderLayout(10, 10));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblIcono = new JLabel("✓", SwingConstants.CENTER);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        lblIcono.setForeground(new Color(0, 150, 0));

        JLabel lblMensaje = new JLabel(
            "<html><center>Los datos fueron guardados correctamente en la(s) hoja(s) de vida</center></html>",
            SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMensaje.setForeground(CT);

        JButton btnAceptar = new JButton("Entrar");
        btnAceptar.addActionListener(e -> dialogo.dispose());
        btnAceptar.setBackground(CB);
        btnAceptar.setForeground(CT);
        btnAceptar.setFocusPainted(false);

        panelContenido.add(lblIcono, BorderLayout.NORTH);
        panelContenido.add(lblMensaje, BorderLayout.CENTER);
        panelContenido.add(btnAceptar, BorderLayout.SOUTH);

        dialogo.add(panelContenido);
        dialogo.setVisible(true);
    }

    public boolean seCompletaronTodasLasHojas() {
        return todasDiligenciadas;
    }
}