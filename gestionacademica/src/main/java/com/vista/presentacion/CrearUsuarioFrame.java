package com.vista.presentacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import com.controlador.GestionUsuariosController;
import com.modelo.dominio.ResultadoOperacion;
import com.modelo.dominio.Rol;
import com.modelo.dominio.Usuario;
import com.modelo.dtos.UsuarioDTO;

/**
 * Formulario para crear usuarios (CU 2.2)
 * Vista que interactúa con GestionUsuariosController
 */
public class CrearUsuarioFrame extends JFrame {
    
    private final Color CB = new Color(255, 212, 160);
    private final Color CBH = new Color(255, 230, 180);
    private final Color CT = new Color(58, 46, 46);
    private final Color CF = new Color(255, 243, 227);
    
    private GestionUsuariosController controller;
    private JFrame ventanaPadre;
    
    // Componentes del formulario
    private JTextField txtNuip;
    private JTextField txtPrimerNombre;
    private JTextField txtSegundoNombre;
    private JTextField txtPrimerApellido;
    private JTextField txtSegundoApellido;
    private JTextField txtEdad;
    private JTextField txtCorreo;
    private JTextField txtTelefono;
    
    // Rol determina el tipo de usuario
    private JComboBox<String> cmbRol;
    
    public CrearUsuarioFrame(GestionUsuariosController controller, JFrame padre) {
        this.controller = controller;
        this.ventanaPadre = padre;
        inicializarComponentes();
        cargarRoles();
    }
    
    private void inicializarComponentes() {
        setTitle("Crear Usuario - Sistema de Gestión Académica");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(ventanaPadre);
        setResizable(false);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);
        
        // Panel superior
        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        
        // Panel central con formulario en scroll
        JScrollPane scrollPane = new JScrollPane(crearPanelFormulario());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con botones
        panelPrincipal.add(crearPanelInferior(), BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel();
        panel.setBackground(CB);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("Crear Nuevo Usuario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(CT);
        panel.add(lblTitulo);
        
        return panel;
    }
    
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Rol del usuario (determina el tipo)
        panel.add(crearSeccion("ROL DEL USUARIO"));
        JLabel lblInfoRol = new JLabel("<html><i>El rol determina el tipo de usuario y genera las credenciales automáticamente</i></html>");
        lblInfoRol.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfoRol.setForeground(new Color(120, 120, 120));
        lblInfoRol.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblInfoRol);
        panel.add(Box.createVerticalStrut(8));
        
        cmbRol = crearComboBox(new String[]{"Seleccionar rol..."});
        panel.add(crearCampoFormulario("Rol *:", cmbRol));
        panel.add(Box.createVerticalStrut(20));
        
        // Datos personales
        panel.add(crearSeccion("DATOS PERSONALES"));
        txtNuip = crearTextField();
        panel.add(crearCampoFormulario("NUIP *:", txtNuip));
        
        txtPrimerNombre = crearTextField();
        panel.add(crearCampoFormulario("Primer nombre *:", txtPrimerNombre));
        
        txtSegundoNombre = crearTextField();
        panel.add(crearCampoFormulario("Segundo nombre:", txtSegundoNombre));
        
        txtPrimerApellido = crearTextField();
        panel.add(crearCampoFormulario("Primer apellido *:", txtPrimerApellido));
        
        txtSegundoApellido = crearTextField();
        panel.add(crearCampoFormulario("Segundo apellido:", txtSegundoApellido));
        
        txtEdad = crearTextField();
        panel.add(crearCampoFormulario("Edad *:", txtEdad));
        
        panel.add(Box.createVerticalStrut(20));
        
        // Datos de contacto
        panel.add(crearSeccion("DATOS DE CONTACTO"));
        txtCorreo = crearTextField();
        panel.add(crearCampoFormulario("Correo electrónico *:", txtCorreo));
        
        txtTelefono = crearTextField();
        panel.add(crearCampoFormulario("Teléfono *:", txtTelefono));
        
        panel.add(Box.createVerticalStrut(20));
        
