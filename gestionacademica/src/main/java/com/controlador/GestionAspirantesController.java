package com.controlador;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar la aprobación y rechazo de aspirantes
 * Implementa RF 3.4 - Aprobar o rechazar aspirante
 * Arquitectura MVC limpia sin servicios intermedios
 */
public class GestionAspirantesController {
    
    private final EntityManager entityManager;
    private final PreinscripcionRepositorio repoPreinscripcion;
    private final RepositorioGenerico<Estudiante> repoEstudiante;
    private final AcudienteRepositorio repoAcudiente;
    private final GrupoRepositorio repoGrupo;
    private final GradoRepositorio repoGrado;
    private final TokenUsuarioRepositorio repoToken;
    private final RolRepositorio repoRol;
    
    public GestionAspirantesController(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.repoPreinscripcion = new PreinscripcionRepositorio(entityManager);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, Estudiante.class);
        this.repoAcudiente = new AcudienteRepositorio(entityManager);
        this.repoGrupo = new GrupoRepositorio(entityManager);
        this.repoGrado = new GradoRepositorio(entityManager);
        this.repoToken = new TokenUsuarioRepositorio(entityManager);
        this.repoRol = new RolRepositorio(entityManager);
    }
    
    /**
     * DTO para transferir información de aspirantes a la UI
     */
    public static class AspiranteDTO {
        private Integer idPreinscripcion;
        private String nombreCompletoAcudiente;
        private Integer idAcudiente;
        private List<EstudianteDTO> estudiantes;
        
        public AspiranteDTO(Integer idPreinscripcion, String nombreCompletoAcudiente, 
                           Integer idAcudiente, List<EstudianteDTO> estudiantes) {
            this.idPreinscripcion = idPreinscripcion;
            this.nombreCompletoAcudiente = nombreCompletoAcudiente;
            this.idAcudiente = idAcudiente;
            this.estudiantes = estudiantes;
        }
        
        public Integer getIdPreinscripcion() { return idPreinscripcion; }
        public String getNombreCompletoAcudiente() { return nombreCompletoAcudiente; }
        public Integer getIdAcudiente() { return idAcudiente; }
        public List<EstudianteDTO> getEstudiantes() { return estudiantes; }
    }
    
    public static class EstudianteDTO {
        private Integer idEstudiante;
        private String nombreCompleto;
        private String nombreGrado;
        private Estado estado;
        
        public EstudianteDTO(Integer idEstudiante, String nombreCompleto, 
                            String nombreGrado, Estado estado) {
            this.idEstudiante = idEstudiante;
            this.nombreCompleto = nombreCompleto;
            this.nombreGrado = nombreGrado;
            this.estado = estado;
        }
        
        public Integer getIdEstudiante() { return idEstudiante; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getNombreGrado() { return nombreGrado; }
        public Estado getEstado() { return estado; }
    }
    
    /**
     * Obtiene la lista de aspirantes pendientes ordenados por fecha
     */
    public ResultadoOperacion obtenerListaAspirantes() {
        try {
            // Obtener preinscripciones PENDIENTES
            List<Preinscripcion> preinscripciones = repoPreinscripcion.buscarPorEstado(Estado.Pendiente);
            
            if (preinscripciones.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            // Convertir a DTOs y filtrar aquellos que tengan estudiantes pendientes
            List<AspiranteDTO> aspirantes = preinscripciones.stream()
                .map(this::convertirADTO)
                .filter(Objects::nonNull) // Filtrar nulos (sin estudiantes pendientes)
                .filter(dto -> !dto.getEstudiantes().isEmpty()) // Asegurar que tenga estudiantes
                .collect(Collectors.toList());
            
            if (aspirantes.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            return ResultadoOperacion.exitoConDatos("Lista obtenida", aspirantes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al acceder a la base de datos, inténtelo nuevamente");
        }
    }
    
    /**
     * Aprueba un estudiante específico
     * RF 3.4 - Aprobar aspirante
     */
    public ResultadoOperacion aprobarEstudiante(Integer idEstudiante) {
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 1. Buscar y validar estudiante
            Optional<Estudiante> estudianteOpt = repoEstudiante.buscarPorId(idEstudiante);
            if (estudianteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Estudiante no encontrado");
            }
            
            Estudiante estudiante = estudianteOpt.get();
            Acudiente acudiente = estudiante.getAcudiente();
            
            if (acudiente == null) {
                transaction.rollback();
                return ResultadoOperacion.error("Acudiente no encontrado");
            }
            
            // 2. Obtener preinscripción
            Preinscripcion preinscripcion = repoPreinscripcion
                .obtenerPreinscripcionPorEstudiante(estudiante);
            
            // 3. Verificar si el acudiente necesita ser aprobado
            // ACUDIENTE SE APRUEBA CON EL PRIMER ESTUDIANTE APROBADO
            if (acudiente.getEstadoAprobacion() != Estado.Aprobada) {
                // Aprobar acudiente (primer estudiante aprobado)
                acudiente.setEstadoAprobacion(Estado.Aprobada);
                
                // Generar token de acceso usando lógica de dominio
                ResultadoOperacion resultadoToken = generarTokenParaAcudiente(acudiente);
                if (!resultadoToken.isExitoso()) {
                    transaction.rollback();
                    return resultadoToken;
                }
                
                repoAcudiente.guardar(acudiente);
                
                System.out.println("Acudiente aprobado. ID: " + acudiente.getIdUsuario() + 
                                ", Correo: " + acudiente.getCorreoElectronico());
            }
            
            // 4. Aprobar estudiante
            estudiante.setEstado(Estado.Aprobada);
            
            // 5. Asignar estudiante a grupo usando lógica de dominio
            ResultadoOperacion resultadoAsignacion = asignarEstudianteAGrupo(estudiante);
            if (!resultadoAsignacion.isExitoso()) {
                transaction.rollback();
                return resultadoAsignacion;
            }
            
            repoEstudiante.guardar(estudiante);
            
            // 6. Actualizar estado de preinscripción
            if (preinscripcion != null) {
                actualizarEstadoPreinscripcion(preinscripcion);
            }
            
            transaction.commit();
            return ResultadoOperacion.exito("¡Listo! El estudiante fue aprobado con éxito");
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al acceder a la base de datos: " + e.getMessage());
        }
    }

    /**
     * Rechaza un estudiante específico
     * RF 3.4 - Rechazar aspirante
     */
    public ResultadoOperacion rechazarEstudiante(Integer idEstudiante) {
    EntityTransaction transaction = entityManager.getTransaction();
    
    try {
        transaction.begin();
        
        // 1. Buscar y validar estudiante
        Optional<Estudiante> estudianteOpt = repoEstudiante.buscarPorId(idEstudiante);
        if (estudianteOpt.isEmpty()) {
            transaction.rollback();
            return ResultadoOperacion.error("Estudiante no encontrado");
        }
        
        Estudiante estudiante = estudianteOpt.get();
        Acudiente acudiente = estudiante.getAcudiente();
        
        if (acudiente == null) {
            transaction.rollback();
            return ResultadoOperacion.error("Acudiente no encontrado");
        }
        
        // 2. Obtener preinscripción
        Preinscripcion preinscripcion = repoPreinscripcion
            .obtenerPreinscripcionPorEstudiante(estudiante);
        
        // 3. Verificar estado de otros estudiantes del acudiente
        boolean tieneEstudiantesPendientes = 
            repoAcudiente.tieneEstudiantesPendientes(acudiente, idEstudiante);
        boolean tieneEstudiantesAprobados = 
            repoAcudiente.tieneEstudiantesAprobados(acudiente);
        
        // 4. SOLO RECHAZAR ACUDIENTE SI:
        //    - NO tiene estudiantes aprobados
        //    - NO tiene más estudiantes pendientes
        //    - (ya que si tiene aprobados, ya está aprobado)
        //    - (si tiene pendientes, aún puede que algunos sean aprobados)
        
        if (!tieneEstudiantesAprobados && !tieneEstudiantesPendientes) {
            // Verificar si TODOS los estudiantes están rechazados
            boolean todosEstudiantesRechazados = acudiente.getEstudiantes().stream()
                .allMatch(e -> e.getEstado() == Estado.Rechazada);
            
            if (todosEstudiantesRechazados) {
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                repoAcudiente.guardar(acudiente);
            }
        }
        
        // 5. Rechazar estudiante
        estudiante.setEstado(Estado.Rechazada);
        repoEstudiante.guardar(estudiante);
        
        // 6. Actualizar estado de preinscripción
        if (preinscripcion != null) {
            actualizarEstadoPreinscripcion(preinscripcion);
        }
        
        transaction.commit();
        return ResultadoOperacion.exito("¡Listo! El estudiante fue rechazado con éxito");
        
    } catch (Exception e) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        e.printStackTrace();
        return ResultadoOperacion.error(
            "Error al acceder a la base de datos, inténtelo nuevamente");
        }
    }

    /**
     * Genera y asigna token para un acudiente aprobado
     * Utiliza lógica de dominio de TokenUsuario
     */
    private ResultadoOperacion generarTokenParaAcudiente(Acudiente acudiente) {
        try {
            // Verificar si ya tiene token
            if (acudiente.getTokenAccess() != null) {
                System.out.println("Acudiente ID " + acudiente.getIdUsuario() + 
                                " ya tiene token asignado");
                return ResultadoOperacion.exito("Token ya existente");
            }
            
            // Obtener rol de Acudiente
            Optional<Rol> rolOpt = repoRol.buscarPorNombreRol("acudiente");
            if (rolOpt.isEmpty()) {
                return ResultadoOperacion.error("Rol de Acudiente no encontrado en el sistema");
            }
            
            Rol rol = rolOpt.get();
            
            // Generar token usando lógica de dominio
            TokenUsuario token = TokenUsuario.generarTokenDesdeUsuario(
                acudiente.getPrimerNombre(),
                acudiente.getSegundoNombre(),
                acudiente.getPrimerApellido(),
                acudiente.getSegundoApellido(),
                rol
            );
            
            // Guardar token
            repoToken.guardar(token);
            
            // Asignar token al acudiente
            acudiente.setTokenAccess(token);
            
            System.out.println("Token generado para acudiente ID: " + 
                            acudiente.getIdUsuario() + 
                            ", Usuario: " + token.getNombreUsuario());
            
            return ResultadoOperacion.exitoConDatos("Token generado", token);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al generar token: " + e.getMessage());
        }
    }
    
    /**
     * Asigna estudiante a un grupo utilizando la lógica de dominio
     */
    private ResultadoOperacion asignarEstudianteAGrupo(Estudiante estudiante) {
        try {
            if (estudiante.getGradoAspira() == null) {
                return ResultadoOperacion.error("El estudiante no tiene grado asignado");
            }
            
            Integer idGrado = estudiante.getGradoAspira().getIdGrado();
            
            // Buscar grupo disponible (ya incluye toda la lógica de prioridades)
            Grupo grupoDisponible = encontrarGrupoDisponibleParaGrado(idGrado);
            
            if (grupoDisponible != null && grupoDisponible.agregarEstudiante(estudiante)) {
                repoGrupo.guardar(grupoDisponible);
                return ResultadoOperacion.exito("Estudiante asignado a grupo existente");
            }
            
            // Crear nuevo grupo si no hay disponibles
            Grupo nuevoGrupo = crearNuevoGrupo(idGrado);
            
            if (nuevoGrupo.agregarEstudiante(estudiante)) {
                repoGrupo.guardar(nuevoGrupo);
                return ResultadoOperacion.exito("Nuevo grupo creado y estudiante asignado");
            }
            
            return ResultadoOperacion.error("No se pudo asignar estudiante a ningún grupo");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al asignar grupo: " + e.getMessage());
        }
    }

    /**
     * Método unificado que encuentra el mejor grupo disponible para un grado
     * Combina las lógicas de búsqueda existentes
     */
    private Grupo encontrarGrupoDisponibleParaGrado(Integer idGrado) {
        List<Grupo> gruposDelGrado = repoGrupo.buscarPorGrado(idGrado);
        
        // Usar tu método existente que ya tiene la lógica correcta
        return buscarGrupoDisponible(gruposDelGrado);
    }

    /**
     * Busca un grupo disponible usando reglas de negocio del dominio
     * Prioridad 1: Grupos inactivos con menos de 5 estudiantes (llenar primero)
     * Prioridad 2: Grupos activos con disponibilidad (menos de 10 estudiantes)
     */
    private Grupo buscarGrupoDisponible(List<Grupo> grupos) {
        if (grupos == null || grupos.isEmpty()) {
            return null;
        }
        
        // Optimización: Ordenar grupos inactivos por cantidad de estudiantes (descendente)
        // Esto prioriza llenar primero los grupos que ya tienen más estudiantes
        
        // Prioridad 1: Grupos inactivos que necesitan completarse
        List<Grupo> gruposInactivos = new ArrayList<>();
        for (Grupo grupo : grupos) {
            if (!grupo.isEstado() && !grupo.tieneEstudiantesSuficientes() && grupo.tieneDisponibilidad()) {
                gruposInactivos.add(grupo);
            }
        }
        
        // Ordenar por cantidad de estudiantes descendente para llenar primero
        if (!gruposInactivos.isEmpty()) {
            gruposInactivos.sort((g1, g2) -> 
                Integer.compare(g2.getCantidadEstudiantes(), g1.getCantidadEstudiantes()));
            return gruposInactivos.get(0);
        }
        
        // Prioridad 2: Grupos activos con disponibilidad
        List<Grupo> gruposActivos = new ArrayList<>();
        for (Grupo grupo : grupos) {
            if (grupo.isEstado() && grupo.tieneDisponibilidad()) {
                gruposActivos.add(grupo);
            }
        }
        
        // Ordenar por cantidad de estudiantes ascendente para balancear carga
        if (!gruposActivos.isEmpty()) {
            gruposActivos.sort((g1, g2) -> 
                Integer.compare(g1.getCantidadEstudiantes(), g2.getCantidadEstudiantes()));
            return gruposActivos.get(0);
        }
        
        return null;
    }

    /**
     * Crea un nuevo grupo para un grado específico
     */
    private Grupo crearNuevoGrupo(Integer idGrado) throws Exception {
        Optional<Grado> gradoOpt = repoGrado.buscarPorId(idGrado);
        if (gradoOpt.isEmpty()) {
            throw new Exception("Grado no encontrado con ID: " + idGrado);
        }
        
        Grado grado = gradoOpt.get();
        
        // Contar grupos existentes del grado
        Long cantidadGrupos = repoGrupo.contarGruposPorGrado(idGrado);
        
        // Generar nombre del grupo
        String nombreGrupo = generarNombreGrupo(grado, cantidadGrupos);
        
        // Crear nuevo grupo
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombreGrupo(nombreGrupo);
        nuevoGrupo.setEstado(false); // Inactivo hasta tener 5 estudiantes
        nuevoGrupo.setGrado(grado);
        nuevoGrupo.setEstudiantes(new HashSet<>());
        
        return repoGrupo.guardar(nuevoGrupo);
    }

    /**
     * Actualiza el estado de la preinscripción y del acudiente basado en el estado de sus estudiantes
     * Reglas:
     * - Al menos uno aprobado → Preinscripción Aprobada, Acudiente Aprobado (con token)
     * - Todos rechazados → Preinscripción Rechazada, Acudiente Rechazado
     * - Ningún aprobado y al menos uno pendiente → Preinscripción Pendiente, Acudiente Pendiente
     */
    private void actualizarEstadoPreinscripcion(Preinscripcion preinscripcion) {
        if (preinscripcion == null || preinscripcion.getEstudiantes() == null || preinscripcion.getAcudiente() == null) {
            return;
        }
        
        Acudiente acudiente = preinscripcion.getAcudiente();
        
        // Contar estudiantes por estado
        long pendientes = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Pendiente)
            .count();
        
        long aprobados = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Aprobada)
            .count();
        
        long rechazados = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Rechazada)
            .count();
        
        long total = preinscripcion.getEstudiantes().size();
        
        // Aplicar reglas de negocio para la preinscripción y el acudiente
        if (aprobados > 0) {
            // AL MENOS UN ESTUDIANTE APROBADO
            preinscripcion.setEstado(Estado.Aprobada);
            
            // Si el acudiente aún no está aprobado, aprobarlo y generar token
            if (acudiente.getEstadoAprobacion() != Estado.Aprobada) {
                acudiente.setEstadoAprobacion(Estado.Aprobada);
                // Generar token si no existe
                if (acudiente.getTokenAccess() == null) {
                    ResultadoOperacion resultadoToken = generarTokenParaAcudiente(acudiente);
                    if (!resultadoToken.isExitoso()) {
                        // Solo loguear error, no fallar la operación
                        System.err.println("Error generando token: " + resultadoToken.getMensaje());
                    }
                }
                repoAcudiente.guardar(acudiente);
            }
            
        } else if (rechazados == total) {
            preinscripcion.setEstado(Estado.Rechazada);
            
            // Rechazar también al acudiente si no tiene otros estudiantes aprobados
            // Verificar si el acudiente tiene estudiantes en otras preinscripciones
            boolean tieneEstudiantesAprobadosEnOtros = repoAcudiente.tieneEstudiantesAprobados(acudiente);
            
            if (!tieneEstudiantesAprobadosEnOtros) {
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                repoAcudiente.guardar(acudiente);
            }
            
        } else if (pendientes > 0) {
            preinscripcion.setEstado(Estado.Pendiente);
            
            // Solo establecer acudiente como pendiente si no está aprobado ya
            if (acudiente.getEstadoAprobacion() != Estado.Aprobada) {
                acudiente.setEstadoAprobacion(Estado.Pendiente);
                repoAcudiente.guardar(acudiente);
            }
        }
        
        // Guardar cambios en la preinscripción
        repoPreinscripcion.guardar(preinscripcion);
    }
    
    /**
     * Genera el nombre del grupo basado en el grado y la cantidad existente
     */
    private String generarNombreGrupo(Grado grado, Long cantidadGrupos) {
        return String.format("%s-%d", grado.getNombreGrado(), cantidadGrupos + 1);
    }
    
    /**
     * Convierte una entidad de preinscripción a DTO para la vista
     * SOLO incluye estudiantes con estado PENDIENTE
     */
    private AspiranteDTO convertirADTO(Preinscripcion preinscripcion) {
        Acudiente acudiente = preinscripcion.getAcudiente();
        String nombreAcudiente = acudiente.obtenerNombreCompleto();
        
        // FILTRAR SOLO ESTUDIANTES PENDIENTES
        List<EstudianteDTO> estudiantesDTO = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Pendiente)
            .map(e -> new EstudianteDTO(
                e.getIdEstudiante(),
                construirNombreCompleto(
                    e.getPrimerNombre(),
                    e.getSegundoNombre(),
                    e.getPrimerApellido(),
                    e.getSegundoApellido()
                ),
                e.getGradoAspira() != null ? e.getGradoAspira().getNombreGrado() : "Sin grado",
                e.getEstado()
            ))
            .collect(Collectors.toList());
        
        // Solo devolver DTO si hay estudiantes pendientes
        if (estudiantesDTO.isEmpty()) {
            return null;
        }
        
        return new AspiranteDTO(
            preinscripcion.getIdPreinscripcion(),
            nombreAcudiente,
            acudiente.getIdUsuario(),
            estudiantesDTO
        );
    }
    
    /**
     * Construye nombre completo de una persona
     */
    private String construirNombreCompleto(String primer, String segundo, 
                                          String primerAp, String segundoAp) {
        StringBuilder nombre = new StringBuilder(primer);
        if (segundo != null && !segundo.isEmpty()) {
            nombre.append(" ").append(segundo);
        }
        nombre.append(" ").append(primerAp);
        if (segundoAp != null && !segundoAp.isEmpty()) {
            nombre.append(" ").append(segundoAp);
        }
        return nombre.toString();
    }
}