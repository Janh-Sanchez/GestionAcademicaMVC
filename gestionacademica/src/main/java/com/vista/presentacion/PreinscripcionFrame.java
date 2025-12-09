package com.vista.presentacion;

import com.controlador.PreinscripcionController;
import com.modelo.dominio.Acudiente;
import com.modelo.dominio.ResultadoOperacion;
import com.modelo.dtos.AcudienteDTO;
import com.modelo.dtos.EstudianteDTO;

import jakarta.persistence.EntityManager;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PreinscripcionFrame {
    private final PreinscripcionController controlador;

    // Datos temporales capturados del usuario 
    private Map<String, String> datosAcudienteCapturados;
    private List<Map<String, String>> datosEstudiantesCapturados;
    
    // Referencias a componentes UI actuales
    private JPanel panelFormularioActual;
    private Map<String, JTextField> camposActuales;
    private Map<String, JLabel> etiquetasActuales;
    private Map<String, JLabel> etiquetasErrorActuales;

    private Map<String, JTextField> mapaCamposActual;
    private Map<String, JLabel> mapaEtiquetasActual;
    private Map<String, JLabel> mapaErroresActual;
    
    // Colores de la UI
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_CAMPO_NORMAL = Color.BLACK;
    private static final Color COLOR_CAMPO_ERROR = Color.RED;
    private static final Color BORDER_NORMAL = new Color(204, 204, 204);
    private static final Color BORDER_ERROR = new Color(220, 53, 69);
    

    public PreinscripcionFrame(EntityManager entityManager) {
        this.controlador = new PreinscripcionController(entityManager);
        this.datosAcudienteCapturados = new HashMap<>();
        this.datosEstudiantesCapturados = new ArrayList<>();
        this.mapaCamposActual = new HashMap<>();
        this.mapaEtiquetasActual = new HashMap<>();
        this.mapaErroresActual = new HashMap<>();
        this.camposActuales = new HashMap<>();
        this.etiquetasActuales = new HashMap<>();
        this.etiquetasErrorActuales = new HashMap<>();
    }
    
    /**
     * Muestra el formulario inicial de preinscripción
     */
    public void mostrarFormularioPreinscripcion() {
        if (datosEstudiantesCapturados.isEmpty()) {
            limpiarDatosTemporales();
        }
        
        JDialog dialog = crearDialogoFormulario();
        dialog.setVisible(true);
    }
    
    // ============================================
    // CREACIÓN DE COMPONENTES UI
    // ============================================
    
    /**
     * Crea el diálogo del formulario
     */
    private JDialog crearDialogoFormulario() {
        JDialog dialog = new JDialog((Frame) null, "Formulario de registro", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(600, 750);
        dialog.setLocationRelativeTo(null);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelFormularioActual = crearPanelFormulario();
        panelPrincipal.add(panelFormularioActual, BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(dialog), BorderLayout.SOUTH);
        
        dialog.add(panelPrincipal);
        return dialog;
    }
    
    /**
     * Crea el panel del formulario con todos los campos
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        camposActuales.clear();
        etiquetasActuales.clear();
        etiquetasErrorActuales.clear();
        
        int fila = 0;
        
        // Mensaje de bienvenida
        fila = agregarEncabezado(panel, gbc, fila,
            "¡Gracias por estar interesado en nuestra institución!",
            "Por favor diligencia tus datos personales correctamente.");
        
        // Campos del acudiente
        fila = agregarSeccionAcudiente(panel, gbc, fila);
        
        // Separador
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        // Campos del estudiante
        fila = agregarSeccionEstudiante(panel, gbc, fila);
        
        return panel;
    }
    
    /**
     * Agrega encabezado al formulario
     */
    private int agregarEncabezado(JPanel panel, GridBagConstraints gbc, 
                                   int fila, String titulo, String subtitulo) {
        JLabel lblBienvenida = new JLabel(
            "<html><b>" + titulo + "</b><br>" + subtitulo + "</html>"
        );
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        panel.add(lblBienvenida, gbc);
        gbc.gridwidth = 1;
        return fila + 1;
    }
    
    /**
     * Agrega los campos de la sección de acudiente
     */
    private int agregarSeccionAcudiente(JPanel panel, GridBagConstraints gbc, int fila) {
        fila = agregarCampo(panel, gbc, fila, "Primer Nombre", "primerNombre", true);
        fila = agregarCampo(panel, gbc, fila, "Segundo Nombre", "segundoNombre", false);
        fila = agregarCampo(panel, gbc, fila, "Primer Apellido", "primerApellido", true);
        fila = agregarCampo(panel, gbc, fila, "Segundo Apellido", "segundoApellido", false);
        fila = agregarCampo(panel, gbc, fila, "NUIP", "nuip", true);
        fila = agregarCampo(panel, gbc, fila, "Edad", "edad", true);
        fila = agregarCampo(panel, gbc, fila, "Correo electrónico", "correoElectronico", true);
        fila = agregarCampo(panel, gbc, fila, "Teléfono", "telefono", true);
        return fila;
    }
    
    /**
     * Agrega los campos de la sección de estudiante
     */
    private int agregarSeccionEstudiante(JPanel panel, GridBagConstraints gbc, int fila) {
        JLabel lblEstudiante = new JLabel(
            "<html><b>Por favor, diligencia los datos del estudiante</b><br>" +
            "Si vas a registrar más de un estudiante, continúa al siguiente paso.</html>"
        );
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        panel.add(lblEstudiante, gbc);
        gbc.gridwidth = 1;
        
        fila = agregarCampo(panel, gbc, fila, "Primer nombre", "est_primerNombre", true);
        fila = agregarCampo(panel, gbc, fila, "Segundo Nombre", "est_segundoNombre", false);
        fila = agregarCampo(panel, gbc, fila, "Primer Apellido", "est_primerApellido", true);
        fila = agregarCampo(panel, gbc, fila, "Segundo Apellido", "est_segundoApellido", false);
        fila = agregarCampo(panel, gbc, fila, "Edad", "est_edad", true);
        fila = agregarCampo(panel, gbc, fila, "NUIP", "est_nuip", true);
        
        // ComboBox de grados
        fila = agregarComboGrados(panel, gbc, fila);
        
        // Nota de campos obligatorios
        JLabel lblObligatorios = new JLabel(
            "<html><font color='red'>(*) Campos Obligatorios</font></html>"
        );
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        panel.add(lblObligatorios, gbc);
        
        return fila + 1;
    }
    
    /**
     * Agrega un campo de texto al formulario
     */
    private int agregarCampo(JPanel panel, GridBagConstraints gbc, 
                            int fila, String etiqueta, String nombre, 
                            boolean obligatorio) {
        // Etiqueta
        JLabel lbl = new JLabel(etiqueta + (obligatorio ? " (*)" : ""));
        lbl.setForeground(COLOR_CAMPO_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lbl, gbc);
        etiquetasActuales.put(nombre, lbl);
        
        // Campo de texto
        JTextField txt = new JTextField(20);
        txt.setName(nombre);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_NORMAL, 1),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        
        // Restaurar datos si existen
        restaurarDatosCampo(txt, nombre);
        
        gbc.gridx = 1;
        panel.add(txt, gbc);
        camposActuales.put(nombre, txt);
        
        // Etiqueta de error
        JLabel lblError = new JLabel("");
        lblError.setForeground(COLOR_ERROR);
        lblError.setFont(new Font("Arial", Font.PLAIN, 10));
        lblError.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = fila + 1;
        gbc.gridwidth = 2;
        panel.add(lblError, gbc);
        etiquetasErrorActuales.put(nombre, lblError);
        gbc.gridwidth = 1;
        
        return fila + 2;
    }
    
    /**
     * Agrega el combo de grados
     */
    private int agregarComboGrados(JPanel panel, GridBagConstraints gbc, int fila) {
        JLabel lblGrado = new JLabel("Grado al que aspira (*)");
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lblGrado, gbc);
        etiquetasActuales.put("est_gradoAspira", lblGrado);
        
        // Solicitar grados al controlador
        ResultadoOperacion resultado = controlador.obtenerGradosDisponibles();
        String[] grados;
        
        if (resultado.isExitoso() && resultado.getDatos() != null) {
            @SuppressWarnings("unchecked")
            List<String> listaGrados = (List<String>) resultado.getDatos();
            grados = listaGrados.toArray(new String[0]);
        } else {
            // Fallback si no se pueden obtener los grados
            grados = new String[]{"Párvulos", "Caminadores", "Pre-Jardín"};
        }
        
        JComboBox<String> cmbGrado = new JComboBox<>(grados);
        cmbGrado.setName("est_gradoAspira");
        gbc.gridx = 1;
        panel.add(cmbGrado, gbc);
        
        // Etiqueta de error para el combo
        JLabel lblError = new JLabel("");
        lblError.setForeground(COLOR_ERROR);
        lblError.setFont(new Font("Arial", Font.PLAIN, 10));
        lblError.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = fila + 1;
        gbc.gridwidth = 2;
        panel.add(lblError, gbc);
        etiquetasErrorActuales.put("est_gradoAspira", lblError);
        
        return fila + 2;
    }
    
    /**
     * Crea el panel de botones
     */
    private JPanel crearPanelBotones(JDialog dialog) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> manejarCancelacion(dialog));
        
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.addActionListener(e -> manejarContinuar(dialog));
        
        panel.add(btnCancelar);
        panel.add(btnContinuar);
        
        return panel;
    }
    
    // ============================================
    // MANEJO DE EVENTOS DEL USUARIO
    // ============================================
    
    /**
     * Maneja el evento de cancelación
     */
    private void manejarCancelacion(JDialog dialog) {
        int respuesta = JOptionPane.showConfirmDialog(
            dialog,
            "¿Está seguro que desea cancelar? Se perderán todos los datos.",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (respuesta == JOptionPane.YES_OPTION) {
            limpiarDatosTemporales();
            dialog.dispose();
        }
    }
    
    /**
     * Maneja el evento de continuar
     */
    private void manejarContinuar(JDialog dialog) {
        limpiarErroresVisuales();
        
        // 1. CAPTURAR datos del formulario (responsabilidad de la VISTA)
        Map<String, String> datosAcudiente = capturarDatosAcudiente();
        Map<String, String> datosEstudiante = capturarDatosEstudiante();
        
        // 2. Crear DTOs para enviar al CONTROLADOR
        AcudienteDTO dtoAcudiente = crearDTOAcudiente(datosAcudiente);
        EstudianteDTO dtoEstudiante = crearDTOEstudiante(datosEstudiante);
        
        // 3. ENVIAR al CONTROLADOR para validar
        ResultadoOperacion resultadoAcudiente = controlador.validarAcudiente(dtoAcudiente);
        
        if (!resultadoAcudiente.isExitoso()) {
            mostrarErrorEnCampo(resultadoAcudiente.getCampoError(), 
                               resultadoAcudiente.getMensaje());
            return;
        }
        
        ResultadoOperacion resultadoEstudiante = controlador.validarEstudiante(dtoEstudiante);
        
        if (!resultadoEstudiante.isExitoso()) {
            mostrarErrorEnCampo("est_" + resultadoEstudiante.getCampoError(), 
                               resultadoEstudiante.getMensaje());
            return;
        }
        
        // 4. Si todo es válido, guardar temporalmente y continuar
        datosAcudienteCapturados = datosAcudiente;
        
        if (datosEstudiantesCapturados.isEmpty()) {
            datosEstudiantesCapturados.clear();
        }
        datosEstudiantesCapturados.add(datosEstudiante);
        
        dialog.dispose();
        mostrarOpcionesPostFormulario();
    }
    
    /**
     * Captura los datos del acudiente del formulario
     */
    private Map<String, String> capturarDatosAcudiente() {
        Map<String, String> datos = new HashMap<>();
        
        for (Map.Entry<String, JTextField> entry : camposActuales.entrySet()) {
            String nombre = entry.getKey();
            JTextField campo = entry.getValue();
            
            if (campo != null && !nombre.startsWith("est_")) {
                datos.put(nombre, campo.getText().trim());
            }
        }
        
        return datos;
    }
    
    /**
     * Captura los datos del estudiante del formulario
     */
    private Map<String, String> capturarDatosEstudiante() {
        Map<String, String> datos = new HashMap<>();
        
        // Capturar campos de texto
        for (Map.Entry<String, JTextField> entry : camposActuales.entrySet()) {
            String nombre = entry.getKey();
            JTextField campo = entry.getValue();
            
            if (campo != null && nombre.startsWith("est_")) {
                String nombreLimpio = nombre.substring(4); // quitar "est_"
                datos.put(nombreLimpio, campo.getText().trim());
            }
        }
        
        // Capturar combo de grado
        for (Component comp : panelFormularioActual.getComponents()) {
            if (comp instanceof JComboBox && comp.getName() != null && 
                comp.getName().equals("est_gradoAspira")) {
                JComboBox<?> cmb = (JComboBox<?>) comp;
                Object selected = cmb.getSelectedItem();
                datos.put("gradoAspira", selected != null ? selected.toString() : "");
                break;
            }
        }
        
        return datos;
    }
    
    /**
     * Crea un DTO de acudiente desde el mapa de datos
     */
    private AcudienteDTO crearDTOAcudiente(Map<String, String> datos) {
        return new AcudienteDTO(
            datos.get("nuip"),
            datos.get("primerNombre"),
            datos.get("segundoNombre"),
            datos.get("primerApellido"),
            datos.get("segundoApellido"),
            parseIntegerSafe(datos.get("edad")),
            datos.get("correoElectronico"),
            datos.get("telefono")
        );
    }
    
    /**
     * Crea un DTO de estudiante desde el mapa de datos
     */
    private EstudianteDTO crearDTOEstudiante(Map<String, String> datos) {
        return new EstudianteDTO(
            datos.get("primerNombre"),
            datos.get("segundoNombre"),
            datos.get("primerApellido"),
            datos.get("segundoApellido"),
            parseIntegerSafe(datos.get("edad")),
            datos.get("nuip"),
            datos.get("gradoAspira")
        );
    }
    
    // ============================================
    // OPCIONES POST-FORMULARIO
    // ============================================
    private void mostrarOpcionesPostFormulario() {
        int cantidadEstudiantes = datosEstudiantesCapturados.size();
        boolean maximoAlcanzado = cantidadEstudiantes >= Acudiente.MAX_ESTUDIANTES;
        
        String mensaje;
        String[] opciones;
        
        if (maximoAlcanzado) {
            mensaje = "<html><center><h3>¡Ha alcanzado el límite máximo!</h3>" +
                    "Estudiantes registrados: <b>" + cantidadEstudiantes + "</b><br>" +
                    "Límite: " + Acudiente.MAX_ESTUDIANTES + " estudiantes<br><br>" +
                    "¿Qué desea hacer?</center></html>";
            opciones = new String[]{"Volver", "Enviar"};
        } else {
            int cuposRestantes = controlador.obtenerCuposRestantes(cantidadEstudiantes);
            mensaje = "<html><center><h3>Estudiante registrado exitosamente</h3>" +
                    "Estudiantes registrados: <b>" + cantidadEstudiantes + "</b><br>" +
                    "Cupos restantes: <b>" + cuposRestantes + "</b><br><br>" +
                    "¿Qué desea hacer?</center></html>";
            opciones = new String[]{"Volver", "Agregar otro estudiante", "Enviar"};
        }
        
        int seleccion = JOptionPane.showOptionDialog(
            null,
            mensaje,
            "Opciones de preinscripción",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[opciones.length - 1]
        );
        
        if (maximoAlcanzado) {
            switch (seleccion) {
                case 0 -> mostrarAdvertenciaSalir();
                case 1 -> enviarPreinscripcion();
            }
        } else {
            switch (seleccion) {
                case 0 -> mostrarAdvertenciaSalir();
                case 1 -> mostrarFormularioEstudianteAdicional();
                case 2 -> enviarPreinscripcion();
                default -> mostrarOpcionesPostFormulario(); // Por si el usuario cierra el diálogo
            }
        }
    }
    
    /**
     * Muestra formulario para agregar estudiante adicional
     */
    private void mostrarFormularioEstudianteAdicional() {
        int numeroEstudiante = datosEstudiantesCapturados.size() + 1;
        
        // Verificar límite
        if (numeroEstudiante > Acudiente.MAX_ESTUDIANTES) {
            JOptionPane.showMessageDialog(null,
                "Ha alcanzado el límite máximo de " + Acudiente.MAX_ESTUDIANTES + " estudiantes",
                "Límite alcanzado",
                JOptionPane.WARNING_MESSAGE);
            mostrarOpcionesPostFormulario();
            return;
        }
        
        JDialog dialog = new JDialog((Frame) null, 
            "Agregar Estudiante #" + numeroEstudiante, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        JLabel lblTitulo = new JLabel(
            "<html><h3>Estudiante " + numeroEstudiante + " de " + Acudiente.MAX_ESTUDIANTES + "</h3>" +
            "Complete los datos del estudiante adicional</html>"
        );
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;
        
        // Campos del formulario
        int fila = 1;
        fila = agregarCampoFormulario(panel, gbc, fila, "Primer nombre (*)", "primerNombre");
        fila = agregarCampoFormulario(panel, gbc, fila, "Segundo Nombre", "segundoNombre");
        fila = agregarCampoFormulario(panel, gbc, fila, "Primer Apellido (*)", "primerApellido");
        fila = agregarCampoFormulario(panel, gbc, fila, "Segundo Apellido", "segundoApellido");
        fila = agregarCampoFormulario(panel, gbc, fila, "Edad (*)", "edad");
        fila = agregarCampoFormulario(panel, gbc, fila, "NUIP (*)", "nuip");
        
        // Combo de grados
        JLabel lblGrado = new JLabel("Grado al que aspira (*)");
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lblGrado, gbc);
        
        // Obtener grados del controlador
        ResultadoOperacion resultadoGrados = controlador.obtenerGradosDisponibles();
        String[] grados;
        
        if (resultadoGrados.isExitoso() && resultadoGrados.getDatos() != null) {
            @SuppressWarnings("unchecked")
            List<String> listaGrados = (List<String>) resultadoGrados.getDatos();
            grados = listaGrados.toArray(new String[0]);
        } else {
            grados = new String[]{"Párvulos", "Caminadores", "Pre-Jardín"};
        }
        
        JComboBox<String> cmbGrado = new JComboBox<>(grados);
        cmbGrado.setName("gradoAspira");
        gbc.gridx = 1;
        gbc.gridy = fila;
        panel.add(cmbGrado, gbc);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            if (confirmarCancelacion(dialog)) {
                dialog.dispose();
            }
        });
        
        JButton btnAgregar = new JButton("Agregar Estudiante");
        btnAgregar.addActionListener(e -> {
            if (procesarEstudianteAdicional(dialog, cmbGrado)) {
                dialog.dispose();
                mostrarOpcionesPostFormulario();
            }
        });
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAgregar);
        
        // Configurar diálogo
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmarCancelacion(dialog);
            }
        });
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Agrega un campo al formulario de estudiante adicional
     */
    private int agregarCampoFormulario(JPanel panel, GridBagConstraints gbc, 
                                    int fila, String etiqueta, String nombre) {
        boolean esObligatorio = etiqueta.contains("(*)");
        
        // Etiqueta
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(esObligatorio ? Color.RED : Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lbl, gbc);
        
        // Campo de texto
        JTextField txt = new JTextField(20);
        txt.setName(nombre);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_NORMAL, 1),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        gbc.gridx = 1;
        gbc.gridy = fila;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txt, gbc);
        
        // Etiqueta de error
        JLabel lblError = new JLabel("");
        lblError.setForeground(COLOR_ERROR);
        lblError.setFont(new Font("Arial", Font.PLAIN, 10));
        lblError.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = fila + 1;
        gbc.gridwidth = 2;
        panel.add(lblError, gbc);
        gbc.gridwidth = 1;
        
        // Guardar referencias
        mapaCamposActual.put(nombre, txt);
        mapaEtiquetasActual.put(nombre, lbl);
        mapaErroresActual.put(nombre, lblError);
        
        return fila + 2;
    }

    /**
     * Procesa el estudiante adicional
     */
    private boolean procesarEstudianteAdicional(JDialog dialog, JComboBox<String> cmbGrado) {
        limpiarErroresAdicionales();
        
        // Capturar datos del formulario
        Map<String, String> datosEstudiante = new HashMap<>();
        
        for (Map.Entry<String, JTextField> entry : mapaCamposActual.entrySet()) {
            String nombre = entry.getKey();
            JTextField txt = entry.getValue();
            datosEstudiante.put(nombre, txt.getText().trim());
        }
        
        // Capturar grado seleccionado
        datosEstudiante.put("nombreGrado", cmbGrado.getSelectedItem().toString());
        
        // Crear DTO para validación
        EstudianteDTO dtoEstudiante = crearDTOEstudianteDesdeMapa(datosEstudiante);
        
        // Validar con el controlador
        ResultadoOperacion resultado = controlador.validarEstudiante(dtoEstudiante);
        
        if (!resultado.isExitoso()) {
            mostrarErrorEnCampoAdicional(resultado.getCampoError(), resultado.getMensaje());
            return false;
        }
        
        // Agregar a la lista de estudiantes
        datosEstudiantesCapturados.add(datosEstudiante);
        
        JOptionPane.showMessageDialog(dialog,
            "Estudiante agregado exitosamente.\n" +
            "Estudiantes registrados: " + datosEstudiantesCapturados.size() + 
            " de " + Acudiente.MAX_ESTUDIANTES,
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE);
        
        return true;
    }

    /**
     * Confirma la cancelación
     */
    private boolean confirmarCancelacion(JDialog dialog) {
        int respuesta = JOptionPane.showConfirmDialog(
            dialog,
            "¿Está seguro que desea cancelar la adición de este estudiante?",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (respuesta == JOptionPane.YES_OPTION) {
            dialog.dispose();
            mostrarOpcionesPostFormulario();
            return true;
        }
        return false;
    }

    /**
     * Limpia errores del formulario adicional
     */
    private void limpiarErroresAdicionales() {
        for (JLabel etiqueta : mapaEtiquetasActual.values()) {
            if (etiqueta != null) {
                etiqueta.setForeground(Color.BLACK);
            }
        }
        
        for (JTextField campo : mapaCamposActual.values()) {
            if (campo != null) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_NORMAL, 1),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
            }
        }
        
        for (JLabel error : mapaErroresActual.values()) {
            if (error != null) {
                error.setText("");
                error.setVisible(false);
            }
        }
    }

    /**
     * Muestra error en campo adicional
     */
    private void mostrarErrorEnCampoAdicional(String nombreCampo, String mensaje) {
        // Cambiar color de etiqueta
        JLabel etiqueta = mapaEtiquetasActual.get(nombreCampo);
        if (etiqueta != null) {
            etiqueta.setForeground(COLOR_CAMPO_ERROR);
        }
        
        // Cambiar borde del campo
        JTextField campo = mapaCamposActual.get(nombreCampo);
        if (campo != null) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ERROR, 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
            campo.requestFocusInWindow();
        }
        
        // Mostrar mensaje de error
        JLabel lblError = mapaErroresActual.get(nombreCampo);
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
        }
    }

    /**
     * Crea DTO de estudiante desde mapa de datos
     */
    private EstudianteDTO crearDTOEstudianteDesdeMapa(Map<String, String> datos) {
        return new EstudianteDTO(
            datos.get("primerNombre"),
            datos.get("segundoNombre"),
            datos.get("primerApellido"),
            datos.get("segundoApellido"),
            parseIntegerSafe(datos.get("edad")),
            datos.get("nuip"),
            datos.get("nombreGrado")
        );
    }

    /**
     * Muestra advertencia al intentar salir
     */
    private void mostrarAdvertenciaSalir() {
        String[] opciones = {"Seguir diligenciando formulario", "Salir"};
        
        int seleccion = JOptionPane.showOptionDialog(
            null,
            "<html><center><h2>¡Espera!</h2>" +
            "Tu preinscripción no se guardará si sales ahora</center></html>",
            "Advertencia",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            opciones,
            opciones[0]
        );
        
        if (seleccion == 0) {
            mostrarOpcionesPostFormulario();
        } else {
            limpiarDatosTemporales();
        }
    }
    
    /**
     * Envía la preinscripción completa al controlador
     */
    private void enviarPreinscripcion() {
        // 1. Crear DTO del acudiente
        AcudienteDTO dtoAcudiente = crearDTOAcudiente(datosAcudienteCapturados);
        
        // 2. Crear lista de DTOs de estudiantes
        List<EstudianteDTO> dtosEstudiantes = new ArrayList<>();
        for (Map<String, String> datosEst : datosEstudiantesCapturados) {
            dtosEstudiantes.add(crearDTOEstudiante(datosEst));
        }
        
        // 3. ENVIAR al CONTROLADOR
        ResultadoOperacion resultado = controlador.registrarPreinscripcion(
            dtoAcudiente, dtosEstudiantes
        );
        
        // 4. MOSTRAR resultado al usuario
        if (resultado.isExitoso()) {
            mostrarMensajeExito(
                "¡Tu formulario fue enviado correctamente!",
                "Por favor espera hasta que la institución se comunique contigo"
            );
            limpiarDatosTemporales();
        } else {
            mostrarMensajeError("Error", resultado.getMensaje());
        }
    }
    
    // ============================================
    // MÉTODOS AUXILIARES DE UI
    // ============================================
    
    /**
     * Muestra un error visual en un campo específico
     */
    private void mostrarErrorEnCampo(String nombreCampo, String mensaje) {
        // Cambiar color de etiqueta
        JLabel etiqueta = etiquetasActuales.get(nombreCampo);
        if (etiqueta != null) {
            etiqueta.setForeground(COLOR_CAMPO_ERROR);
        }
        
        // Cambiar borde del campo
        JTextField campo = camposActuales.get(nombreCampo);
        if (campo != null) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ERROR, 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
            campo.requestFocusInWindow();
        }
        
        // Mostrar mensaje de error
        JLabel lblError = etiquetasErrorActuales.get(nombreCampo);
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
        }
    }
    
    /**
     * Limpia todos los errores visuales
     */
    private void limpiarErroresVisuales() {
        for (JLabel etiqueta : etiquetasActuales.values()) {
            if (etiqueta != null) {
                etiqueta.setForeground(COLOR_CAMPO_NORMAL);
            }
        }
        
        for (JTextField campo : camposActuales.values()) {
            if (campo != null) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_NORMAL, 1),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
            }
        }
        
        for (JLabel error : etiquetasErrorActuales.values()) {
            if (error != null) {
                error.setText("");
                error.setVisible(false);
            }
        }
    }
    
    /**
     * Restaura datos en un campo si existen
     */
    private void restaurarDatosCampo(JTextField campo, String nombreCampo) {
        if (nombreCampo.startsWith("est_") && !datosEstudiantesCapturados.isEmpty()) {
            Map<String, String> datosEst = datosEstudiantesCapturados.get(0);
            String campoLimpio = nombreCampo.substring(4);
            if (datosEst.containsKey(campoLimpio)) {
                campo.setText(datosEst.get(campoLimpio));
            }
        } else if (!nombreCampo.startsWith("est_") && !datosAcudienteCapturados.isEmpty()) {
            if (datosAcudienteCapturados.containsKey(nombreCampo)) {
                campo.setText(datosAcudienteCapturados.get(nombreCampo));
            }
        }
    }
    
    /**
     * Muestra mensaje de éxito
     */
    private void mostrarMensajeExito(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(
            null,
            "<html><center><h2>" + titulo + "</h2><br>" + mensaje + "</center></html>",
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Muestra mensaje de error
     */
    private void mostrarMensajeError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(
            null,
            "<html><center><h2>" + titulo + "</h2><br>" + mensaje + "</center></html>",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Limpia los datos temporales capturados
     */
    private void limpiarDatosTemporales() {
        datosAcudienteCapturados.clear();
        datosEstudiantesCapturados.clear();
        camposActuales.clear();
        etiquetasActuales.clear();
        etiquetasErrorActuales.clear();
    }
    
    /**
     * Convierte String a Integer de forma segura
     */
    private Integer parseIntegerSafe(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}