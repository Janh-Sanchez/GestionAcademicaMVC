package com.controlador;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.GrupoRepositorio;

/**
 * Controlador para gestión de grupos
 * Responsabilidad: Coordinar operaciones de consulta de grupos y estudiantes
 */
public class ConsultarGruposController {
    private final EntityManager entityManager;
    private final GrupoRepositorio grupoRepo;

    public ConsultarGruposController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.grupoRepo = new GrupoRepositorio(entityManager);
    }

    /**
     * Obtiene el grupo asignado a un profesor con sus estudiantes ordenados alfabéticamente
     */
    public ResultadoOperacion obtenerGrupoDeProfesor(Profesor profesor) {
        try {
            if (!profesor.tieneGrupoAsignado()) {
                return ResultadoOperacion.error("No has sido asignado a ningún grupo todavía");
            }
            
            Grupo grupo = profesor.getGrupoAsignado();
            Optional<Grupo> grupoConRelaciones = grupoRepo.buscarPorIdConRelaciones(grupo.getIdGrupo());
            
            if (grupoConRelaciones.isEmpty()) {
                return ResultadoOperacion.error("Error al cargar información del grupo");
            }
            
            Grupo grupoCompleto = grupoConRelaciones.get();
            
            // Ordenar estudiantes alfabéticamente
            List<Estudiante> estudiantesOrdenados = ordenarEstudiantesAlfabeticamente(
                new ArrayList<>(grupoCompleto.getEstudiantes())
            );
            
                DatosGrupoConsulta datos = new DatosGrupoConsulta(
                grupoCompleto,
                estudiantesOrdenados
            );
            
            return ResultadoOperacion.exitoConDatos("Grupo obtenido exitosamente", datos);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener grupo: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los grupos válidos para el directivo (activos con profesor asignado)
     */
    public ResultadoOperacion obtenerTodosLosGruposValidos() {
        try {
            List<Grupo> grupos = grupoRepo.buscarGruposListosConProfesor();
            
            if (grupos.isEmpty()) {
                return ResultadoOperacion.error("No hay grupos disponibles para consultar");
            }
            
            return ResultadoOperacion.exitoConDatos("Grupos obtenidos exitosamente", grupos);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener grupos: " + e.getMessage());
        }
    }

    /**
     * Obtiene un grupo específico por su ID con estudiantes ordenados
     */
    public ResultadoOperacion obtenerGrupoPorId(Integer idGrupo) {
        try {
            Optional<Grupo> grupoOpt = grupoRepo.buscarPorIdConRelaciones(idGrupo);
            
            if (grupoOpt.isEmpty()) {
                return ResultadoOperacion.error("Grupo no encontrado");
            }
            
            Grupo grupo = grupoOpt.get();
            
            // Verificar que el grupo esté válido para consulta
            if (!grupo.estaListo() || !grupo.tieneProfesorAsignado()) {
                return ResultadoOperacion.error("El grupo no está disponible para consulta");
            }
            
            // Ordenar estudiantes alfabéticamente
            List<Estudiante> estudiantesOrdenados = ordenarEstudiantesAlfabeticamente(
                new ArrayList<>(grupo.getEstudiantes())
            );
            
            // Crear DTO con la información necesaria
            DatosGrupoConsulta datos = new DatosGrupoConsulta(
                grupo,
                estudiantesOrdenados
            );
            
            return ResultadoOperacion.exitoConDatos("Grupo obtenido exitosamente", datos);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener grupo: " + e.getMessage());
        }
    }

    /**
     * Ordena una lista de estudiantes alfabéticamente por nombre completo
     */
    private List<Estudiante> ordenarEstudiantesAlfabeticamente(List<Estudiante> estudiantes) {
        return estudiantes.stream()
            .sorted(Comparator.comparing(Estudiante::obtenerNombreCompleto))
            .collect(Collectors.toList());
    }

    /**
     * Clase interna para transportar datos de consulta de grupo
     */
    public static class DatosGrupoConsulta {
        private final Grupo grupo;
        private final List<Estudiante> estudiantesOrdenados;
        
        public DatosGrupoConsulta(Grupo grupo, List<Estudiante> estudiantesOrdenados) {
            this.grupo = grupo;
            this.estudiantesOrdenados = estudiantesOrdenados;
        }
        
        public Grupo getGrupo() {
            return grupo;
        }
        
        public List<Estudiante> getEstudiantesOrdenados() {
            return estudiantesOrdenados;
        }
        
        public String getNombreGrupo() {
            return grupo.getNombreGrupo();
        }
        
        public String getNombreGrado() {
            return grupo.getGrado().getNombreGrado();
        }
        
        public String getNombreProfesor() {
            return grupo.getProfesor() != null ? 
                grupo.getProfesor().obtenerNombreCompleto() : "Sin asignar";
        }
        
        public int getCantidadEstudiantes() {
            return estudiantesOrdenados.size();
        }
    }
}