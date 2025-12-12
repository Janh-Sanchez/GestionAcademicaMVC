package com.vista.presentacion;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.controlador.LoginController;
import com.modelo.dominio.ResultadoOperacion;
import com.modelo.dominio.Usuario;

public class LoginFrame extends JFrame {
    private LoginController controller;
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIniciarSesion;
    private JLabel lblError;
    private final Color CB = new Color(255, 212, 160);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public LoginFrame() {
        this.controller = new LoginController();
        configurarVentana();
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.add(crearPanelIzquierdo());
        panelPrincipal.add(crearPanelDerecho());
        add(panelPrincipal);
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión Académica");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel();
        panel.setBackground(CF);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        panel.add(crearLabel("¿Ya tienes una cuenta?", Font.BOLD, 28));
        panel.add(Box.createVerticalStrut(15));
        panel.add(crearLabel("¡Bienvenido de vuelta!", Font.PLAIN, 20));
        panel.add(Box.createVerticalStrut(50));

        txtUsuario = (JTextField) crearCampo(false);
        txtContrasena = (JPasswordField) crearCampo(true);

        panel.add(crearPanelCampo(txtUsuario, "Usuario"));
        panel.add(Box.createVerticalStrut(25));
        panel.add(crearPanelCampo(txtContrasena, "Contraseña"));
        panel.add(Box.createVerticalStrut(35));

        btnIniciarSesion = crearBoton("Iniciar sesión", e -> iniciarSesion());
        panel.add(btnIniciarSesion);
        panel.add(Box.createVerticalStrut(20));

        lblError = new JLabel("");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Arial", Font.PLAIN, 14));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setVisible(false);
        panel.add(lblError);
        panel.add(Box.createVerticalStrut(40));

        JLabel lblPregunta = new JLabel("<html><center>¿Te interesa registrar a tus hijos<br>en nuestra institución?</center></html>");
        lblPregunta.setFont(new Font("Arial", Font.BOLD, 18));
        lblPregunta.setForeground(CT);
        lblPregunta.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPregunta.setHorizontalAlignment(SwingConstants.CENTER);
        lblPregunta.setMaximumSize(new Dimension(400, 60));

        JButton btnRegistro = crearBoton("REGISTRARSE", e -> abrirRegistro());
        btnRegistro.setMaximumSize(new Dimension(280, 40));

        JLabel lblInvitacion = new JLabel("<html><center>No esperes más <span style='color: red;'>¡Únete ahora!</span></center></html>");
        lblInvitacion.setFont(new Font("Arial", Font.BOLD, 18));
        lblInvitacion.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblInvitacion.setHorizontalAlignment(SwingConstants.CENTER);
        lblInvitacion.setMaximumSize(new Dimension(400, 30));

        panel.add(lblPregunta);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnRegistro);
        panel.add(Box.createVerticalStrut(18));
        panel.add(lblInvitacion);

        return panel;
    }

    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 220, 180));

        java.net.URL url = getClass().getResource("/imagenes/imagenLogin.jpg");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(500, 700, Image.SCALE_SMOOTH);
            panel.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        }

        return panel;
    }

    private JComponent crearCampo(boolean esPassword) {
        JComponent campo = esPassword ? new JPasswordField() : new JTextField();
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        campo.setBackground(new Color(255, 255, 255, 200));
        campo.setFont(new Font("Arial", Font.PLAIN, 16));
        campo.setForeground(CT);

        if (esPassword) {
            ((JPasswordField) campo).addActionListener(e -> iniciarSesion());
        } else {
            ((JTextField) campo).addActionListener(e -> txtContrasena.requestFocus());
        }

        return campo;
    }

    private JPanel crearPanelCampo(JComponent campo, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CF);
        panel.setMaximumSize(new Dimension(350, 45));

        JLabel lblPlaceholder = new JLabel(placeholder);
        lblPlaceholder.setForeground(new Color(138, 127, 127, 180));
        lblPlaceholder.setFont(new Font("Arial", Font.ITALIC, 16));
        lblPlaceholder.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(350, 45));
        layered.setMaximumSize(new Dimension(350, 45));

        campo.setBounds(0, 0, 350, 45);
        lblPlaceholder.setBounds(0, 0, 350, 45);

        layered.add(campo, JLayeredPane.DEFAULT_LAYER);
        layered.add(lblPlaceholder, JLayeredPane.PALETTE_LAYER);

        if (campo instanceof JPasswordField) {
            ((JPasswordField) campo).addCaretListener(e -> lblPlaceholder.setVisible(((JPasswordField) campo).getPassword().length == 0));
        } else {
            ((JTextField) campo).addCaretListener(e -> lblPlaceholder.setVisible(((JTextField) campo).getText().isEmpty()));
        }

        panel.add(layered, BorderLayout.CENTER);
        return panel;
    }

    private JButton crearBoton(String texto, ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setBackground(CB);
        btn.setForeground(CT);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(350, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(accion);
        return btn;
    }

    private JLabel crearLabel(String texto, int estilo, int tamaño) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", estilo, tamaño));
        lbl.setForeground(CT);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        ResultadoOperacion resultado = controller.autenticarConValidacion(usuario, contrasena);

        if (resultado.isExitoso()) {
            lblError.setVisible(false);
            Usuario usuarioAutenticado = (Usuario) resultado.getDatos();
            this.dispose();
            controller.navegarSegunRol(usuarioAutenticado);
        } else {
            mostrarError(resultado);
            if (controller.estaBloqueado()) {
                btnIniciarSesion.setEnabled(false);
                btnIniciarSesion.setBackground(new Color(207, 207, 207));
                btnIniciarSesion.setText("SIN INTENTOS");
            }
        }
    }

    private void mostrarError(ResultadoOperacion resultado) {
        String campoError = resultado.getCampoError();
        if (campoError != null) {
            if ("nombreUsuario".equals(campoError)) txtUsuario.requestFocus();
            else if ("contrasena".equals(campoError)) txtContrasena.requestFocus();
            lblError.setText(resultado.getMensaje());
            lblError.setVisible(true);
        } else {
            mostrarDialogoError("Error de autenticación", resultado.getMensaje());
        }
    }

    private void mostrarDialogoError(String titulo, String mensaje) {
        JDialog dialogo = new JDialog(this, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(450, 280);
        dialogo.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel iconoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconoPanel.setBackground(Color.WHITE);
        iconoPanel.setPreferredSize(new Dimension(450, 80));

        JLabel icono = new JLabel("X");
        icono.setFont(new Font("Arial", Font.BOLD, 70));
        icono.setForeground(Color.RED);
        icono.setHorizontalAlignment(SwingConstants.CENTER);
        icono.setVerticalAlignment(SwingConstants.CENTER);
        iconoPanel.add(icono);

        JLabel msg = new JLabel("<html><center>" + mensaje + "</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Arial", Font.PLAIN, 16));
        msg.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btn = new JButton("Aceptar");
        btn.addActionListener(e -> dialogo.dispose());
        btn.setBackground(CB);
        btn.setForeground(CT);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btn);

        panel.add(iconoPanel, BorderLayout.NORTH);
        panel.add(msg, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialogo.add(panel);
        dialogo.setVisible(true);
    }

    private void abrirRegistro() {
        PreinscripcionFrame form = new PreinscripcionFrame(this);
        form.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                LoginFrame.this.setVisible(true);
                LoginFrame.this.toFront();
            }
        });
        form.mostrarFormulario();
    }
}