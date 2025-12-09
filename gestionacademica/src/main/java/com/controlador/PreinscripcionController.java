package com.controlador;

import com.modelo.dominio.*;
import com.modelo.dtos.AcudienteDTO;
import com.modelo.dtos.EstudianteDTO;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.*;

/**
 * CONTROLADOR - PreinscripcionController
 * 
 * Responsabilidades del Controlador en MVC:
 * ==========================================
 * 1. Recibir solicitudes de la Vista
 * 2. Coordinar operaciones entre Modelo y Repositorios
 * 3. Transformar datos entre la Vista y el Modelo
 * 4. Orquestar transacciones
 * 5. Devolver DTOs o resultados a la Vista
 * 
 * LO QUE NO DEBE HACER:
 * - NO contiene lógica de validación de negocio (eso es del MODELO)
 * - NO crea componentes visuales (eso es de la VISTA)
 * - NO tiene lógica compleja de negocio (eso es del MODELO)
 */
public class PreinscripcionController {
    
    // ============================================
    // DEPENDENCIAS
    // ============================================
    private final RepositorioGenerico<Preinscripcion> repoPreinscripcion;
    private final RepositorioGenerico<Acudiente> repoAcudiente;
    private final RepositorioGenerico<Estudiante> repoEstudiante;
    private final UsuarioRepositorio usuarioRepositorio;
    private final GradoRepositorio gradoRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final EntityManager entityManager;
    
