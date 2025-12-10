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
            List<Preinscripcion> preinscripciones = repoPreinscripcion.buscarPendientes();
            
            if (preinscripciones.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            List<AspiranteDTO> aspirantes = preinscripciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
            
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
            boolean acudienteNecesitaAprobacion = 
                !repoAcudiente.tieneEstudiantesAprobados(acudiente);
            
            if (acudienteNecesitaAprobacion) {
                // Aprobar acudiente
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
            
            // 4. Rechazar acudiente solo si no tiene más estudiantes pendientes ni aprobados
            if (!tieneEstudiantesPendientes && !tieneEstudiantesAprobados) {
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                repoAcudiente.guardar(acudiente);
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
            // Validar que el estudiante tenga grado asignado
            if (estudiante.getGradoAspira() == null) {
                return ResultadoOperacion.error("El estudiante no tiene grado asignado");
            }
            
            Integer idGrado = estudiante.getGradoAspira().getIdGrado();
            
            // Obtener grupos ordenados por cantidad de estudiantes
            List<Grupo> grupos = repoGrupo
                .buscarActivosPorGradoOrdenadosPorEstudiantes(idGrado);
            
            // Buscar grupo disponible usando lógica de dominio
            Grupo grupoAsignado = buscarGrupoDisponible(grupos);
            
            // Si no hay grupos disponibles, crear uno nuevo
            if (grupoAsignado == null) {
                grupoAsignado = crearNuevoGrupo(idGrado);
            }
            
            // Asignar estudiante al grupo
            estudiante.setGrupo(grupoAsignado);
            
            // Actualizar estado del grupo según cantidad de estudiantes
            actualizarEstadoGrupo(grupoAsignado);
            
            return ResultadoOperacion.exito("Estudiante asignado a grupo");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al asignar grupo: " + e.getMessage());
        }
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
        
        // Prioridad 1: Grupos inactivos que necesitan completarse
        for (Grupo grupo : grupos) {
            if (!grupo.isEstado() && !grupo.tieneEstudiantesSuficientes()) {
                return grupo;
            }
        }
        
        // Prioridad 2: Grupos activos con disponibilidad
        for (Grupo grupo : grupos) {
            if (grupo.isEstado() && grupo.tieneDisponibilidad()) {
                return grupo;
            }
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
     * Actualiza el estado del grupo basado en su lógica de dominio
     * Activo: >= 5 estudiantes
     * Inactivo: < 5 estudiantes
     */
    private void actualizarEstadoGrupo(Grupo grupo) {
        if (grupo == null) return;
        
        // Usar lógica de dominio para determinar estado
        boolean debeEstarActivo = grupo.tieneEstudiantesSuficientes();
        
        if (grupo.isEstado() != debeEstarActivo) {
            grupo.setEstado(debeEstarActivo);
            repoGrupo.guardar(grupo);
        }
    }

    /**
     * Actualiza el estado de la preinscripción basado en el estado de sus estudiantes
     * Reglas:
     * - Al menos uno aprobado → Preinscripción Aprobada
     * - Todos rechazados → Preinscripción Rechazada
     * - Hay pendientes → Preinscripción Pendiente
     */
    private void actualizarEstadoPreinscripcion(Preinscripcion preinscripcion) {
        if (preinscripcion == null || preinscripcion.getEstudiantes() == null) {
            return;
        }
        
        // Contar estudiantes por estado
        long aprobados = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Aprobada)
            .count();
        
        long rechazados = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Rechazada)
            .count();
        
        long pendientes = preinscripcion.getEstudiantes().stream()
            .filter(e -> e.getEstado() == Estado.Pendiente)
            .count();
        
        long total = preinscripcion.getEstudiantes().size();
        
        // Aplicar reglas de negocio
        if (aprobados > 0) {
            preinscripcion.setEstado(Estado.Aprobada);
        } else if (rechazados == total) {
            preinscripcion.setEstado(Estado.Rechazada);
        } else if (pendientes > 0) {
            preinscripcion.setEstado(Estado.Pendiente);
        }
        
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
     */
    private AspiranteDTO convertirADTO(Preinscripcion preinscripcion) {
        Acudiente acudiente = preinscripcion.getAcudiente();
        String nombreAcudiente = acudiente.obtenerNombreCompleto();
        
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