package com.vista.presentacion.observador;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import com.controlador.GestionObservadorController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Grupo;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Panel para que el directivo consulte observadores de todos los grupos
 */
public class ConsultarObservadorDirectivoPanel extends JFrame {
    private GestionObservadorController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public ConsultarObservadorDirectivoPanel() {
        this.controller = new GestionObservadorController();
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Consultar Observador - Todos los Grupos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);

        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelListaGrupos(), BorderLayout.CENTER);

        add(panelPrincipal);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

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

        JLabel lblTitulo = new JLabel("CONSULTAR OBSERVADOR");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(CT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane crearPanelListaGrupos() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setBackground(CF);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Obtener todos los grupos
        ResultadoOperacion resultado = controller.obtenerTodosLosGruposValidos();

        if (!resultado.isExitoso()) {
            JLabel lblError = new JLabel(resultado.getMensaje());
            lblError.setFont(new Font("Arial", Font.ITALIC, 14));
            lblError.setForeground(Color.RED);
            lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenedor.add(Box.createVerticalStrut(50));
            panelContenedor.add(lblError);
        } else {
            @SuppressWarnings("unchecked")
            List<Grupo> grupos = (List<Grupo>) resultado.getDatos();
            
            if (grupos.isEmpty()) {
                JLabel lblSinGrupos = new JLabel("No hay grupos registrados");
                lblSinGrupos.setFont(new Font("Arial", Font.ITALIC, 14));
                lblSinGrupos.setForeground(Color.GRAY);
                lblSinGrupos.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelContenedor.add(Box.createVerticalStrut(50));
                panelContenedor.add(lblSinGrupos);
            } else {
                // Mostrar grupos
                for (Grupo grupo : grupos) {
                    if (grupo.getEstudiantes() != null && !grupo.getEstudiantes().isEmpty()) {
                        panelContenedor.add(crearPanelGrupo(grupo));
                        panelContenedor.add(Box.createVerticalStrut(10));
                    }
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

    private JPanel crearPanelGrupo(Grupo grupo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Encabezado del grupo
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(Color.BLACK);
        panelEncabezado.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panelEncabezado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblGrupo = new JLabel(grupo.getNombreGrupo() + " - " + grupo.getGrado().getNombreGrado());
        lblGrupo.setFont(new Font("Arial", Font.BOLD, 14));
        lblGrupo.setForeground(Color.WHITE);
        panelEncabezado.add(lblGrupo, BorderLayout.WEST);

        panel.add(panelEncabezado);
        panel.add(Box.createVerticalStrut(10));

        // Lista de estudiantes
        for (Estudiante estudiante : grupo.getEstudiantes()) {
            panel.add(crearFilaEstudiante(estudiante));
            panel.add(Box.createVerticalStrut(5));
        }

        return panel;
    }

    private JPanel crearFilaEstudiante(Estudiante estudiante) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblEstudiante = new JLabel("• " + estudiante.obtenerNombreCompleto());
        lblEstudiante.setFont(new Font("Arial", Font.PLAIN, 13));
        lblEstudiante.setForeground(CT);
        panel.add(lblEstudiante, BorderLayout.WEST);

        JButton btnVerObservador = new JButton("VER OBSERVADOR");
        btnVerObservador.setFont(new Font("Arial", Font.BOLD, 11));
        btnVerObservador.setBackground(CB);
        btnVerObservador.setForeground(CT);
        btnVerObservador.setFocusPainted(false);
        btnVerObservador.setBorderPainted(false);
        btnVerObservador.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerObservador.setPreferredSize(new Dimension(150, 30));
        
        btnVerObservador.addActionListener(e -> abrirObservador(estudiante));
        btnVerObservador.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnVerObservador.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnVerObservador.setBackground(CB);
            }
        });
        
        panel.add(btnVerObservador, BorderLayout.EAST);

        return panel;
    }

    private void abrirObservador(Estudiante estudiante) {
        // El directivo solo puede consultar, no modificar
        ConsultarObservadorDialog dialogo = new ConsultarObservadorDialog(
            this, estudiante, controller, false, null);
        dialogo.setVisible(true);
    }
}