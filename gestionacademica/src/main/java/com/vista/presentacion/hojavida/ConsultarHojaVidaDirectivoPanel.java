package com.vista.presentacion.hojavida;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import com.aplicacion.JPAUtil;
import com.controlador.GestionHojaVidaController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Grupo;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Panel para que el directivo consulte hojas de vida de todos los grupos
 */
public class ConsultarHojaVidaDirectivoPanel extends JFrame {
    private GestionHojaVidaController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public ConsultarHojaVidaDirectivoPanel() {
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.controller = new GestionHojaVidaController(em);
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Consultar Hoja de Vida - Todos los Grupos");
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

        JLabel lblTitulo = new JLabel("CONSULTAR HOJAS DE VIDA");
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

        JButton btnVerHojaVida = new JButton("👁 VER HOJA DE VIDA");
        btnVerHojaVida.setFont(new Font("Arial", Font.BOLD, 11));
        btnVerHojaVida.setBackground(CB);
        btnVerHojaVida.setForeground(CT);
        btnVerHojaVida.setFocusPainted(false);
        btnVerHojaVida.setBorderPainted(false);
        btnVerHojaVida.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerHojaVida.setPreferredSize(new Dimension(170, 30));
        
        btnVerHojaVida.addActionListener(e -> abrirHojaVida(estudiante));
        btnVerHojaVida.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnVerHojaVida.setBackground(CBH);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnVerHojaVida.setBackground(CB);
            }
        });
        
        panel.add(btnVerHojaVida, BorderLayout.EAST);

        return panel;
    }

    private void abrirHojaVida(Estudiante estudiante) {
        ConsultarHojaVidaDialog dialogo = new ConsultarHojaVidaDialog(
            this, estudiante, controller);
        dialogo.setVisible(true);
    }
}