        return panel;
    }
    
    private JLabel crearSeccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(100, 100, 100));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JPanel crearCampoFormulario(String label, JComponent campo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CF);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        
        JLabel lblCampo = new JLabel(label);
        lblCampo.setFont(new Font("Arial", Font.PLAIN, 13));
        lblCampo.setForeground(CT);
        lblCampo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblCampo);
        
        panel.add(Box.createVerticalStrut(5));
        
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(campo);
        
        return panel;
    }
    
    private JTextField crearTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }
    
    private JComboBox<String> crearComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        return combo;
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(CF);
        
        JButton btnGuardar = crearBoton("GUARDAR", e -> guardarUsuario());
        panel.add(btnGuardar);
        
        JButton btnCancelar = crearBoton("CANCELAR", e -> cancelar());
        panel.add(btnCancelar);
        
        return panel;
    }
    
    private JButton crearBoton(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setBackground(CB);
        boton.setForeground(CT);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(150, 40));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(accion);
        boton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { boton.setBackground(CBH); }
            public void mouseExited(MouseEvent e) { boton.setBackground(CB); }
        });
        return boton;
    }
    
    private void cargarRoles() {
        ResultadoOperacion resultado = controller.obtenerRolesDisponibles();
        if (resultado.isExitoso()) {
            @SuppressWarnings("unchecked")
            List<Rol> roles = (List<Rol>) resultado.getDatos();
            
            // Filtrar solo Profesor y Directivo
            for (Rol rol : roles) {
                String nombreRol = rol.getNombre();
                if (nombreRol.equalsIgnoreCase("Profesor") || 
                    nombreRol.equalsIgnoreCase("Directivo")) {
                    cmbRol.addItem(nombreRol);
                }
            }
        }
    }
    
    private void guardarUsuario() {
        // Recopilar datos del formulario
        UsuarioDTO datos = new UsuarioDTO();
        datos.nuip = txtNuip.getText().trim();
        datos.primerNombre = txtPrimerNombre.getText().trim();
        datos.segundoNombre = txtSegundoNombre.getText().trim();
        datos.primerApellido = txtPrimerApellido.getText().trim();
        datos.segundoApellido = txtSegundoApellido.getText().trim();
        datos.correoElectronico = txtCorreo.getText().trim();
        datos.telefono = txtTelefono.getText().trim();
        
        // Validar rol y determinar tipo de usuario
        String rol = (String) cmbRol.getSelectedItem();
        if (rol == null || rol.equals("Seleccionar rol...")) {
            mostrarError("rol", "Debe seleccionar un rol");
            return;
        }
        datos.nombreRol = rol;
        
        // Validar y parsear edad
        try {
            String edadStr = txtEdad.getText().trim();
            if (edadStr.isEmpty()) {
                mostrarError("edad", "La edad es obligatoria");
                return;
            }
            datos.edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            mostrarError("edad", "La edad debe ser un número válido");
            return;
        }
        
        // Enviar al controlador
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ResultadoOperacion resultado = controller.crearUsuario(datos);
        setCursor(Cursor.getDefaultCursor());
        
        if (resultado.isExitoso()) {
            // Obtener el usuario creado con el token generado
            Usuario usuarioCreado = (Usuario) resultado.getDatos();
            
            // Mostrar credenciales generadas
            String mensaje = "Usuario creado exitosamente.\n\n" +
                           "Tipo: " + rol + "\n" +
                           "Nombre: " + usuarioCreado.obtenerNombreCompleto();
            
            JOptionPane.showMessageDialog(this,
                mensaje,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
        } else {
            mostrarError(resultado.getCampoError(), resultado.getMensaje());
        }
    }
    
    private void mostrarError(String campo, String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error de validación",
            JOptionPane.ERROR_MESSAGE);
        
        // Enfocar el campo con error
        enfocarCampo(campo);
    }
    
    private void enfocarCampo(String campo) {
        if (campo == null) return;
        
        switch (campo) {
            case "rol": cmbRol.requestFocus(); break;
            case "nuip": txtNuip.requestFocus(); break;
            case "primerNombre": txtPrimerNombre.requestFocus(); break;
            case "segundoNombre": txtSegundoNombre.requestFocus(); break;
            case "primerApellido": txtPrimerApellido.requestFocus(); break;
            case "segundoApellido": txtSegundoApellido.requestFocus(); break;
            case "edad": txtEdad.requestFocus(); break;
            case "correoElectronico": txtCorreo.requestFocus(); break;
            case "telefono": txtTelefono.requestFocus(); break;
        }
    }
    
    private void limpiarFormulario() {
        cmbRol.setSelectedIndex(0);
        txtNuip.setText("");
        txtPrimerNombre.setText("");
        txtSegundoNombre.setText("");
        txtPrimerApellido.setText("");
        txtSegundoApellido.setText("");
        txtEdad.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
    }
    
    private void cancelar() {
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de cancelar? Se perderán los datos ingresados.",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            ventanaPadre.setVisible(true);
            this.dispose();
        }
    }
}