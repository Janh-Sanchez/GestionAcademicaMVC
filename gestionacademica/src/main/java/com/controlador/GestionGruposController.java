package com.controlador;

import com.aplicacion.JPAUtil;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;

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
    
    /**
     * Obtiene la lista completa de grupos con su información
     */
    public ResultadoOperacion obtenerListaGrupos() {
        try {
            List<Grupo> grupos = repoGrupo.buscarTodosOrdenadosConInfo();
            
            if (grupos.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            return ResultadoOperacion.exitoConDatos("Lista obtenida", grupos);
            
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
            
            return ResultadoOperacion.exitoConDatos(
                "Profesores disponibles", profesores);
            
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
}