    public PreinscripcionController(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.repoPreinscripcion = new RepositorioGenerico<>(entityManager, Preinscripcion.class);
        this.repoAcudiente = new RepositorioGenerico<>(entityManager, Acudiente.class);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, Estudiante.class);
        this.gradoRepositorio = new GradoRepositorio(entityManager);
        this.usuarioRepositorio = new UsuarioRepositorio(entityManager);
        this.estudianteRepositorio = new EstudianteRepositorio(entityManager);
    }
    
    // ============================================
    // MÉTODOS DEL CONTROLADOR
    // ============================================
    
    /**
     * Valida los datos del acudiente
     * 
     * RESPONSABILIDAD DEL CONTROLADOR:
     * - Recibe un DTO de la vista
     * - Crea una instancia temporal del Modelo
     * - Delega la validación al MODELO
     * - Verifica duplicados en la BD a través de repositorios
     * - Devuelve un resultado a la vista
     */

    // Pasa de DTO a objeto de dominio
    public ResultadoOperacion validarAcudiente(AcudienteDTO datos) {
        // 1. Crear instancia temporal del modelo para validar
        Acudiente acudiente = new Acudiente();
        acudiente.setNuipUsuario(datos.nuip);
        acudiente.setPrimerNombre(datos.primerNombre);
        acudiente.setSegundoNombre(datos.segundoNombre);
        acudiente.setPrimerApellido(datos.primerApellido);
        acudiente.setSegundoApellido(datos.segundoApellido);
        acudiente.setEdad(datos.edad);
        acudiente.setCorreoElectronico(datos.correoElectronico);
        acudiente.setTelefono(datos.telefono);
        
        // 2. Delegar validación al MODELO (las reglas están ahí)
        ResultadoValidacionDominio validacion = acudiente.validar();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // 3. Verificar duplicados usando repositorios
        if (usuarioRepositorio.existePorNuip(datos.nuip)) {
            return ResultadoOperacion.errorValidacion("nuip",
                "Ya existe un usuario registrado con este NUIP");
        }
        
        if (usuarioRepositorio.existePorCorreo(datos.correoElectronico)) {
            return ResultadoOperacion.errorValidacion("correoElectronico",
                "Ya existe un usuario registrado con este correo electrónico");
        }
        
        if (usuarioRepositorio.existePorTelefono(datos.telefono)) {
            return ResultadoOperacion.errorValidacion("telefono",
                "Ya existe un usuario registrado con este teléfono");
        }
        
        // 4. Retornar resultado exitoso a la vista
        return ResultadoOperacion.exito("Datos válidos");
    }
    
    /**
     * Valida los datos del estudiante
     * 
     * Sigue el mismo patrón: recibe DTO, usa el Modelo para validar,
     * consulta repositorios, devuelve resultado
     */
    public ResultadoOperacion validarEstudiante(EstudianteDTO datos) {
        // 1. Crear instancia temporal del modelo
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre(datos.primerNombre);
        estudiante.setSegundoNombre(datos.segundoNombre);
        estudiante.setPrimerApellido(datos.primerApellido);
        estudiante.setSegundoApellido(datos.segundoApellido);
        estudiante.setEdad(datos.edad);
        estudiante.setNuip(datos.nuip);
        
        // Buscar y asignar el grado
        Optional<Grado> gradoOpt = gradoRepositorio.buscarPornombreGrado(datos.nombreGrado);
        if (!gradoOpt.isPresent()) {
            return ResultadoOperacion.errorValidacion("gradoAspira",
                "El grado seleccionado no existe");
        }
        estudiante.setGradoAspira(gradoOpt.get());
        
        // 2. Delegar validación al MODELO
        ResultadoValidacionDominio validacion = estudiante.validar();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // 3. Verificar duplicados
        if (estudianteRepositorio.existePorNuip(datos.nuip)) {
            return ResultadoOperacion.errorValidacion("nuip",
                "Ya existe un estudiante registrado con este NUIP");
        }
        
        return ResultadoOperacion.exito("Datos válidos");
    }
    
    /**
     * Registra una preinscripción completa
     * 
     * RESPONSABILIDAD DEL CONTROLADOR:
     * - Orquestar la transacción completa
     * - Coordinar entre diferentes repositorios
     * - Transformar DTOs en entidades del Modelo
     * - Manejar la persistencia
     * - Devolver resultado a la Vista
     */
    public ResultadoOperacion registrarPreinscripcion(
            AcudienteDTO datosAcudiente,
            List<EstudianteDTO> datosEstudiantes) {
        
        // Validación básica de entrada
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
            
            // 1. Crear y validar acudiente
            Acudiente acudiente = construirAcudiente(datosAcudiente);
            ResultadoValidacionDominio validacionAcudiente = acudiente.validar();
            
            if (!validacionAcudiente.isValido()) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion(
                    validacionAcudiente.getCampoInvalido(),
                    validacionAcudiente.getMensajeError()
                );
            }
            
            // Guardar acudiente
            repoAcudiente.guardar(acudiente);
            
            // 2. Crear preinscripción
            Preinscripcion preinscripcion = new Preinscripcion();
            preinscripcion.setFechaRegistro(LocalDate.now());
            preinscripcion.setEstado(Estado.Pendiente);
            preinscripcion.setAcudiente(acudiente);
            repoPreinscripcion.guardar(preinscripcion);
            
            // 3. Procesar estudiantes
            for (EstudianteDTO datosEst : datosEstudiantes) {
                Estudiante estudiante = construirEstudiante(datosEst);
                
                // Validar cada estudiante
                ResultadoValidacionDominio validacionEst = estudiante.validar();
                if (!validacionEst.isValido()) {
                    transaction.rollback();
                    return ResultadoOperacion.errorValidacion(
                        validacionEst.getCampoInvalido(),
                        validacionEst.getMensajeError()
                    );
                }
                
                // Establecer relaciones
                estudiante.setAcudiente(acudiente);
                estudiante.setPreinscripcion(preinscripcion);
                estudiante.setEstado(Estado.Pendiente);
                
                // Agregar al acudiente (usa lógica de negocio del modelo)
                try {
                    acudiente.agregarEstudiante(estudiante);
                } catch (Acudiente.DomainException e) {
                    transaction.rollback();
                    return ResultadoOperacion.error(e.getMessage());
                }
                
                // Guardar estudiante
                repoEstudiante.guardar(estudiante);
            }
            
            // 4. Completar transacción
            entityManager.refresh(preinscripcion);
            transaction.commit();
            
            // 5. Retornar resultado exitoso con datos
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
    }
    
    /**
     * Obtiene los grados disponibles para mostrar en el formulario
     */
    public ResultadoOperacion obtenerGradosDisponibles() {
        try {
            List<Grado> grados = gradoRepositorio.buscarTodos();
            List<String> nombresGrados = new ArrayList<>();
            
            for (Grado grado : grados) {
                nombresGrados.add(grado.getNombreGrado());
            }
            
            return ResultadoOperacion.exitoConDatos(
                "Grados obtenidos",
                nombresGrados
            );
        } catch (Exception e) {
            return ResultadoOperacion.error(
                "Error al obtener los grados: " + e.getMessage()
            );
        }
    }
    
    /**
     * Verifica si un acudiente puede agregar más estudiantes
     * Método auxiliar que delega al Modelo
     */
    public boolean puedeAgregarMasEstudiantes(int cantidadActual) {
        return cantidadActual < Acudiente.MAX_ESTUDIANTES;
    }
    
    /**
     * Obtiene los cupos restantes
     */
    public int obtenerCuposRestantes(int cantidadActual) {
        return Math.max(0, Acudiente.MAX_ESTUDIANTES - cantidadActual);
    }
    
    // ============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================
    
    /**
     * Construye una entidad Acudiente desde un DTO
     * Transformación de datos entre capas
     */
    private Acudiente construirAcudiente(AcudienteDTO datos) {
        Acudiente acudiente = new Acudiente();
        acudiente.setNuipUsuario(datos.nuip);
        acudiente.setPrimerNombre(datos.primerNombre);
        acudiente.setSegundoNombre(datos.segundoNombre);
        acudiente.setPrimerApellido(datos.primerApellido);
        acudiente.setSegundoApellido(datos.segundoApellido);
        acudiente.setEdad(datos.edad);
        acudiente.setCorreoElectronico(datos.correoElectronico);
        acudiente.setTelefono(datos.telefono);
        acudiente.setEstadoAprobacion(Estado.Pendiente);
        return acudiente;
    }
    
    /**
     * Construye una entidad Estudiante desde un DTO
     */
    private Estudiante construirEstudiante(EstudianteDTO datos) {
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre(datos.primerNombre);
        estudiante.setSegundoNombre(datos.segundoNombre);
        estudiante.setPrimerApellido(datos.primerApellido);
        estudiante.setSegundoApellido(datos.segundoApellido);
        estudiante.setEdad(datos.edad);
        estudiante.setNuip(datos.nuip);
        
        // Buscar y asignar el grado
        Optional<Grado> gradoOpt = gradoRepositorio.buscarPornombreGrado(datos.nombreGrado);
        if (gradoOpt.isPresent()) {
            estudiante.setGradoAspira(gradoOpt.get());
        }
        
        return estudiante;
    }
}