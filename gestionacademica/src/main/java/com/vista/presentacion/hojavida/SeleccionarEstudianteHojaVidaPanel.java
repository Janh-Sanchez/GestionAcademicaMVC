package com.vista.presentacion.hojavida;

import java.awt.*;
import java.util.Set;
import javax.swing.*;

import com.controlador.GestionHojaVidaController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Grupo;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;

/**
 * Panel para que el profesor seleccione un estudiante de su grupo
 * y consulte su hoja de vida
 */
public class SeleccionarEstudianteHojaVidaPanel extends JFrame {
    private Profesor profesor;
    private GestionHojaVidaController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public SeleccionarEstudianteHojaVidaPanel(Profesor profesor) {
        this.profesor = profesor;
        this.controller = new GestionHojaVidaController();
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Hojas de Vida de Mi Grupo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);

        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelListaEstudiantes(), BorderLayout.CENTER);

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

        Grupo grupo = profesor.getGrupoAsignado();
        String tituloGrupo = grupo != null ? " - " + grupo.getNombreGrupo() + " / " + 
            grupo.getGrado().getNombreGrado() : "";
        
        JLabel lblTitulo = new JLabel("HOJAS DE VIDA DE MI GRUPO" + tituloGrupo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(CT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane crearPanelListaEstudiantes() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setBackground(CF);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Obtener estudiantes del grupo
        ResultadoOperacion resultado = controller.obtenerEstudiantesDelGrupo(profesor);

        if (!resultado.isExitoso()) {
            JLabel lblError = new JLabel(resultado.getMensaje());
            lblError.setFont(new Font("Arial", Font.ITALIC, 14));
            lblError.setForeground(Color.RED);
            lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenedor.add(Box.createVerticalStrut(50));
            panelContenedor.add(lblError);
        } else {
            @SuppressWarnings("unchecked")
            Set<Estudiante> estudiantes = (Set<Estudiante>) resultado.getDatos();
            
            if (estudiantes.isEmpty()) {
                JLabel lblSinEstudiantes = new JLabel("No hay estudiantes en el grupo");
                lblSinEstudiantes.setFont(new Font("Arial", Font.ITALIC, 14));
                lblSinEstudiantes.setForeground(Color.GRAY);
                lblSinEstudiantes.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelContenedor.add(Box.createVerticalStrut(50));
                panelContenedor.add(lblSinEstudiantes);
            } else {
                // Encabezado de lista
                JPanel panelEncabezadoLista = crearEncabezadoLista();
                panelEncabezadoLista.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                panelContenedor.add(panelEncabezadoLista);
                panelContenedor.add(Box.createVerticalStrut(5));

                // Lista de estudiantes
                for (Estudiante estudiante : estudiantes) {
                    panelContenedor.add(crearFilaEstudiante(estudiante));
                    panelContenedor.add(Box.createVerticalStrut(5));
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

    private JPanel crearEncabezadoLista() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblNombre = new JLabel(profesor.getGrupoAsignado().getNombreGrupo() + " - " + 
            profesor.getGrupoAsignado().getGrado().getNombreGrado());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setForeground(Color.WHITE);
        panel.add(lblNombre, BorderLayout.WEST);

        return panel;
    }

    private JPanel crearFilaEstudiante(Estudiante estudiante) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Nombre del estudiante con bullet point
        JLabel lblEstudiante = new JLabel("• " + estudiante.obtenerNombreCompleto());
        lblEstudiante.setFont(new Font("Arial", Font.PLAIN, 13));
        lblEstudiante.setForeground(CT);
        panel.add(lblEstudiante, BorderLayout.WEST);

        // Botón ver hoja de vida
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