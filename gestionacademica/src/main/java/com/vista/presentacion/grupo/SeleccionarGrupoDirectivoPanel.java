package com.vista.presentacion.grupo;

import com.aplicacion.JPAUtil;
import com.controlador.ConsultarGruposController;
import com.modelo.dominio.Grupo;
import com.modelo.dominio.ResultadoOperacion;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel para que el directivo seleccione un grupo a consultar
 */
public class SeleccionarGrupoDirectivoPanel extends JFrame {
    private final ConsultarGruposController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    private JPanel panelGrupos;
    private List<Grupo> grupos;

    public SeleccionarGrupoDirectivoPanel() {
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.controller = new ConsultarGruposController(em);
        
        inicializarComponentes();
        cargarGrupos();
    }

    private void inicializarComponentes() {
        setTitle("Consultar Grupos - Sistema de Gestión Académica");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(CF);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior
        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        
        // Panel central con lista de grupos
        panelPrincipal.add(crearPanelListaGrupos(), BorderLayout.CENTER);
        
        // Panel inferior con botones
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblTitulo = new JLabel("CONSULTAR GRUPOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(CT);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        
        panel.add(Box.createVerticalStrut(10));

        JLabel lblInstruccion = new JLabel("Seleccione un grupo para ver sus estudiantes");
        lblInstruccion.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInstruccion.setForeground(CT);
        lblInstruccion.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblInstruccion);

        return panel;
    }

    private JPanel crearPanelListaGrupos() {
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(CF);

        panelGrupos = new JPanel();
        panelGrupos.setLayout(new BoxLayout(panelGrupos, BoxLayout.Y_AXIS));
        panelGrupos.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(panelGrupos);
        scrollPane.setBorder(BorderFactory.createLineBorder(CB, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panelContenedor.add(scrollPane, BorderLayout.CENTER);

        return panelContenedor;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(CF);

        JButton btnVolver = crearBoton("Volver", "🔙");
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
        boton.setPreferredSize(new Dimension(150, 40));
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

    @SuppressWarnings("unchecked")
    private void cargarGrupos() {
        ResultadoOperacion resultado = controller.obtenerTodosLosGruposValidos();
        
        if (!resultado.isExitoso()) {
            mostrarError(resultado.getMensaje());
            return;
        }
        
        grupos = (List<Grupo>) resultado.getDatos();
        
        if (grupos.isEmpty()) {
            mostrarMensajeSinGrupos();
            return;
        }
        
        mostrarListaGrupos();
    }

    private void mostrarListaGrupos() {
        panelGrupos.removeAll();
        
        // Agrupar por grado
        String gradoActual = "";
        
        for (Grupo grupo : grupos) {
            String nombreGrado = grupo.getGrado().getNombreGrado();
            
            // Agregar separador de grado si es necesario
            if (!nombreGrado.equals(gradoActual)) {
                if (!gradoActual.isEmpty()) {
                    panelGrupos.add(Box.createVerticalStrut(15));
                }
                
                JPanel separadorGrado = crearSeparadorGrado(nombreGrado);
                panelGrupos.add(separadorGrado);
                panelGrupos.add(Box.createVerticalStrut(10));
                
                gradoActual = nombreGrado;
            }
            
            JPanel itemPanel = crearItemGrupo(grupo);
            panelGrupos.add(itemPanel);
            panelGrupos.add(Box.createVerticalStrut(5));
        }
        
        panelGrupos.revalidate();
        panelGrupos.repaint();
    }

    private JPanel crearSeparadorGrado(String nombreGrado) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 240, 240));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel lblGrado = new JLabel(nombreGrado);
        lblGrado.setFont(new Font("Arial", Font.BOLD, 14));
        lblGrado.setForeground(CT);
        panel.add(lblGrado);

        return panel;
    }

    private JPanel crearItemGrupo(Grupo grupo) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Información del grupo
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);

        JLabel lblNombreGrupo = new JLabel(grupo.getNombreGrupo());
        lblNombreGrupo.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombreGrupo.setForeground(CT);
        panelInfo.add(lblNombreGrupo);

        panelInfo.add(Box.createVerticalStrut(5));

        String nombreProfesor = grupo.getProfesor() != null ? 
            grupo.getProfesor().obtenerNombreCompleto() : "Sin profesor";
        JLabel lblProfesor = new JLabel("Profesor: " + nombreProfesor);
        lblProfesor.setFont(new Font("Arial", Font.PLAIN, 11));
        lblProfesor.setForeground(new Color(100, 100, 100));
        panelInfo.add(lblProfesor);

        panelInfo.add(Box.createVerticalStrut(3));

        JLabel lblEstudiantes = new JLabel("Estudiantes: " + grupo.getCantidadEstudiantes());
        lblEstudiantes.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstudiantes.setForeground(new Color(100, 100, 100));
        panelInfo.add(lblEstudiantes);

        panel.add(panelInfo, BorderLayout.CENTER);

        // Botón consultar
        JButton btnConsultar = new JButton("Consultar 👁");
        btnConsultar.setFont(new Font("Arial", Font.BOLD, 11));
        btnConsultar.setBackground(CB);
        btnConsultar.setForeground(CT);
        btnConsultar.setFocusPainted(false);
        btnConsultar.setBorderPainted(false);
        btnConsultar.setPreferredSize(new Dimension(120, 35));
        btnConsultar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnConsultar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnConsultar.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnConsultar.setBackground(CB);
            }
        });
        
        btnConsultar.addActionListener(e -> consultarGrupo(grupo));
        panel.add(btnConsultar, BorderLayout.EAST);

        // Efecto hover en el panel
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(250, 245, 235));
                panelInfo.setBackground(new Color(250, 245, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(Color.WHITE);
                panelInfo.setBackground(Color.WHITE);
            }
        });

        return panel;
    }

    private void mostrarMensajeSinGrupos() {
        panelGrupos.removeAll();
        
        JLabel lblMensaje = new JLabel("No hay grupos disponibles para consultar");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMensaje.setForeground(new Color(150, 150, 150));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelGrupos.add(Box.createVerticalGlue());
        panelGrupos.add(lblMensaje);
        panelGrupos.add(Box.createVerticalGlue());
        
        panelGrupos.revalidate();
        panelGrupos.repaint();
    }

    private void consultarGrupo(Grupo grupo) {
        ConsultarGrupoDirectivoPanel panel = new ConsultarGrupoDirectivoPanel(grupo.getIdGrupo());
        panel.setVisible(true);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}