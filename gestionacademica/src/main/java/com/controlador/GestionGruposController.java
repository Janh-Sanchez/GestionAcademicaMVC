package com.controlador;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar la asignación de profesores a grupos
 * Implementa RF - Asignar profesor a grupo
 * Arquitectura MVC limpia sin servicios intermedios
 */
public class GestionGruposController {
    
    private final EntityManager entityManager;
    private final GrupoRepositorio repoGrupo;
    private final ProfesorRepositorio repoProfesor;
    
    public GestionGruposController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.repoGrupo = new GrupoRepositorio(entityManager);
        this.repoProfesor = new ProfesorRepositorio(entityManager);
    }
    
    // ============================================
    // DTOs PARA TRANSFERENCIA DE DATOS
    // ============================================
    
    /**
     * DTO para transferir información de grupos a la UI
     */
    public static class GrupoDTO {
        private Integer idGrupo;
        private String nombreGrupo;
        private String nombreGrado;
        private int cantidadEstudiantes;
        private boolean estaListo;
        private boolean estaEnFormacion;
        private ProfesorDTO profesorAsignado;
        
        public GrupoDTO(Integer idGrupo, String nombreGrupo, String nombreGrado,
                       int cantidadEstudiantes, boolean estaListo, boolean estaEnFormacion,
                       ProfesorDTO profesorAsignado) {
            this.idGrupo = idGrupo;
            this.nombreGrupo = nombreGrupo;
            this.nombreGrado = nombreGrado;
            this.cantidadEstudiantes = cantidadEstudiantes;
            this.estaListo = estaListo;
            this.estaEnFormacion = estaEnFormacion;
            this.profesorAsignado = profesorAsignado;
        }
        
        public Integer getIdGrupo() { return idGrupo; }
        public String getNombreGrupo() { return nombreGrupo; }
        public String getNombreGrado() { return nombreGrado; }
        public int getCantidadEstudiantes() { return cantidadEstudiantes; }
        public boolean isEstaListo() { return estaListo; }
        public boolean isEstaEnFormacion() { return estaEnFormacion; }
        public ProfesorDTO getProfesorAsignado() { return profesorAsignado; }
    }
    
    /**
     * DTO para transferir información de profesores a la UI
     */
    public static class ProfesorDTO {
        private Integer idProfesor;
        private String nombreCompleto;
        private boolean tieneGrupoAsignado;
        
        public ProfesorDTO(Integer idProfesor, String nombreCompleto, 
                          boolean tieneGrupoAsignado) {
            this.idProfesor = idProfesor;
            this.nombreCompleto = nombreCompleto;
            this.tieneGrupoAsignado = tieneGrupoAsignado;
        }
        
        public Integer getIdProfesor() { return idProfesor; }
        public String getNombreCompleto() { return nombreCompleto; }
        public boolean isTieneGrupoAsignado() { return tieneGrupoAsignado; }
    }
    
    // Metodos publicos

    /**
     * Obtiene la lista completa de grupos con su información
     */
    public ResultadoOperacion obtenerListaGrupos() {
        try {
            List<Grupo> grupos = repoGrupo.buscarTodosOrdenadosConInfo();
            
            if (grupos.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            List<GrupoDTO> gruposDTO = grupos.stream()
                .map(this::convertirGrupoADTO)
                .collect(Collectors.toList());
            
            return ResultadoOperacion.exitoConDatos("Lista obtenida", gruposDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al acceder a la base de datos, inténtelo nuevamente");
        }
    }
    
    /**
     * Obtiene la lista de profesores disponibles (sin grupo asignado)
     */
    public ResultadoOperacion obtenerProfesoresDisponibles() {
        try {
            List<Profesor> profesores = repoProfesor.buscarProfesoresSinGrupo();
            
            if (profesores.isEmpty()) {
                return ResultadoOperacion.error("No hay profesores disponibles");
            }
            
            List<ProfesorDTO> profesoresDTO = profesores.stream()
                .map(this::convertirProfesorADTO)
                .collect(Collectors.toList());
            
            return ResultadoOperacion.exitoConDatos(
                "Profesores disponibles", profesoresDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al obtener profesores disponibles");
        }
    }
    
    /**
     * Asigna un profesor a un grupo específico
     */
    public ResultadoOperacion asignarProfesorAGrupo(Integer idGrupo, Integer idProfesor) {
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 1. Validar y obtener grupo
            Optional<Grupo> grupoOpt = repoGrupo.buscarPorIdConRelaciones(idGrupo);
            if (grupoOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Grupo no encontrado");
            }
            
            Grupo grupo = grupoOpt.get();
            
            // 2. Validar que el grupo esté listo
            if (grupo.estaEnFormacion()) {
                transaction.rollback();
                return ResultadoOperacion.error(
                    "El grupo " + grupo.getNombreGrupo() + " aún está en formación. " +
                    "Necesita al menos 5 estudiantes.");
            }
            
            if (grupo.tieneProfesorAsignado()) {
                transaction.rollback();
                return ResultadoOperacion.error(
                    "El grupo ya tiene asignado al profesor: " + 
                    grupo.getProfesor().obtenerNombreCompleto());
            }
            
            // 3. Validar y obtener profesor
            Optional<Profesor> profesorOpt = repoProfesor.buscarPorIdConGrupo(idProfesor);
            if (profesorOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error("Profesor no encontrado");
            }
            
            Profesor profesor = profesorOpt.get();
            
            // 4. Validar que el profesor no tenga grupo asignado
            if (profesor.tieneGrupoAsignado()) {
                transaction.rollback();
                return ResultadoOperacion.error(
                    "El profesor ya tiene asignado el grupo: " + 
                    profesor.getGrupoAsignado().getNombreGrupo());
            }
            
            // 5. Realizar asignación usando lógica de dominio
            ResultadoOperacion resultadoAsignacion = profesor.asignarGrupo(grupo);
            if (!resultadoAsignacion.isExitoso()) {
                transaction.rollback();
                return resultadoAsignacion;
            }
            
            // 6. Persistir cambios
            repoProfesor.guardar(profesor);
            repoGrupo.guardar(grupo);
            
            transaction.commit();
            
            System.out.println("Asignación exitosa: Profesor " + 
                             profesor.obtenerNombreCompleto() + 
                             " asignado al grupo " + grupo.getNombreGrupo());
            
            return ResultadoOperacion.exito(
                "¡Listo! El profesor fue asignado correctamente al grupo");
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al asignar profesor: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene estadísticas de grupos y profesores
     */
    public ResultadoOperacion obtenerEstadisticas() {
        try {
            Long gruposListos = repoGrupo.contarGruposListosSinProfesor();
            Long profesoresDisponibles = repoProfesor.contarProfesoresSinGrupo();
            
            Map<String, Long> estadisticas = new HashMap<>();
            estadisticas.put("gruposListosSinProfesor", gruposListos);
            estadisticas.put("profesoresDisponibles", profesoresDisponibles);
            
            return ResultadoOperacion.exitoConDatos("Estadísticas", estadisticas);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al obtener estadísticas");
        }
    }
    
    // ============================================
    // MÉTODOS PRIVADOS DE CONVERSIÓN
    // ============================================
    
    /**
     * Convierte una entidad Grupo a DTO para la vista
     */
    private GrupoDTO convertirGrupoADTO(Grupo grupo) {
        ProfesorDTO profesorDTO = null;
        if (grupo.tieneProfesorAsignado()) {
            Profesor profesor = grupo.getProfesor();
            profesorDTO = new ProfesorDTO(
                profesor.getIdUsuario(),
                profesor.obtenerNombreCompleto(),
                true
            );
        }
        
        int cantidadEstudiantes = grupo.getCantidadEstudiantes();
        
        return new GrupoDTO(
            grupo.getIdGrupo(),
            grupo.getNombreGrupo(),
            grupo.getGrado() != null ? grupo.getGrado().getNombreGrado() : "Sin grado",
            cantidadEstudiantes,  // Usamos el método ya implementado
            grupo.estaListo(),
            grupo.estaEnFormacion(),
            profesorDTO
        );
    }
    
    /**
     * Convierte una entidad Profesor a DTO para la vista
     */
    private ProfesorDTO convertirProfesorADTO(Profesor profesor) {
        return new ProfesorDTO(
            profesor.getIdUsuario(),
            profesor.obtenerNombreCompleto(),
            profesor.tieneGrupoAsignado()
        );
    }
}