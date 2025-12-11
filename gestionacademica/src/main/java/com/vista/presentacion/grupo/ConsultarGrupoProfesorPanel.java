package com.vista.presentacion.grupo;

import com.aplicacion.JPAUtil;
import com.controlador.ConsultarGruposController;
import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Profesor;
import com.modelo.dominio.ResultadoOperacion;
import com.modelo.GeneradorPDFListado;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Panel para que el profesor consulte su grupo asignado
 */
public class ConsultarGrupoProfesorPanel extends JFrame {
    private final Profesor profesor;
    private final ConsultarGruposController controller;
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    private JLabel lblNombreGrupo;
    private JLabel lblNombreGrado;
    private JLabel lblCantidadEstudiantes;
    private JPanel panelEstudiantes;
    private ConsultarGruposController.DatosGrupoConsulta datosGrupo;

    public ConsultarGrupoProfesorPanel(Profesor profesor) {
        this.profesor = profesor;
        var em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.controller = new ConsultarGruposController(em);
        
        inicializarComponentes();
        cargarDatosGrupo();
    }

    private void inicializarComponentes() {
        setTitle("Mi Grupo - Sistema de Gestión Académica");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(CF);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior con información del grupo
        panelPrincipal.add(crearPanelInformacion(), BorderLayout.NORTH);
        
        // Panel central con lista de estudiantes
        panelPrincipal.add(crearPanelListaEstudiantes(), BorderLayout.CENTER);
        
        // Panel inferior con botones
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CB, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Título
        JLabel lblTitulo = new JLabel("MI GRUPO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(CT);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        
        panel.add(Box.createVerticalStrut(15));

        // Información del grupo
        lblNombreGrado = new JLabel("Grado: Cargando...");
        lblNombreGrado.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombreGrado.setForeground(CT);
        lblNombreGrado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblNombreGrado);
        
        panel.add(Box.createVerticalStrut(5));

        lblNombreGrupo = new JLabel("Grupo: Cargando...");
        lblNombreGrupo.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombreGrupo.setForeground(CT);
        lblNombreGrupo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblNombreGrupo);
        
        panel.add(Box.createVerticalStrut(5));

        lblCantidadEstudiantes = new JLabel("Estudiantes: 0");
        lblCantidadEstudiantes.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCantidadEstudiantes.setForeground(CT);
        lblCantidadEstudiantes.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblCantidadEstudiantes);

        return panel;
    }

    private JPanel crearPanelListaEstudiantes() {
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(CF);

        JLabel lblTitulo = new JLabel("LISTA DE ESTUDIANTES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setForeground(CT);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panelContenedor.add(lblTitulo, BorderLayout.NORTH);

        panelEstudiantes = new JPanel();
        panelEstudiantes.setLayout(new BoxLayout(panelEstudiantes, BoxLayout.Y_AXIS));
        panelEstudiantes.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(panelEstudiantes);
        scrollPane.setBorder(BorderFactory.createLineBorder(CB, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panelContenedor.add(scrollPane, BorderLayout.CENTER);

        return panelContenedor;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(CF);

        JButton btnDescargar = crearBoton("Descargar listado de clase", "📥");
        btnDescargar.addActionListener(e -> descargarListadoPDF());
        panel.add(btnDescargar);

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
        boton.setPreferredSize(new Dimension(220, 40));
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

    private void cargarDatosGrupo() {
        ResultadoOperacion resultado = controller.obtenerGrupoDeProfesor(profesor);
        
        if (!resultado.isExitoso()) {
            mostrarError(resultado.getMensaje());
            return;
        }
        
        datosGrupo = (ConsultarGruposController.DatosGrupoConsulta) resultado.getDatos();
        
        // Actualizar información del grupo
        lblNombreGrado.setText("Grado: " + datosGrupo.getNombreGrado());
        lblNombreGrupo.setText("Grupo: " + datosGrupo.getNombreGrupo());
        lblCantidadEstudiantes.setText("Estudiantes: " + datosGrupo.getCantidadEstudiantes());
        
        // Cargar lista de estudiantes
        cargarListaEstudiantes(datosGrupo.getEstudiantesOrdenados());
    }

    private void cargarListaEstudiantes(List<Estudiante> estudiantes) {
        panelEstudiantes.removeAll();
        
        int numero = 1;
        for (Estudiante estudiante : estudiantes) {
            JPanel itemPanel = crearItemEstudiante(numero, estudiante);
            panelEstudiantes.add(itemPanel);
            panelEstudiantes.add(Box.createVerticalStrut(5));
            numero++;
        }
        
        panelEstudiantes.revalidate();
        panelEstudiantes.repaint();
    }

    private JPanel crearItemEstudiante(int numero, Estudiante estudiante) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Número
        JLabel lblNumero = new JLabel(String.format("%2d.", numero));
        lblNumero.setFont(new Font("Arial", Font.BOLD, 12));
        lblNumero.setForeground(CT);
        panel.add(lblNumero, BorderLayout.WEST);

        // Nombre completo
        JLabel lblNombre = new JLabel(estudiante.obtenerNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNombre.setForeground(CT);
        panel.add(lblNombre, BorderLayout.CENTER);

        return panel;
    }

    private void descargarListadoPDF() {
        if (datosGrupo == null) {
            mostrarError("No hay datos del grupo para descargar");
            return;
        }
        
        try {
            File archivoPDF = GeneradorPDFListado.generarListadoClase(
                datosGrupo.getGrupo(),
                datosGrupo.getEstudiantesOrdenados(),
                profesor.obtenerNombreCompleto()
            );
            
            JOptionPane.showMessageDialog(this,
                "Listado descargado exitosamente en:\n" + archivoPDF.getAbsolutePath(),
                "Descarga exitosa",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            mostrarError("Error al generar PDF: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}