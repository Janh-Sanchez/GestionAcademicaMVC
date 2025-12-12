package com.controlador;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JFrame;

public class PreinscripcionController extends JFrame{
    private final RepositorioGenerico<Preinscripcion> repoPreinscripcion;
    private final RepositorioGenerico<Acudiente> repoAcudiente;
    private final RepositorioGenerico<Estudiante> repoEstudiante;
    private final UsuarioRepositorio usuarioRepositorio;
    private final GradoRepositorio gradoRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final EntityManager entityManager;
    
    public PreinscripcionController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.repoPreinscripcion = new RepositorioGenerico<>(entityManager, Preinscripcion.class);
        this.repoAcudiente = new RepositorioGenerico<>(entityManager, Acudiente.class);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, Estudiante.class);
        this.gradoRepositorio = new GradoRepositorio(entityManager);
        this.usuarioRepositorio = new UsuarioRepositorio(entityManager);
        this.estudianteRepositorio = new EstudianteRepositorio(entityManager);
    }
    
    /**
     * Valida datos primitivos del acudiente - Interfaz para la vista
     */
    public ResultadoOperacion validarDatosAcudiente(
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            String nuip,
            String edadStr,
            String correoElectronico,
            String telefono) {
        
        // Convertir datos primitivos
        Integer edad = parseIntegerSafe(edadStr);
        
        // 1. Validar conversión básica
        if (edadStr != null && !edadStr.trim().isEmpty() && edad == null) {
            return ResultadoOperacion.errorValidacion("edad", "La edad debe ser un número válido");
        }
        
        // 2. Crear objeto de dominio (responsabilidad controlador)
        Acudiente acudiente = new Acudiente();
        acudiente.setNuipUsuario(nuip);
        acudiente.setPrimerNombre(primerNombre);
        acudiente.setSegundoNombre(segundoNombre);
        acudiente.setPrimerApellido(primerApellido);
        acudiente.setSegundoApellido(segundoApellido);
        acudiente.setEdad(edad);
        acudiente.setCorreoElectronico(correoElectronico);
        acudiente.setTelefono(telefono);
        acudiente.setEstadoAprobacion(Estado.Pendiente);
        
        // 3. Delegar validación al MODELO
        ResultadoValidacionDominio validacion = acudiente.validar();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // 4. Verificar duplicados en repositorios
        if (usuarioRepositorio.existePorNuip(nuip)) {
            return ResultadoOperacion.errorValidacion("nuip",
                "Ya existe un usuario registrado con este NUIP");
        }
        
        if (usuarioRepositorio.existePorCorreo(correoElectronico)) {
            return ResultadoOperacion.errorValidacion("correoElectronico",
                "Ya existe un usuario registrado con este correo electrónico");
        }
        
        if (usuarioRepositorio.existePorTelefono(telefono)) {
            return ResultadoOperacion.errorValidacion("telefono",
                "Ya existe un usuario registrado con este teléfono");
        }
        
        return ResultadoOperacion.exito("Datos válidos");
    }
    
    /**
     * Valida datos primitivos del estudiante - Interfaz para la vista
     */
    public ResultadoOperacion validarDatosEstudiante(
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            String edadStr,
            String nuip,
            String nombreGrado) {
        
        // Convertir datos primitivos
        Integer edad = parseIntegerSafe(edadStr);
        
        // 1. Validar conversión básica
        if (edadStr != null && !edadStr.trim().isEmpty() && edad == null) {
            return ResultadoOperacion.errorValidacion("edad", "La edad debe ser un número válido");
        }
        
        // 2. Verificar que el grado existe
        Optional<Grado> gradoOpt = gradoRepositorio.buscarPornombreGrado(nombreGrado);
        if (!gradoOpt.isPresent()) {
            return ResultadoOperacion.errorValidacion("gradoAspira",
                "El grado seleccionado no existe");
        }
        
        // 3. Crear objeto de dominio (responsabilidad controlador)
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre(primerNombre);
        estudiante.setSegundoNombre(segundoNombre);
        estudiante.setPrimerApellido(primerApellido);
        estudiante.setSegundoApellido(segundoApellido);
        estudiante.setEdad(edad);
        estudiante.setNuip(nuip);
        estudiante.setGradoAspira(gradoOpt.get());
        
        // 4. Delegar validación al MODELO
        ResultadoValidacionDominio validacion = estudiante.validar();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // 5. Verificar duplicados en repositorios
        if (estudianteRepositorio.existePorNuip(nuip)) {
            return ResultadoOperacion.errorValidacion("nuip",
                "Ya existe un estudiante registrado con este NUIP");
        }

        return ResultadoOperacion.exito("Datos válidos");
    }
    
    // ========== MÉTODO PRINCIPAL DE REGISTRO ==========
    
    /**
     * Registra la preinscripción completa - Recibe datos primitivos de la vista
     */
    public ResultadoOperacion registrarPreinscripcion(
            Map<String, String> datosAcudiente,
            List<Map<String, String>> datosEstudiantes) {
        
        // 1. Validar entrada básica
        if (datosEstudiantes == null || datosEstudiantes.isEmpty()) {
            return ResultadoOperacion.error("Debe registrar al menos un estudiante");
        }
        
        if (datosEstudiantes.size() > Acudiente.MAX_ESTUDIANTES) {
            return ResultadoOperacion.error(
                "Solo puede inscribir máximo " + Acudiente.MAX_ESTUDIANTES + " estudiantes");
        }
        
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 2. Crear y guardar acudiente
            Acudiente acudiente = crearAcudienteDesdeDatos(datosAcudiente);
            ResultadoValidacionDominio validacionAcudiente = acudiente.validar();
            
            if (!validacionAcudiente.isValido()) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion(
                    validacionAcudiente.getCampoInvalido(),
                    validacionAcudiente.getMensajeError()
                );
            }
            
            if (usuarioRepositorio.existePorNuip(datosAcudiente.get("nuip"))) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion("nuip",
                    "Ya existe un acudiente registrado con este NUIP");
            }
            
            repoAcudiente.guardar(acudiente);
            
            // 3. Crear preinscripción
            Preinscripcion preinscripcion = new Preinscripcion();
            preinscripcion.setFechaRegistro(LocalDate.now());
            preinscripcion.setEstado(Estado.Pendiente);
            preinscripcion.setAcudiente(acudiente);
            repoPreinscripcion.guardar(preinscripcion);
            
            // 4. Procesar estudiantes
            for (Map<String, String> datosEst : datosEstudiantes) {
                Estudiante estudiante = crearEstudianteDesdeDatos(datosEst);
                
                // Validar estudiante
                ResultadoValidacionDominio validacionEst = estudiante.validar();
                if (!validacionEst.isValido()) {
                    transaction.rollback();
                    return ResultadoOperacion.errorValidacion(
                        validacionEst.getCampoInvalido(),
                        validacionEst.getMensajeError()
                    );
                }
                
                if (estudianteRepositorio.existePorNuip(datosEst.get("nuip"))) {
                    transaction.rollback();
                    return ResultadoOperacion.errorValidacion("nuip",
                        "Ya existe un estudiante registrado con este NUIP");
                }
                
                // Establecer relaciones
                estudiante.setAcudiente(acudiente);
                estudiante.setPreinscripcion(preinscripcion);
                estudiante.setEstado(Estado.Pendiente);
                
                // Agregar al acudiente (regla de negocio en dominio)
                try {
                    acudiente.agregarEstudiante(estudiante);
                } catch (Acudiente.DomainException e) {
                    transaction.rollback();
                    return ResultadoOperacion.error(e.getMessage());
                }
                
                repoEstudiante.guardar(estudiante);
            }
            
            // 5. Completar transacción
            entityManager.refresh(preinscripcion);
            transaction.commit();
            
            return ResultadoOperacion.exitoConDatos(
                "Preinscripción registrada exitosamente",
                preinscripcion.getIdPreinscripcion()
            );
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return ResultadoOperacion.error(
                "Error al guardar la preinscripción: " + e.getMessage()
            );
        }
        finally{
            entityManager.close();
        }
    }
    
    // ========== MÉTODOS AUXILIARES PARA CONSTRUIR DOMINIO ==========
    
    /**
     * Construye Acudiente desde datos primitivos - Responsabilidad controlador
     */
    private Acudiente crearAcudienteDesdeDatos(Map<String, String> datos) {
        Acudiente acudiente = new Acudiente();
        acudiente.setNuipUsuario(datos.get("nuip"));
        acudiente.setPrimerNombre(datos.get("primerNombre"));
        acudiente.setSegundoNombre(datos.get("segundoNombre"));
        acudiente.setPrimerApellido(datos.get("primerApellido"));
        acudiente.setSegundoApellido(datos.get("segundoApellido"));
        acudiente.setEdad(parseIntegerSafe(datos.get("edad")));
        acudiente.setCorreoElectronico(datos.get("correoElectronico"));
        acudiente.setTelefono(datos.get("telefono"));
        acudiente.setEstadoAprobacion(Estado.Pendiente);
        return acudiente;
    }
    
    /**
     * Construye Estudiante desde datos primitivos - Responsabilidad controlador
     */
    private Estudiante crearEstudianteDesdeDatos(Map<String, String> datos) {
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre(datos.get("primerNombre"));
        estudiante.setSegundoNombre(datos.get("segundoNombre"));
        estudiante.setPrimerApellido(datos.get("primerApellido"));
        estudiante.setSegundoApellido(datos.get("segundoApellido"));
        estudiante.setEdad(parseIntegerSafe(datos.get("edad")));
        estudiante.setNuip(datos.get("nuip"));
        
        // Buscar grado
        Optional<Grado> gradoOpt = gradoRepositorio.buscarPornombreGrado(
            datos.get("gradoAspira")
        );
        gradoOpt.ifPresent(estudiante::setGradoAspira);
        
        return estudiante;
    }
    
    // ========== MÉTODOS DE SERVICIO PARA LA VISTA ==========
    
    /**
     * Obtiene grados disponibles para mostrar en UI
     */
    public ResultadoOperacion obtenerGradosDisponibles() {
        try {
            List<Grado> grados = gradoRepositorio.buscarTodos();
            List<String> nombresGrados = grados.stream()
                .map(Grado::getNombreGrado)
                .collect(Collectors.toList());
            
            return ResultadoOperacion.exitoConDatos("Grados obtenidos", nombresGrados);
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener los grados: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si se pueden agregar más estudiantes
     * Usa la lógica del modelo de dominio (Acudiente)
     */
    public boolean puedeAgregarMasEstudiantes(int cantidadActual) {
        // Crear un acudiente temporal para usar su lógica de negocio
        Acudiente acudienteTemp = new Acudiente();
        
        // Simular la cantidad de estudiantes actual
        if (cantidadActual > 0) {
            Set<Estudiante> estudiantesTemp = new HashSet<>();
            for (int i = 0; i < cantidadActual; i++) {
                estudiantesTemp.add(new Estudiante());
            }
            acudienteTemp.setEstudiantes(estudiantesTemp);
        }
        
        // Usar el método del dominio
        return acudienteTemp.puedeAgregarMasEstudiantes();
    }
    
    /**
     * Obtiene cupos restantes
     */
    public int obtenerCuposRestantes(int cantidadActual) {
        return Math.max(0, Acudiente.MAX_ESTUDIANTES - cantidadActual);
    }
    
    /**
     * Obtiene el máximo de estudiantes permitido
     */
    public int obtenerMaximoEstudiantes() {
        return Acudiente.MAX_ESTUDIANTES;
    }
    
    /**
     * Utilidad para parseo seguro de enteros
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
