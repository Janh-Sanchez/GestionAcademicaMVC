package com.controlador;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.EstudianteRepositorio;
import com.modelo.persistencia.repositorios.GrupoRepositorio;
import com.modelo.persistencia.repositorios.ObservacionRepositorio;
import com.modelo.persistencia.repositorios.ObservadorRepositorio;

/**
 * Controlador para gestión de observadores
 * Responsabilidad: Coordinar operaciones de consulta y modificación de observadores
 */
public class GestionObservadorController {
    private final EntityManager entityManager;
    private final ObservadorRepositorio observadorRepo;
    private final ObservacionRepositorio observacionRepo;
    private final EstudianteRepositorio estudianteRepo;
    private final GrupoRepositorio grupoRepo;

    public GestionObservadorController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.observadorRepo = new ObservadorRepositorio(entityManager);
        this.observacionRepo = new ObservacionRepositorio(entityManager);
        this.estudianteRepo = new EstudianteRepositorio(entityManager);
        this.grupoRepo = new GrupoRepositorio(entityManager);
    }

    /**
     * Obtiene el observador de un estudiante con todas sus observaciones
     */
    public ResultadoOperacion obtenerObservadorDeEstudiante(Integer idEstudiante) {
        try {
            Optional<Observador> observadorOpt = observadorRepo.buscarPorEstudianteConObservaciones(idEstudiante);
            
            if (observadorOpt.isEmpty()) {
                return ResultadoOperacion.error("El estudiante no tiene observador asignado");
            }
            
            return ResultadoOperacion.exitoConDatos("Observador encontrado", observadorOpt.get());
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener observador: " + e.getMessage());
        }
    }

    /**
     * Crea el observador para un estudiante si no existe
     */
    public ResultadoOperacion crearObservadorSiNoExiste(Estudiante estudiante) {
        try {
            entityManager.getTransaction().begin();
            
            // Verificar si ya tiene observador
            if (observadorRepo.existePorEstudiante(estudiante.getIdEstudiante())) {
                entityManager.getTransaction().rollback();
                return ResultadoOperacion.error("El estudiante ya tiene un observador");
            }
            
            // Crear nuevo observador
            Observador nuevoObservador = new Observador();
            nuevoObservador.setEstudiante(estudiante);
            
            observadorRepo.guardar(nuevoObservador);
            
            entityManager.getTransaction().commit();
            
            return ResultadoOperacion.exitoConDatos("Observador creado exitosamente", nuevoObservador);
            
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return ResultadoOperacion.error("Error al crear observador: " + e.getMessage());
        }
    }

    /**
     * Agrega una observación al observador de un estudiante
     */
    public ResultadoOperacion agregarObservacion(Integer idEstudiante, String descripcion, Profesor profesor) {
        try {
            entityManager.getTransaction().begin();
            
            // Buscar o crear observador
            Optional<Observador> observadorOpt = observadorRepo.buscarPorEstudiante(idEstudiante);
            
            Observador observador;
            if (observadorOpt.isEmpty()) {
                // Crear observador si no existe
                Optional<Estudiante> estudianteOpt = estudianteRepo.buscarPorId(idEstudiante);
                if (estudianteOpt.isEmpty()) {
                    entityManager.getTransaction().rollback();
                    return ResultadoOperacion.error("Estudiante no encontrado");
                }
                
                observador = new Observador();
                observador.setEstudiante(estudianteOpt.get());
                observadorRepo.guardar(observador);
            } else {
                observador = observadorOpt.get();
            }
            
            // Crear nueva observación
            Observacion nuevaObservacion = new Observacion();
            nuevaObservacion.setDescripcion(descripcion);
            nuevaObservacion.setFechaObservacion(LocalDate.now());
            nuevaObservacion.setObservador(observador);
            nuevaObservacion.setProfesor(profesor);
            
            // Validar observación
            if (!nuevaObservacion.esValida()) {
                entityManager.getTransaction().rollback();
                return ResultadoOperacion.error("La observación debe tener entre 10 y 200 caracteres");
            }
            
            observador.agregarObservacion(nuevaObservacion);
            observacionRepo.guardar(nuevaObservacion);
            
            entityManager.getTransaction().commit();
            
            return ResultadoOperacion.exitoConDatos("Observación agregada exitosamente", nuevaObservacion);
            
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return ResultadoOperacion.error("Error al agregar observación: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los estudiantes del grupo de un profesor
     */
    public ResultadoOperacion obtenerEstudiantesDelGrupo(Profesor profesor) {
        try {
            if (!profesor.tieneGrupoAsignado()) {
                return ResultadoOperacion.error("El profesor no tiene grupo asignado");
            }
            
            Grupo grupo = profesor.getGrupoAsignado();
            Optional<Grupo> grupoConEstudiantes = grupoRepo.buscarPorIdConRelaciones(grupo.getIdGrupo());
            
            if (grupoConEstudiantes.isEmpty()) {
                return ResultadoOperacion.error("Grupo no encontrado");
            }
            
            return ResultadoOperacion.exitoConDatos("Estudiantes obtenidos", 
                grupoConEstudiantes.get().getEstudiantes());
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener estudiantes: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los grupos con estudiantes para el directivo
     */
    public ResultadoOperacion obtenerTodosLosGruposValidos() {
        try {
            List<Grupo> grupos = grupoRepo.buscarGruposListosConProfesor();
            return ResultadoOperacion.exitoConDatos("Grupos obtenidos", grupos);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener grupos: " + e.getMessage());
        }
    }

    /**
     * Verifica si un acudiente tiene acceso a un estudiante
     */
    public boolean acudienteTieneAccesoAEstudiante(Acudiente acudiente, Integer idEstudiante) {
        return acudiente.getEstudiantes().stream()
            .anyMatch(est -> est.getIdEstudiante().equals(idEstudiante));
    }
}