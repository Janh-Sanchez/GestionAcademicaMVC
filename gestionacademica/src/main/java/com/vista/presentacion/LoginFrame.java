package com.vista.presentacion;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import com.aplicacion.JPAUtil;
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
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);

    public LoginFrame() {
        this.controller = new LoginController();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Gestión Académica - Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal dividido en dos
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.add(crearPanelIzquierdo());
        panelPrincipal.add(crearPanelDerecho());
        
        add(panelPrincipal);
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel();
        panel.setBackground(CF);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Título
        panel.add(crearLabel("¿Ya tienes una cuenta?", Font.BOLD, 24));
        panel.add(Box.createVerticalStrut(10));
        panel.add(crearLabel("¡Bienvenido de vuelta!", Font.PLAIN, 18));
        panel.add(Box.createVerticalStrut(40));

        // Campos de entrada
        panel.add(crearCampoConPlaceholder("Usuario", false));
        panel.add(Box.createVerticalStrut(20));
        panel.add(crearCampoConPlaceholder("Contraseña", true));
        panel.add(Box.createVerticalStrut(30));
        
        // Botón de iniciar sesión
        btnIniciarSesion = crearBoton("Iniciar sesión", e -> iniciarSesion());
        panel.add(btnIniciarSesion);
        panel.add(Box.createVerticalStrut(15));

        // Etiqueta de error
        lblError = new JLabel("");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Arial", Font.PLAIN, 12));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setVisible(false);
        panel.add(lblError);
        panel.add(Box.createVerticalStrut(30));

        // Panel de registro
        JPanel panelRegistro = new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBackground(CF);
                setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        };

        JLabel lblPregunta = new JLabel(
            "<html><center>¿Te interesa registrar a tus hijos<br>en nuestra institución?</center></html>", 
            SwingConstants.CENTER
        );
        lblPregunta.setFont(new Font("Arial", Font.BOLD, 16));
        lblPregunta.setForeground(CT);
        lblPregunta.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelRegistro.add(lblPregunta);
        panelRegistro.add(Box.createVerticalStrut(15));
        
        // Botón de registro
        JButton btnRegistrarse = crearBoton("REGISTRARSE", e -> abrirRegistro());
        btnRegistrarse.setMaximumSize(new Dimension(250, 35));
        panelRegistro.add(btnRegistrarse);
        panelRegistro.add(Box.createVerticalStrut(15));

        JLabel lblInvitacion = new JLabel(
            "<html><center>No esperes más <span style='color: red;'>¡Únete ahora!</span></center></html>", 
            SwingConstants.CENTER
        );
        lblInvitacion.setFont(new Font("Arial", Font.BOLD, 16));
        lblInvitacion.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelRegistro.add(lblInvitacion);
        
        panel.add(panelRegistro);
        return panel;
    }

    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 220, 180));
        
        // Cargar imagen
        java.net.URL url = getClass().getResource("/imagenes/imagenLogin.jpg");
        if (url != null) {
            ImageIcon iconOriginal = new ImageIcon(url);
            Image imagenEscalada = iconOriginal.getImage().getScaledInstance(450, 600, Image.SCALE_SMOOTH);
            JLabel lblImagen = new JLabel(new ImageIcon(imagenEscalada));
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblImagen, BorderLayout.CENTER);
        } else {
            // Imagen de respaldo
            JLabel lblTexto = new JLabel("Bienvenido al Sistema Académico", SwingConstants.CENTER);
            lblTexto.setFont(new Font("Arial", Font.BOLD, 18));
            lblTexto.setForeground(new Color(100, 100, 100));
            panel.add(lblTexto, BorderLayout.CENTER);
        }
        
        return panel;
    }

    private JPanel crearCampoConPlaceholder(String placeholder, boolean esPassword) {
        JPanel panelCampo = new JPanel(new BorderLayout());
        panelCampo.setBackground(CF);
        panelCampo.setMaximumSize(new Dimension(300, 40));

        JComponent componente;
        if (esPassword) {
            componente = new JPasswordField();
            txtContrasena = (JPasswordField) componente;
            ((JPasswordField) componente).addActionListener(e -> iniciarSesion());
        } else {
            componente = new JTextField();
            txtUsuario = (JTextField) componente;
            ((JTextField) componente).addActionListener(e -> txtContrasena.requestFocus());
        }

        // Estilo del campo
        componente.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        componente.setBackground(new Color(255, 255, 255, 200));
        componente.setFont(new Font("Arial", Font.PLAIN, 14));
        componente.setForeground(CT);

        // Placeholder
        JLabel lblPlaceholder = new JLabel(placeholder);
        lblPlaceholder.setForeground(new Color(138, 127, 127, 180));
        lblPlaceholder.setFont(new Font("Arial", Font.ITALIC, 14));
        lblPlaceholder.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        
        // Usar JLayeredPane para superponer
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(300, 40));
        layeredPane.setMaximumSize(new Dimension(300, 40));
        componente.setBounds(0, 0, 300, 40);
        lblPlaceholder.setBounds(0, 0, 300, 40);
        layeredPane.add(componente, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(lblPlaceholder, JLayeredPane.PALETTE_LAYER);
        panelCampo.add(layeredPane, BorderLayout.CENTER);

        // Manejar la visibilidad del placeholder
        if (esPassword) {
            JPasswordField campoPass = (JPasswordField) componente;
            campoPass.addCaretListener(e -> 
                lblPlaceholder.setVisible(campoPass.getPassword().length == 0)
            );
        } else {
            JTextField campoTexto = (JTextField) componente;
            campoTexto.addCaretListener(e -> 
                lblPlaceholder.setVisible(campoTexto.getText().isEmpty())
            );
        }
        
        return panelCampo;
    }

    private JButton crearBoton(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setBackground(CB);
        boton.setForeground(CT);
        boton.setFont(new Font("Arial", Font.BOLD, 16));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setMaximumSize(new Dimension(300, 40));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.addActionListener(accion);
        boton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(CBH);
            }
            public void mouseExited(MouseEvent e) {
                boton.setBackground(CB);
            }
        });
        return boton;
    }

    private JLabel crearLabel(String texto, int estilo, int tamaño) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", estilo, tamaño));
        label.setForeground(CT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Método principal para iniciar sesión - implementa correctamente MVC
     */
    private void iniciarSesion() {
        // 1. Obtener datos de la vista
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        
        // 2. Usar el controlador para validar y autenticar
        ResultadoOperacion resultado = controller.autenticarConValidacion(usuario, contrasena);
        
        // 3. Procesar resultado del controlador
        if (resultado.isExitoso()) {
            // Login exitoso
            lblError.setVisible(false);
            
            // Obtener usuario autenticado del resultado
            Usuario usuarioAutenticado = (Usuario) resultado.getDatos();
            
            // Cerrar ventana de login
            this.dispose();
            
            // Navegar según el rol del usuario
            controller.navegarSegunRol(usuarioAutenticado);
            
        } else {
            // Mostrar error
            mostrarError(resultado);
            
            // Verificar si la cuenta está bloqueada
            if (controller.estaBloqueado()) {
                deshabilitarLogin();
            }
        }
    }
    
    /**
     * Muestra el error obtenido del controlador
     */
    private void mostrarError(ResultadoOperacion resultado) {
        String mensaje = resultado.getMensaje();
        String campoError = resultado.getCampoError();
        
        // Si es error de validación de campo específico
        if (campoError != null) {
            switch (campoError) {
                case "nombreUsuario":
                    txtUsuario.requestFocus();
                    break;
                case "contrasena":
                    txtContrasena.requestFocus();
                    break;
            }
            
            // Mostrar error simple en label
            lblError.setText(mensaje);
            lblError.setVisible(true);
        } else {
            // Mostrar diálogo para errores graves
            mostrarDialogoError("Error de autenticación", mensaje);
        }
    }
    
    /**
     * Deshabilita el botón de login cuando se alcanza el límite de intentos
     */
    private void deshabilitarLogin() {
        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setBackground(new Color(207, 207, 207));
        btnIniciarSesion.setText("CUENTA BLOQUEADA");
    }
    
    /**
     * Muestra un diálogo de error personalizado
     */
    private void mostrarDialogoError(String titulo, String mensaje) {
        JDialog dialogo = new JDialog(this, titulo, true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.setUndecorated(true);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.setSize(400, 250);
        dialogo.setLocationRelativeTo(this);

        JPanel panelContenido = new JPanel(new BorderLayout(10, 10));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Icono de error
        JLabel lblIcono = new JLabel("⚠️", SwingConstants.CENTER);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        lblIcono.setForeground(Color.RED);

        // Mensaje
        JLabel lblMensaje = new JLabel("<html><center>" + mensaje + "</center></html>", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMensaje.setForeground(CT);

        // Botón para aceptar
        JButton btnAceptar = new JButton("Aceptar");
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
    
    /**
     * Abre el formulario de registro/preinscripción
     */
    private void abrirRegistro() {
        SwingUtilities.invokeLater(() -> {
            try {
                var em = JPAUtil.getEntityManagerFactory().createEntityManager();
                
                PreinscripcionFrame preinscripcionDialog = new PreinscripcionFrame(this, em);
                
                // Agregar el listener ANTES de mostrar
                preinscripcionDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        LoginFrame.this.setVisible(true);
                        LoginFrame.this.toFront(); // Traer al frente
                    }
                });
                
                preinscripcionDialog.mostrarFormulario();
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(LoginFrame.this, 
                    "Error al abrir el formulario: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                this.setVisible(true); // Mostrar login si hay error
            }
        });
    }
}