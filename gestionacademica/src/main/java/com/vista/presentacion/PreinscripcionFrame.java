package com.vista.presentacion;

import com.controlador.PreinscripcionController;
import com.modelo.dominio.ResultadoOperacion;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PreinscripcionFrame extends JDialog {
    private final PreinscripcionController controlador;

    // Datos temporales capturados del usuario 
    private Map<String, String> datosAcudienteCapturados;
    private List<Map<String, String>> datosEstudiantesCapturados;
    
    // Referencias a componentes UI del acudiente
    private Map<String, JTextField> camposAcudiente;
    private Map<String, JLabel> etiquetasAcudiente;
    private Map<String, JLabel> etiquetasErrorAcudiente;
    
    // Lista de paneles de estudiantes
    private List<PanelEstudiante> panelesEstudiantes;
    
    // Panel principal contenedor
    private JPanel panelContenidoScroll;
    private JButton btnAgregarEstudiante;
    private JLabel lblContadorEstudiantes;
    
    // Colores de la UI - Paleta del LoginFrame
    private static final Color CB = new Color(255, 212, 160);
    private static final Color CT = new Color(58, 46, 46);
    private static final Color CF = new Color(255, 243, 227);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_CAMPO_NORMAL = CT;
    private static final Color COLOR_CAMPO_ERROR = Color.RED;
    private static final Color BORDER_ERROR = new Color(220, 53, 69);
    
    // Clase interna para manejar cada panel de estudiante
    private class PanelEstudiante {
        JPanel panel;
        Map<String, JTextField> campos;
        Map<String, JLabel> etiquetas;
        Map<String, JLabel> etiquetasError;
        JComboBox<String> comboGrado;
        int numero;
        
        PanelEstudiante(int numero) {
            this.numero = numero;
            this.campos = new HashMap<>();
            this.etiquetas = new HashMap<>();
            this.etiquetasError = new HashMap<>();
            this.panel = crearPanel();
        }
        
        private JPanel crearPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(CB, 2),
                    "Estudiante #" + numero,
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 14),
                    CT
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            p.setBackground(Color.WHITE);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            int fila = 0;
            fila = agregarCampoEstudiante(p, gbc, fila, "Primer nombre", "primerNombre", true);
            fila = agregarCampoEstudiante(p, gbc, fila, "Segundo Nombre", "segundoNombre", false);
            fila = agregarCampoEstudiante(p, gbc, fila, "Primer Apellido", "primerApellido", true);
            fila = agregarCampoEstudiante(p, gbc, fila, "Segundo Apellido", "segundoApellido", false);
            fila = agregarCampoEstudiante(p, gbc, fila, "Edad", "edad", true);
            fila = agregarCampoEstudiante(p, gbc, fila, "NUIP", "nuip", true);
            
            // ComboBox de grados
            JLabel lblGrado = new JLabel("Grado al que aspira (*)");
            lblGrado.setFont(new Font("Arial", Font.PLAIN, 14));
            lblGrado.setForeground(CT);
            gbc.gridx = 0;
            gbc.gridy = fila;
            p.add(lblGrado, gbc);
            etiquetas.put("gradoAspira", lblGrado);
            
            ResultadoOperacion resultado = controlador.obtenerGradosDisponibles();
            String[] grados;
            
            if (resultado.isExitoso() && resultado.getDatos() != null) {
                @SuppressWarnings("unchecked")
                List<String> listaGrados = (List<String>) resultado.getDatos();
                grados = listaGrados.toArray(new String[0]);
            } else {
                grados = new String[]{"Párvulos", "Caminadores", "Pre-Jardín"};
            }
            
            comboGrado = new JComboBox<>(grados);
            comboGrado.setFont(new Font("Arial", Font.PLAIN, 14));
            comboGrado.setBackground(Color.WHITE);
            gbc.gridx = 1;
            p.add(comboGrado, gbc);
            
            JLabel lblError = new JLabel("");
            lblError.setForeground(COLOR_ERROR);
            lblError.setFont(new Font("Arial", Font.PLAIN, 10));
            lblError.setVisible(false);
            gbc.gridx = 0;
            gbc.gridy = fila + 1;
            gbc.gridwidth = 2;
            p.add(lblError, gbc);
            etiquetasError.put("gradoAspira", lblError);
            
            return p;
        }
        
        private int agregarCampoEstudiante(JPanel panel, GridBagConstraints gbc, 
                                int fila, String etiqueta, String nombre, boolean obligatorio) {
            JLabel lbl = new JLabel(etiqueta + (obligatorio ? " (*)" : ""));
            lbl.setForeground(COLOR_CAMPO_NORMAL);
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = fila;
            panel.add(lbl, gbc);
            etiquetas.put(nombre, lbl);
            
            JTextField txt = new JTextField(20);
            txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            txt.setBackground(new Color(255, 255, 255, 200));
            txt.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            panel.add(txt, gbc);
            campos.put(nombre, txt);
            
            JLabel lblError = new JLabel("");
            lblError.setForeground(COLOR_ERROR);
            lblError.setFont(new Font("Arial", Font.PLAIN, 10));
            lblError.setVisible(false);
            gbc.gridx = 0;
            gbc.gridy = fila + 1;
            gbc.gridwidth = 2;
            panel.add(lblError, gbc);
            etiquetasError.put(nombre, lblError);
            gbc.gridwidth = 1;
            
            return fila + 2;
        }
        
        void limpiarErrores() {
            for (JLabel etiqueta : etiquetas.values()) {
                if (etiqueta != null) {
                    etiqueta.setForeground(COLOR_CAMPO_NORMAL);
                }
            }
            
            for (JTextField campo : campos.values()) {
                if (campo != null) {
                    campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                }
            }
            
            for (JLabel error : etiquetasError.values()) {
                if (error != null) {
                    error.setText("");
                    error.setVisible(false);
                }
            }
        }
        
        void mostrarError(String nombreCampo, String mensaje) {
            JLabel etiqueta = etiquetas.get(nombreCampo);
            if (etiqueta != null) {
                etiqueta.setForeground(COLOR_CAMPO_ERROR);
            }
            
            JTextField campo = campos.get(nombreCampo);
            if (campo != null) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_ERROR, 2),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
                campo.requestFocusInWindow();
            }
            
            JLabel lblError = etiquetasError.get(nombreCampo);
            if (lblError != null) {
                lblError.setText(mensaje);
                lblError.setVisible(true);
            }
        }
        
        Map<String, String> capturarDatos() {
            Map<String, String> datos = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : campos.entrySet()) {
                datos.put(entry.getKey(), entry.getValue().getText().trim());
            }
            datos.put("gradoAspira", comboGrado.getSelectedItem().toString());
            return datos;
        }
    }
    
    // Constructores
    public PreinscripcionFrame(JFrame padre) {
        super(padre, "Formulario de preinscripción", true);
        this.controlador = new PreinscripcionController();
        inicializarDatos();
        inicializarUI();
    }
    
    private void inicializarDatos() {
        this.datosAcudienteCapturados = new HashMap<>();
        this.datosEstudiantesCapturados = new ArrayList<>();
        this.camposAcudiente = new HashMap<>();
        this.etiquetasAcudiente = new HashMap<>();
        this.etiquetasErrorAcudiente = new HashMap<>();
        this.panelesEstudiantes = new ArrayList<>();
    }
    
    private void inicializarUI() {
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                manejarCierreDialogo();
            }
        });
    }
    
    public void mostrarFormulario() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(CF);
        
        // Panel con scroll
        panelContenidoScroll = new JPanel();
        panelContenidoScroll.setLayout(new BoxLayout(panelContenidoScroll, BoxLayout.Y_AXIS));
        panelContenidoScroll.setBackground(CF);
        
        // Agregar sección de acudiente
        panelContenidoScroll.add(crearSeccionAcudiente());
        
        // Agregar primer estudiante
        agregarPanelEstudiante();
        
        // Scroll
        JScrollPane scrollPane = new JScrollPane(panelContenidoScroll);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CF);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);
        
        getContentPane().removeAll();
        getContentPane().add(panelPrincipal);
        
        setVisible(true);
    }
    
    private JPanel crearSeccionAcudiente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CB, 2),
                "Datos del Acudiente",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14),
                CT
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int fila = 0;
        
        JLabel lblBienvenida = new JLabel(
            "<html><div style='text-align: center;'><b>¡Gracias por estar interesado en nuestra institución!</b><br>" +
            "Por favor diligencia tus datos personales correctamente.</div></html>"
        );
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 16));
        lblBienvenida.setForeground(CT);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        panel.add(lblBienvenida, gbc);
        gbc.gridwidth = 1;
        
        fila = agregarCampoAcudiente(panel, gbc, fila, "Primer Nombre", "primerNombre", true);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Segundo Nombre", "segundoNombre", false);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Primer Apellido", "primerApellido", true);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Segundo Apellido", "segundoApellido", false);
        fila = agregarCampoAcudiente(panel, gbc, fila, "NUIP", "nuip", true);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Edad", "edad", true);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Correo electrónico", "correoElectronico", true);
        fila = agregarCampoAcudiente(panel, gbc, fila, "Teléfono", "telefono", true);
        
        return panel;
    }
    
    private int agregarCampoAcudiente(JPanel panel, GridBagConstraints gbc, 
                            int fila, String etiqueta, String nombre, boolean obligatorio) {
        JLabel lbl = new JLabel(etiqueta + (obligatorio ? " (*)" : ""));
        lbl.setForeground(COLOR_CAMPO_NORMAL);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lbl, gbc);
        etiquetasAcudiente.put(nombre, lbl);
        
        JTextField txt = new JTextField(20);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txt.setBackground(new Color(255, 255, 255, 200));
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(txt, gbc);
        camposAcudiente.put(nombre, txt);
        
        JLabel lblError = new JLabel("");
        lblError.setForeground(COLOR_ERROR);
        lblError.setFont(new Font("Arial", Font.PLAIN, 10));
        lblError.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = fila + 1;
        gbc.gridwidth = 2;
        panel.add(lblError, gbc);
        etiquetasErrorAcudiente.put(nombre, lblError);
        gbc.gridwidth = 1;
        
        return fila + 2;
    }
    
    private void agregarPanelEstudiante() {
        int numeroEstudiante = panelesEstudiantes.size() + 1;
        PanelEstudiante panelEst = new PanelEstudiante(numeroEstudiante);
        panelesEstudiantes.add(panelEst);
        panelContenidoScroll.add(panelEst.panel);
        panelContenidoScroll.revalidate();
        panelContenidoScroll.repaint();
        
        actualizarEstadoBotones();
    }
    
    private void actualizarEstadoBotones() {
        int cantidadActual = panelesEstudiantes.size();
        int maximo = controlador.obtenerMaximoEstudiantes();
        
        if (btnAgregarEstudiante != null) {
            btnAgregarEstudiante.setEnabled(cantidadActual < maximo);
        }
        
        if (lblContadorEstudiantes != null) {
            lblContadorEstudiantes.setText("Estudiantes: " + cantidadActual + " de " + maximo);
        }
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(CF);
        
        // Panel izquierdo con contador
        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelIzquierdo.setBackground(CF);
        lblContadorEstudiantes = new JLabel("Estudiantes: 1 de " + controlador.obtenerMaximoEstudiantes());
        lblContadorEstudiantes.setFont(new Font("Arial", Font.BOLD, 14));
        lblContadorEstudiantes.setForeground(CT);
        panelIzquierdo.add(lblContadorEstudiantes);
        
        // Panel centro con botón agregar
        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCentro.setBackground(CF);
        btnAgregarEstudiante = new JButton("Agregar otro estudiante");
        btnAgregarEstudiante.setBackground(CB);
        btnAgregarEstudiante.setForeground(CT);
        btnAgregarEstudiante.setFont(new Font("Arial", Font.BOLD, 14));
        btnAgregarEstudiante.setFocusPainted(false);
        btnAgregarEstudiante.setBorderPainted(false);
        btnAgregarEstudiante.addActionListener(e -> agregarPanelEstudiante());
        panelCentro.add(btnAgregarEstudiante);
        
        // Panel derecho con botones de acción
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelDerecho.setBackground(CF);
        
        JButton btnVolver = new JButton("Volver");
        btnVolver.setBackground(CB);
        btnVolver.setForeground(CT);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 14));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.addActionListener(e -> manejarVolver());
        
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setBackground(CB);
        btnEnviar.setForeground(CT);
        btnEnviar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.addActionListener(e -> manejarEnviar());
        
        panelDerecho.add(btnVolver);
        panelDerecho.add(btnEnviar);
        
        panel.add(panelIzquierdo, BorderLayout.WEST);
        panel.add(panelCentro, BorderLayout.CENTER);
        panel.add(panelDerecho, BorderLayout.EAST);
        
        return panel;
    }
    
    private void manejarVolver() {
        String[] opciones = {"Seguir diligenciando formulario", "Salir"};
        
        int seleccion = JOptionPane.showOptionDialog(
            this,
            "<html><center><h2>¡Espera!</h2>" +
            "Tu preinscripción no se guardará si sales ahora</center></html>",
            "Advertencia",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            opciones,
            opciones[0]
        );
        
        if (seleccion == 1) {
            limpiarDatosTemporales();
            dispose();
        }
    }
    
    private void manejarEnviar() {
        limpiarTodosLosErrores();
        
        // Validar acudiente
        Map<String, String> datosAcudiente = capturarDatosAcudiente();
        ResultadoOperacion resultadoAcudiente = controlador.validarDatosAcudiente(
            datosAcudiente.get("primerNombre"),
            datosAcudiente.get("segundoNombre"),
            datosAcudiente.get("primerApellido"),
            datosAcudiente.get("segundoApellido"),
            datosAcudiente.get("nuip"),
            datosAcudiente.get("edad"),
            datosAcudiente.get("correoElectronico"),
            datosAcudiente.get("telefono")
        );
        
        if (!resultadoAcudiente.isExitoso()) {
            mostrarErrorEnCampoAcudiente(resultadoAcudiente.getCampoError(), 
                                        resultadoAcudiente.getMensaje());
            return;
        }
        
        // Validar todos los estudiantes
        List<Map<String, String>> todosLosEstudiantes = new ArrayList<>();
        Set<String> nuipsRegistrados = new HashSet<>();
        
        for (int i = 0; i < panelesEstudiantes.size(); i++) {
            PanelEstudiante panelEst = panelesEstudiantes.get(i);
            panelEst.limpiarErrores();
            
            Map<String, String> datosEst = panelEst.capturarDatos();
            
            // Validar duplicados locales
            String nuip = datosEst.get("nuip");
            if (nuipsRegistrados.contains(nuip)) {
                panelEst.mostrarError("nuip", 
                    "Ya has registrado un estudiante con este NUIP en esta preinscripción");
                return;
            }
            
            // Validar con el controlador
            ResultadoOperacion resultadoEst = controlador.validarDatosEstudiante(
                datosEst.get("primerNombre"),
                datosEst.get("segundoNombre"),
                datosEst.get("primerApellido"),
                datosEst.get("segundoApellido"),
                datosEst.get("edad"),
                datosEst.get("nuip"),
                datosEst.get("gradoAspira")
            );
            
            if (!resultadoEst.isExitoso()) {
                panelEst.mostrarError(resultadoEst.getCampoError(), resultadoEst.getMensaje());
                return;
            }
            
            nuipsRegistrados.add(nuip);
            todosLosEstudiantes.add(datosEst);
        }
        
        // Registrar preinscripción
        ResultadoOperacion resultado = controlador.registrarPreinscripcion(
            datosAcudiente,
            todosLosEstudiantes
        );
        
        if (resultado.isExitoso()) {
            mostrarMensajeExito(
                "¡Tu formulario fue enviado correctamente!",
                "Por favor espera hasta que la institución se comunique contigo"
            );
            limpiarDatosTemporales();
            dispose();
        } else {
            mostrarMensajeError("Error", resultado.getMensaje());
        }
    }
    
    private void manejarCierreDialogo() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cancelar? Se perderán todos los datos.",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (respuesta == JOptionPane.YES_OPTION) {
            limpiarDatosTemporales();
            dispose();
        }
    }
    
    private Map<String, String> capturarDatosAcudiente() {
        Map<String, String> datos = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : camposAcudiente.entrySet()) {
            datos.put(entry.getKey(), entry.getValue().getText().trim());
        }
        return datos;
    }
    
    private void limpiarTodosLosErrores() {
        // Limpiar errores del acudiente
        for (JLabel etiqueta : etiquetasAcudiente.values()) {
            if (etiqueta != null) {
                etiqueta.setForeground(COLOR_CAMPO_NORMAL);
            }
        }
        
        for (JTextField campo : camposAcudiente.values()) {
            if (campo != null) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, CT),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        }
        
        for (JLabel error : etiquetasErrorAcudiente.values()) {
            if (error != null) {
                error.setText("");
                error.setVisible(false);
            }
        }
        
        // Limpiar errores de todos los estudiantes
        for (PanelEstudiante panelEst : panelesEstudiantes) {
            panelEst.limpiarErrores();
        }
    }
    
    private void mostrarErrorEnCampoAcudiente(String nombreCampo, String mensaje) {
        JLabel etiqueta = etiquetasAcudiente.get(nombreCampo);
        if (etiqueta != null) {
            etiqueta.setForeground(COLOR_CAMPO_ERROR);
        }
        
        JTextField campo = camposAcudiente.get(nombreCampo);
        if (campo != null) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ERROR, 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
            campo.requestFocusInWindow();
        }
        
        JLabel lblError = etiquetasErrorAcudiente.get(nombreCampo);
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
        }
    }
    
    private void mostrarMensajeExito(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            "<html><center><h2>" + titulo + "</h2><br>" + mensaje + "</center></html>",
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void mostrarMensajeError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            "<html><center><h2>" + titulo + "</h2><br>" + mensaje + "</center></html>",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void limpiarDatosTemporales() {
        datosAcudienteCapturados.clear();
        datosEstudiantesCapturados.clear();
        camposAcudiente.clear();
        etiquetasAcudiente.clear();
        etiquetasErrorAcudiente.clear();
        panelesEstudiantes.clear();
    }
}