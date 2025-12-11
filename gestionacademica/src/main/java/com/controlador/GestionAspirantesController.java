package com.controlador;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;
import java.util.stream.Collectors;

public class GestionAspirantesController {
    
    private final EntityManager entityManager;
    private final PreinscripcionRepositorio repoPreinscripcion;
    private final RepositorioGenerico<Estudiante> repoEstudiante;
    private final AcudienteRepositorio repoAcudiente;
    private final GrupoRepositorio repoGrupo;
    private final GradoRepositorio repoGrado;
    private final RolRepositorio repoRol;
    
    public GestionAspirantesController(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.repoPreinscripcion = new PreinscripcionRepositorio(entityManager);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, Estudiante.class);
        this.repoAcudiente = new AcudienteRepositorio(entityManager);
        this.repoGrupo = new GrupoRepositorio(entityManager);
        this.repoGrado = new GradoRepositorio(entityManager);
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
            // Obtener TODAS las preinscripciones (no solo las pendientes)
            List<Preinscripcion> preinscripciones = repoPreinscripcion.buscarTodos();
            
            if (preinscripciones.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            // Convertir a DTOs y filtrar aquellas que tengan estudiantes pendientes
            List<AspiranteDTO> aspirantes = preinscripciones.stream()
                .map(this::convertirADTO)
                .filter(Objects::nonNull) // Filtrar nulos (sin estudiantes pendientes)
                .filter(dto -> !dto.getEstudiantes().isEmpty()) // Asegurar que tenga estudiantes pendientes
                .sorted((a1, a2) -> {
                    // Ordenar por ID de preinscripción (más antiguos primero)
                    return a1.getIdPreinscripcion().compareTo(a2.getIdPreinscripcion());
                })
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
     * Obtiene rol de acudiente (lógica del controlador)
     */
    private Optional<Rol> obtenerRolAcudiente() {
        try {
            return repoRol.buscarPorNombreRol("acudiente");
        } catch (Exception e) {
            System.err.println("Error obteniendo rol de acudiente: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Aprueba un estudiante específico
     */
    public ResultadoOperacion aprobarEstudiante(Integer idEstudiante) {
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 1. Obtener rol de acudiente
            Optional<Rol> rolAcudienteOpt = obtenerRolAcudiente();
            if (rolAcudienteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error(
                    "Error: El rol 'acudiente' no está configurado en el sistema");
            }
            
            Rol rolAcudiente = rolAcudienteOpt.get();
            
            // 2. Buscar estudiante y su preinscripción
            Optional<Estudiante> estudianteOpt = repoEstudiante.buscarPorId(idEstudiante);
            if (estudianteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Estudiante no encontrado");
            }
            
            Estudiante estudiante = estudianteOpt.get();
            Preinscripcion preinscripcion = estudiante.getPreinscripcion();
            
            if (preinscripcion == null) {
                transaction.rollback();
                return ResultadoOperacion.error("El estudiante no tiene preinscripción asociada");
            }
            
            // 3. Delegar al dominio
            ResultadoOperacion resultadoAprobacion = preinscripcion.aprobarEstudiante(
                idEstudiante, rolAcudiente);
            
            if (!resultadoAprobacion.isExitoso()) {
                transaction.rollback();
                return resultadoAprobacion;
            }
            
            // 4. Asignar estudiante a grupo
            ResultadoOperacion resultadoAsignacion = asignarEstudianteAGrupo(estudiante);
            if (!resultadoAsignacion.isExitoso()) {
                transaction.rollback();
                return resultadoAsignacion;
            }
            
            // 5. Persistir TODOS los cambios
            repoPreinscripcion.guardar(preinscripcion);
            repoAcudiente.guardar(preinscripcion.getAcudiente());
            repoEstudiante.guardar(estudiante);
            
            // 6. Determinar mensaje final
            Acudiente acudiente = preinscripcion.getAcudiente();
            StringBuilder mensajeBuilder = new StringBuilder("¡Listo! El estudiante fue aprobado con éxito");
            
            // Verificar si AHORA no hay estudiantes pendientes
            long pendientesDespues = preinscripcion.getEstudiantes().stream()
                .filter(e -> e.getEstado() == Estado.Pendiente)
                .count();
            
            boolean noHayMasPendientes = (pendientesDespues == 0);
            
            // Mostrar mensaje de credenciales si:
            // 1. La preinscripción está APROBADA
            // 2. El acudiente tiene token
            // 3. NO hay más estudiantes pendientes
            if (preinscripcion.getEstado() == Estado.Aprobada && 
                acudiente.getTokenAccess() != null &&
                noHayMasPendientes) {
                
                mensajeBuilder.append("\n\n¡CREDENCIALES GENERADAS! Se han enviado las credenciales de acceso al acudiente.");
            }
            // Si aún hay estudiantes pendientes, mostrar cuántos faltan
            else if (pendientesDespues > 0) {
                mensajeBuilder.append("\n\nAún hay ").append(pendientesDespues)
                    .append(" estudiante(s) pendiente(s) en esta preinscripción.");
            }
            // Caso especial: preinscripción aprobada pero no se generó token
            else if (preinscripcion.getEstado() == Estado.Aprobada && 
                    acudiente.getTokenAccess() == null) {
                mensajeBuilder.append("\n\nAdvertencia: Preinscripción aprobada pero no se generaron credenciales.");
            }
            
            String mensajeFinal = mensajeBuilder.toString();
            
            transaction.commit();
            return ResultadoOperacion.exito(mensajeFinal);
            
        } catch (Preinscripcion.DomainException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return ResultadoOperacion.error(e.getMessage());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al procesar la aprobación: " + e.getMessage());
        }
    }

    /**
     * Rechaza un estudiante específico
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
            Preinscripcion preinscripcion = estudiante.getPreinscripcion();
            
            if (preinscripcion == null) {
                transaction.rollback();
                return ResultadoOperacion.error("El estudiante no tiene preinscripción asociada");
            }
            
            // 2. Delegar al dominio
            ResultadoOperacion resultadoRechazo = preinscripcion.rechazarEstudiante(idEstudiante);
            
            if (!resultadoRechazo.isExitoso()) {
                transaction.rollback();
                return resultadoRechazo;
            }
            
            // 3. Verificar si todos están rechazados
            if (preinscripcion.todosEstudiantesRechazados()) {
                preinscripcion.setEstado(Estado.Rechazada);
                Acudiente acudiente = preinscripcion.getAcudiente();
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                
                if (acudiente.getTokenAccess() != null) {
                    acudiente.setTokenAccess(null);
                }
            }
            
            // 4. Obtener acudiente actualizado
            Acudiente acudiente = preinscripcion.getAcudiente();
            
            // 5. Persistir TODOS los cambios
            repoPreinscripcion.guardar(preinscripcion);
            repoAcudiente.guardar(acudiente);
            repoEstudiante.guardar(estudiante);
            
            transaction.commit();
            
            // 6. Determinar mensaje final
            if (preinscripcion.todosEstudiantesRechazados()) {
                return ResultadoOperacion.exito(
                    "¡Listo! El estudiante fue rechazado.\n\n" +
                    "TODOS los estudiantes de esta preinscripción están ahora rechazados.\n" +
                    "El acudiente ha sido marcado como RECHAZADO y NO se le generó token de acceso."
                );
            } else {
                // Verificar si AHORA no hay estudiantes pendientes
                long pendientesDespues = preinscripcion.getEstudiantes().stream()
                    .filter(e -> e.getEstado() == Estado.Pendiente)
                    .count();
                
                boolean noHayMasPendientes = (pendientesDespues == 0);
                
                StringBuilder mensaje = new StringBuilder("¡Listo! El estudiante fue rechazado con éxito");
                
                // Si no hay más pendientes y la preinscripción está aprobada
                if (noHayMasPendientes && 
                    preinscripcion.getEstado() == Estado.Aprobada && 
                    acudiente.getTokenAccess() != null) {
                    
                    mensaje.append("\n\n¡CREDENCIALES GENERADAS! Se han enviado las credenciales de acceso al acudiente.");
                }
                // Si aún hay pendientes
                else if (pendientesDespues > 0) {
                    mensaje.append("\n\nAún hay ").append(pendientesDespues)
                        .append(" estudiante(s) pendiente(s) en esta preinscripción.");
                }
                
                return ResultadoOperacion.exito(mensaje.toString());
            }
            
        } catch (Preinscripcion.DomainException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return ResultadoOperacion.error(e.getMessage());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al procesar el rechazo: " + e.getMessage());
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
            
            // Buscar grupo disponible
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
     */
    private Grupo encontrarGrupoDisponibleParaGrado(Integer idGrado) {
        List<Grupo> gruposDelGrado = repoGrupo.buscarPorGrado(idGrado);
        return buscarGrupoDisponible(gruposDelGrado);
    }

    /**
     * Busca un grupo disponible usando reglas de negocio del dominio
     */
    private Grupo buscarGrupoDisponible(List<Grupo> grupos) {
        if (grupos == null || grupos.isEmpty()) {
            return null;
        }
        
        // Optimización: Ordenar grupos inactivos por cantidad de estudiantes (descendente)
        List<Grupo> gruposInactivos = new ArrayList<>();
        for (Grupo grupo : grupos) {
            if (!grupo.isEstado() && !grupo.tieneEstudiantesSuficientes() && grupo.tieneDisponibilidad()) {
                gruposInactivos.add(grupo);
            }
        }
        
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
        
        Long cantidadGrupos = repoGrupo.contarGruposPorGrado(idGrado);
        String nombreGrupo = generarNombreGrupo(grado, cantidadGrupos);
        
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombreGrupo(nombreGrupo);
        nuevoGrupo.setEstado(false);
        nuevoGrupo.setGrado(grado);
        nuevoGrupo.setEstudiantes(new HashSet<>());
        
        return repoGrupo.guardar(nuevoGrupo);
    }
    
    private String generarNombreGrupo(Grado grado, Long cantidadGrupos) {
        return String.format("%s-%d", grado.getNombreGrado(), cantidadGrupos + 1);
    }

    private AspiranteDTO convertirADTO(Preinscripcion preinscripcion) {
        Acudiente acudiente = preinscripcion.getAcudiente();
        if (acudiente == null) {
            return null;
        }
        
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