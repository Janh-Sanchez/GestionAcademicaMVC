package com.servicios;

import com.dominio.*;
import com.persistencia.entidades.*;
import com.persistencia.mappers.DominioAPersistenciaMapper;
import com.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la aprobación y rechazo de aspirantes
 * Implementa RF 3.4 - Aprobar o rechazar aspirante
 */
public class GestionAspirantesService {
    
    private final EntityManager entityManager;
    private final RepositorioGenerico<PreinscripcionEntity> repoPreinscripcion;
    private final RepositorioGenerico<EstudianteEntity> repoEstudiante;
    private final RepositorioGenerico<AcudienteEntity> repoAcudiente;
    private final GrupoRepositorio repoGrupo;
    private final GradoRepositorio repoGrado;
    private final GestionUsuariosService gestionUsuariosService;
    
    public GestionAspirantesService(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.repoPreinscripcion = new RepositorioGenerico<>(entityManager, PreinscripcionEntity.class);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, EstudianteEntity.class);
        this.repoAcudiente = new RepositorioGenerico<>(entityManager, AcudienteEntity.class);
        this.repoGrupo = new GrupoRepositorio(entityManager);
        this.repoGrado = new GradoRepositorio(entityManager);
        this.gestionUsuariosService = new GestionUsuariosService();
    }
    
    /**
     * Clase para encapsular el resultado de operaciones
     */
    public static class ResultadoOperacion {
        private final boolean exitoso;
        private final String mensaje;
        private final Object dato;
        
        private ResultadoOperacion(boolean exitoso, String mensaje, Object dato) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.dato = dato;
        }
        
        public static ResultadoOperacion exito(String mensaje, Object dato) {
            return new ResultadoOperacion(true, mensaje, dato);
        }
        
        public static ResultadoOperacion exito(String mensaje) {
            return new ResultadoOperacion(true, mensaje, null);
        }
        
        public static ResultadoOperacion error(String mensaje) {
            return new ResultadoOperacion(false, mensaje, null);
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public Object getDato() { return dato; }
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
            String jpql = "SELECT p FROM preinscripcion p " +
                         "WHERE p.estado = :estado " +
                         "ORDER BY p.fechaRegistro ASC";
            
            List<PreinscripcionEntity> preinscripciones = entityManager
                .createQuery(jpql, PreinscripcionEntity.class)
                .setParameter("estado", Estado.Pendiente)
                .getResultList();
            
            if (preinscripciones.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            List<AspiranteDTO> aspirantes = preinscripciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
            
            return ResultadoOperacion.exito("Lista obtenida", aspirantes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al acceder a la base de datos, inténtelo nuevamente");
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
            
            // 1. Buscar estudiante
            Optional<EstudianteEntity> estudianteOpt = repoEstudiante.buscarPorId(idEstudiante);
            if (estudianteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Estudiante no encontrado");
            }
            
            EstudianteEntity estudiante = estudianteOpt.get();
            AcudienteEntity acudiente = estudiante.getAcudiente();
            
            if (acudiente == null) {
                transaction.rollback();
                return ResultadoOperacion.error("Acudiente no encontrado");
            }
            
            // 2. Verificar si el acudiente ya tenía estudiantes aprobados previamente
            boolean acudienteTieneEstudiantesAprobados = tieneEstudiantesAprobados(acudiente);
            
            // 3. Si el acudiente NO tenía estudiantes aprobados, aprobarlo también
            if (!acudienteTieneEstudiantesAprobados) {
                acudiente.setEstadoAprobacion(Estado.Aprobada);
                repoAcudiente.guardar(acudiente);
                
                // 4. NO CREAR USUARIO - EL ACUDIENTE YA ES UN USUARIO
                // El acudiente ya existe en la tabla usuario (heredando o relacionado)
                // Solo necesitamos actualizar su estado de aprobación
                
                System.out.println("Acudiente aprobado. ID: " + acudiente.getIdUsuario() + 
                                ", Correo: " + acudiente.getCorreoElectronico());
            }
            
            // 5. Aprobar estudiante
            estudiante.setEstado(Estado.Aprobada);
            
            // 6. Asignar estudiante a un grupo
            ResultadoOperacion resultadoAsignacion = asignarEstudianteAGrupo(estudiante);
            if (!resultadoAsignacion.isExitoso()) {
                transaction.rollback();
                return resultadoAsignacion;
            }
            
            repoEstudiante.guardar(estudiante);
            
            transaction.commit();
            return ResultadoOperacion.exito("¡Listo! El estudiante fue aprobado con éxito");
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error("Error al acceder a la base de datos: " + e.getMessage());
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
            
            // 1. Buscar estudiante
            Optional<EstudianteEntity> estudianteOpt = repoEstudiante.buscarPorId(idEstudiante);
            if (estudianteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Estudiante no encontrado");
            }
            
            EstudianteEntity estudiante = estudianteOpt.get();
            AcudienteEntity acudiente = estudiante.getAcudiente();
            
            if (acudiente == null) {
                transaction.rollback();
                return ResultadoOperacion.error("Acudiente no encontrado");
            }
            
            // 2. Verificar si el acudiente tiene más estudiantes con estado pendiente
            boolean tieneEstudiantesPendientes = tieneEstudiantesPendientes(acudiente, idEstudiante);
            
            // 3. Si NO tiene más estudiantes pendientes, rechazar también al acudiente
            if (!tieneEstudiantesPendientes) {
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                repoAcudiente.guardar(acudiente);
            }
            
            // 4. Rechazar estudiante
            estudiante.setEstado(Estado.Rechazada);
            repoEstudiante.guardar(estudiante);
            
            transaction.commit();
            return ResultadoOperacion.exito("¡Listo! El estudiante fue rechazado con éxito");
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error("Error al acceder a la base de datos, inténtelo nuevamente");
        }
    }
    
    /**
     * Asigna un estudiante a un grupo según las reglas de negocio
     */
    private ResultadoOperacion asignarEstudianteAGrupo(EstudianteEntity estudiante) {
        try {
            if (estudiante.getGradoAspira() == null) {
                return ResultadoOperacion.error("El estudiante no tiene grado asignado");
            }
            
            Integer idGrado = estudiante.getGradoAspira().getIdGrado();
            
            // Buscar grupos del grado ordenados por número de estudiantes
            String jpql = "SELECT g FROM grupo g " +
                         "WHERE g.grado.idGrado = :idGrado " +
                         "AND g.estado = true " +
                         "ORDER BY SIZE(g.estudiantes) ASC";
            
            List<GrupoEntity> grupos = entityManager
                .createQuery(jpql, GrupoEntity.class)
                .setParameter("idGrado", idGrado)
                .getResultList();
            
            GrupoEntity grupoAsignado = null;
            
            // Estrategia: Primero llenar grupos hasta el mínimo, luego distribuir
            for (GrupoEntity grupo : grupos) {
                Grupo grupoDomain = DominioAPersistenciaMapper.toDomain(grupo);
                
                // Si el grupo no ha alcanzado el mínimo, asignar ahí
                if (!grupoDomain.tieneEstudiantesSuficientes()) {
                    grupoAsignado = grupo;
                    break;
                }
            }
            
            // Si todos tienen el mínimo, buscar uno con disponibilidad
            if (grupoAsignado == null) {
                for (GrupoEntity grupo : grupos) {
                    Grupo grupoDomain = DominioAPersistenciaMapper.toDomain(grupo);
                    
                    if (grupoDomain.tieneDisponibilidad()) {
                        grupoAsignado = grupo;
                        break;
                    }
                }
            }
            
            // Si no hay grupos con disponibilidad, crear uno nuevo
            if (grupoAsignado == null) {
                grupoAsignado = crearNuevoGrupo(idGrado);
            }
            
            // Asignar estudiante al grupo
            estudiante.setGrupo(grupoAsignado);
            
            return ResultadoOperacion.exito("Estudiante asignado a grupo");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al asignar grupo: " + e.getMessage());
        }
    }
    
    /**
     * Crea un nuevo grupo para un grado específico
     */
    private GrupoEntity crearNuevoGrupo(Integer idGrado) throws Exception {
        Optional<GradoEntity> gradoOpt = repoGrado.buscarPorId(idGrado);
        if (gradoOpt.isEmpty()) {
            throw new Exception("Grado no encontrado");
        }
        
        GradoEntity grado = gradoOpt.get();
        
        // Contar grupos existentes del grado para generar nombre
        String jpqlCount = "SELECT COUNT(g) FROM grupo g WHERE g.grado.idGrado = :idGrado";
        Long cantidadGrupos = entityManager
            .createQuery(jpqlCount, Long.class)
            .setParameter("idGrado", idGrado)
            .getSingleResult();
        
        // Crear nuevo grupo
        GrupoEntity nuevoGrupo = new GrupoEntity();
        nuevoGrupo.setNombreGrupo(grado.getNombreGrado() + "-" + (cantidadGrupos + 1));
        nuevoGrupo.setEstado(true);
        nuevoGrupo.setGrado(grado);
        nuevoGrupo.setEstudiantes(new HashSet<>());
        
        return repoGrupo.guardar(nuevoGrupo);
    }
    
    /**
     * Verifica si un acudiente tiene estudiantes aprobados
     */
    private boolean tieneEstudiantesAprobados(AcudienteEntity acudiente) {
        String jpql = "SELECT COUNT(e) FROM estudiante e " +
                     "WHERE e.acudiente.idUsuario = :idAcudiente " +
                     "AND e.estado = :estado";
        
        Long count = entityManager
            .createQuery(jpql, Long.class)
            .setParameter("idAcudiente", acudiente.getIdUsuario())
            .setParameter("estado", Estado.Aprobada)
            .getSingleResult();
        
        return count > 0;
    }
    
    /**
     * Verifica si un acudiente tiene más estudiantes pendientes (excepto el indicado)
     */
    private boolean tieneEstudiantesPendientes(AcudienteEntity acudiente, Integer idEstudianteExcluir) {
        String jpql = "SELECT COUNT(e) FROM estudiante e " +
                     "WHERE e.acudiente.idUsuario = :idAcudiente " +
                     "AND e.estado = :estado " +
                     "AND e.idEstudiante != :idExcluir";
        
        Long count = entityManager
            .createQuery(jpql, Long.class)
            .setParameter("idAcudiente", acudiente.getIdUsuario())
            .setParameter("estado", Estado.Pendiente)
            .setParameter("idExcluir", idEstudianteExcluir)
            .getSingleResult();
        
        return count > 0;
    }
    
    /**
     * Convierte una entidad de preinscripción a DTO
     */
    private AspiranteDTO convertirADTO(PreinscripcionEntity preinscripcion) {
        AcudienteEntity acudiente = preinscripcion.getAcudiente();
        String nombreAcudiente = construirNombreCompleto(
            acudiente.getPrimerNombre(),
            acudiente.getSegundoNombre(),
            acudiente.getPrimerApellido(),
            acudiente.getSegundoApellido()
        );
        
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
    
    public void cerrar() {
        if (gestionUsuariosService != null) {
            gestionUsuariosService.cerrar();
        }
    }
}