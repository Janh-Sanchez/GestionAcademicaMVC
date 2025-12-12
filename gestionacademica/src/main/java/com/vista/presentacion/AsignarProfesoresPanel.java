package com.vista.presentacion;

import com.controlador.GestionGruposController;
import com.modelo.dominio.Grupo;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * Panel para asignar profesores a grupos
 * VISTA en arquitectura MVC - se comunica únicamente con el Controlador
 */
public class AsignarProfesoresPanel extends JFrame {
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    private final Color GRIS = new Color(200, 200, 200);
    private final Color VERDE = new Color(76, 175, 80);
    
    // Controlador MVC - única dependencia de la vista
    private GestionGruposController controlador;
    private JPanel panelContenido;
    private JScrollPane scrollPane;
    private List<Profesor> profesoresDisponibles;
    
    public AsignarProfesoresPanel() {
        this.controlador = new GestionGruposController();
        inicializarComponentes();
        cargarDatos();
    }
    
    private void inicializarComponentes() {
        setTitle("Asignar Profesores a Grupos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);
        
        // Panel superior con título y botón volver
        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        
        // Panel central con lista de grupos
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
        
        // Botón volver
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
        
        // Título
        JLabel lblTitulo = new JLabel("ASIGNAR PROFESORES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Carga los datos iniciales usando el controlador MVC
     */
    @SuppressWarnings("unchecked")
    private void cargarDatos() {
        // Cargar profesores disponibles
        ResultadoOperacion resultadoProfesores = controlador.obtenerProfesoresDisponibles();
        if (resultadoProfesores.isExitoso()) {
            profesoresDisponibles = (List<Profesor>) resultadoProfesores.getDatos();
        } else {
            profesoresDisponibles = List.of();
        }
        
        // Cargar grupos
        cargarGrupos();
    }
    
    /**
     * Carga la lista de grupos usando el controlador MVC
     */
    private void cargarGrupos() {
        panelContenido.removeAll();
        
        // Comunicación con el controlador
        ResultadoOperacion resultado = controlador.obtenerListaGrupos();
        
        if (!resultado.isExitoso()) {
            if ("VACIA".equals(resultado.getMensaje())) {
                mostrarListaVacia();
            } else {
                mostrarError(resultado.getMensaje());
            }
            return;
        }
        
        // Agregar encabezado
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(Color.BLACK);
        panelEncabezado.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelEncabezado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblEncabezado = new JLabel("LISTA ACTUALIZADA DE GRUPOS 2025 - III");
        lblEncabezado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEncabezado.setForeground(Color.WHITE);
        
        panelEncabezado.add(lblEncabezado, BorderLayout.WEST);
        panelContenido.add(panelEncabezado);
        panelContenido.add(Box.createVerticalStrut(10));
        
        // Agrupar por grado
        @SuppressWarnings("unchecked")
        List<Grupo> grupos = (List<Grupo>) resultado.getDatos();
        Map<String, List<Grupo>> gruposPorGrado = grupos.stream()
            .collect(java.util.stream.Collectors.groupingBy(g -> 
                g.getGrado() != null ? g.getGrado().getNombreGrado() : "Sin grado"));
        
        // Agregar grupos organizados por grado
        for (Map.Entry<String, List<Grupo>> entry : gruposPorGrado.entrySet()) {
            panelContenido.add(crearPanelGrado(entry.getKey(), entry.getValue()));
            panelContenido.add(Box.createVerticalStrut(15));
        }
        
        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    private JPanel crearPanelGrado(String nombreGrado, List<Grupo> grupos) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Título del grado
        JLabel lblGrado = new JLabel(nombreGrado);
        lblGrado.setFont(new Font("Arial", Font.BOLD, 15));
        lblGrado.setForeground(CT);
        lblGrado.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblGrado);
        
        panel.add(Box.createVerticalStrut(8));
        
        // Grupos del grado
        for (Grupo grupo : grupos) {
            panel.add(crearPanelGrupo(grupo));
            panel.add(Box.createVerticalStrut(8));
        }
        
        return panel;
    }
    
    private JPanel crearPanelGrupo(Grupo grupo) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Panel izquierdo - Información del grupo
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);
        
        JLabel lblNombre = new JLabel(grupo.getNombreGrupo());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 13));
        lblNombre.setForeground(CT);
        panelInfo.add(lblNombre);
        
        // Información de estudiantes y estado
        String estadoTexto = grupo.estaEnFormacion() ? 
            "• " + grupo.getCantidadEstudiantes() + " estudiantes - El grupo está en formación" :
            "• " + grupo.getCantidadEstudiantes() + " estudiantes";
        
        JLabel lblEstado = new JLabel(estadoTexto);
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(grupo.estaEnFormacion() ? 
            new Color(255, 100, 100) : new Color(100, 100, 100));
        panelInfo.add(lblEstado);
        
        panel.add(panelInfo, BorderLayout.WEST);
        
        // Panel derecho - Asignación de profesor
        if (grupo.estaEnFormacion()) {
            // Grupo en formación - mostrar deshabilitado
            JComboBox<String> comboProfesor = new JComboBox<>();
            comboProfesor.addItem("Asignar Profesor");
            comboProfesor.setEnabled(false);
            comboProfesor.setBackground(GRIS);
            comboProfesor.setFont(new Font("Arial", Font.PLAIN, 11));
            comboProfesor.setPreferredSize(new Dimension(200, 30));
            
            JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelDerecho.setBackground(Color.WHITE);
            panelDerecho.add(comboProfesor);
            
            panel.add(panelDerecho, BorderLayout.EAST);
        } else if (grupo.tieneProfesorAsignado()) {
            // Grupo con profesor asignado
            Profesor profesor = grupo.getProfesor();
            JLabel lblProfesor = new JLabel("✓ " + profesor.obtenerNombreCompleto());
            lblProfesor.setFont(new Font("Arial", Font.BOLD, 12));
            lblProfesor.setForeground(VERDE);
            
            JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelDerecho.setBackground(Color.WHITE);
            panelDerecho.add(lblProfesor);
            
            panel.add(panelDerecho, BorderLayout.EAST);
        } else {
            // Grupo listo sin profesor - mostrar combo activo
            JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            panelDerecho.setBackground(Color.WHITE);
            
            JComboBox<Profesor> comboProfesor = new JComboBox<>();
            
            // Agregar placeholder
            comboProfesor.addItem(null);
            comboProfesor.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) {
                        setText("Asignar Profesor");
                    } else if (value instanceof Profesor) {
                        setText(((Profesor) value).obtenerNombreCompleto());
                    }
                    return this;
                }
            });
            
            for (Profesor profesor : profesoresDisponibles) {
                comboProfesor.addItem(profesor);
            }
            
            comboProfesor.setFont(new Font("Arial", Font.PLAIN, 11));
            comboProfesor.setBackground(Color.WHITE);
            comboProfesor.setPreferredSize(new Dimension(200, 30));
            panelDerecho.add(comboProfesor);
            
            JButton btnAsignar = crearBotonAccion("Asignar", CB, CBH);
            btnAsignar.addActionListener(e -> {
                Profesor profesorSeleccionado = (Profesor) comboProfesor.getSelectedItem();
                if (profesorSeleccionado != null) {
                    asignarProfesor(grupo, profesorSeleccionado);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Por favor seleccione un profesor",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
            panelDerecho.add(btnAsignar);
            
            panel.add(panelDerecho, BorderLayout.EAST);
        }
        
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
        boton.setPreferredSize(new Dimension(75, 28));
        
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
     * Asigna un profesor a un grupo a través del controlador MVC
     */
    private void asignarProfesor(Grupo grupo, Profesor profesor) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea asignar a " + profesor.obtenerNombreCompleto() + 
            "\nal grupo " + grupo.getNombreGrupo() + "?",
            "Confirmar asignación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            // Comunicación con el controlador usando IDs directos
            ResultadoOperacion resultado = controlador.asignarProfesorAGrupo(
                grupo.getIdGrupo(), 
                profesor.getIdUsuario()
            );
            
            if (resultado.isExitoso()) {
                mostrarMensajeExito(resultado.getMensaje());
                cargarDatos(); // Recargar datos
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
            "<html><center>En este momento no hay grupos registrados<br>" +
            "en el sistema</center></html>");
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
        lblIcono.setForeground(new Color(244, 67, 54));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblIcono);
        
        panelContenido.add(Box.createVerticalStrut(15));
        
        JLabel lblTitulo = new JLabel("ERROR");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(244, 67, 54));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblTitulo);
        
        panelContenido.add(Box.createVerticalStrut(10));
        
        JLabel lblMensaje = new JLabel("<html><center>" + mensaje + "</center></html>");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblMensaje);
        
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
        
        JLabel lblMensaje = new JLabel("<html><center>" + mensaje + "</center></html>");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblMensaje);
        
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