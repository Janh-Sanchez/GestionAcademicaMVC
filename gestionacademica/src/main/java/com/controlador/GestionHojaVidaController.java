package com.controlador;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.AcudienteRepositorio;
import com.modelo.persistencia.repositorios.EstudianteRepositorio;
import com.modelo.persistencia.repositorios.GrupoRepositorio;
import com.modelo.persistencia.repositorios.HojaVidaRepositorio;

/**
 * Controlador para gestión de hojas de vida
 * Responsabilidad: Coordinar operaciones de consulta y modificación de hojas de vida
 */
public class GestionHojaVidaController {
    private final EntityManager entityManager;
    private final HojaVidaRepositorio hojaVidaRepo;
    private final EstudianteRepositorio estudianteRepo;
    private final GrupoRepositorio grupoRepo;
    private final AcudienteRepositorio acudienteRepo;

    public GestionHojaVidaController(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.hojaVidaRepo = new HojaVidaRepositorio(entityManager);
        this.estudianteRepo = new EstudianteRepositorio(entityManager);
        this.grupoRepo = new GrupoRepositorio(entityManager);
        this.acudienteRepo = new AcudienteRepositorio(entityManager);
    }

    /**
     * Obtiene la hoja de vida de un estudiante
     */
    public ResultadoOperacion obtenerHojaVidaDeEstudiante(Integer idEstudiante) {
        try {
            Optional<HojaVida> hojaVidaOpt = hojaVidaRepo.buscarPorEstudianteConEstudiante(idEstudiante);
            
            if (hojaVidaOpt.isEmpty()) {
                return ResultadoOperacion.error("El estudiante no tiene hoja de vida registrada");
            }
            
            return ResultadoOperacion.exitoConDatos("Hoja de vida encontrada", hojaVidaOpt.get());
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener hoja de vida: " + e.getMessage());
        }
    }

    /**
     * Verifica si todos los estudiantes de un acudiente tienen hoja de vida completa
     */
    public ResultadoOperacion verificarHojasVidaCompletas(Acudiente acudiente) {
        try {
            // Recargar acudiente con estudiantes
            Acudiente acudienteCompleto = acudienteRepo.buscarConEstudiantes(acudiente.getIdUsuario());
            
            if (acudienteCompleto == null) {
                return ResultadoOperacion.error("Acudiente no encontrado");
            }
            
            Set<Estudiante> estudiantes = acudienteCompleto.getEstudiantes();
            
            if (estudiantes == null || estudiantes.isEmpty()) {
                return ResultadoOperacion.exitoConDatos("Sin estudiantes", true);
            }
            
            // Verificar cada estudiante
            for (Estudiante estudiante : estudiantes) {
                Optional<HojaVida> hojaVidaOpt = hojaVidaRepo.buscarPorEstudiante(estudiante.getIdEstudiante());
                
                if (hojaVidaOpt.isEmpty() || !hojaVidaOpt.get().estaCompleta()) {
                    return ResultadoOperacion.exitoConDatos("Hojas incompletas", false);
                }
            }
            
            return ResultadoOperacion.exitoConDatos("Todas las hojas completas", true);
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al verificar hojas de vida: " + e.getMessage());
        }
    }

    /**
     * Guarda o actualiza la hoja de vida de un estudiante
     */
    public ResultadoOperacion guardarHojaVida(Integer idEstudiante, String alergias, 
                                             String aspectosRelevantes, String enfermedades) {
        try {
            entityManager.getTransaction().begin();
            
            // Buscar estudiante
            Optional<Estudiante> estudianteOpt = estudianteRepo.buscarPorId(idEstudiante);
            if (estudianteOpt.isEmpty()) {
                entityManager.getTransaction().rollback();
                return ResultadoOperacion.error("Estudiante no encontrado");
            }
            
            Estudiante estudiante = estudianteOpt.get();
            
            // Buscar o crear hoja de vida
            Optional<HojaVida> hojaVidaOpt = hojaVidaRepo.buscarPorEstudiante(idEstudiante);
            
            HojaVida hojaVida;
            boolean esNueva = false;
            
            if (hojaVidaOpt.isEmpty()) {
                hojaVida = new HojaVida();
                hojaVida.setEstudiante(estudiante);
                esNueva = true;
            } else {
                hojaVida = hojaVidaOpt.get();
            }
            
            // Establecer valores
            hojaVida.setAlergias(alergias);
            hojaVida.setAspectosRelevantes(aspectosRelevantes);
            hojaVida.setEnfermedades(enfermedades);
            
            // Validar
            ResultadoValidacionDominio validacion = hojaVida.validar();
            if (!validacion.isValido()) {
                entityManager.getTransaction().rollback();
                return ResultadoOperacion.errorValidacion(
                    validacion.getCampoInvalido(), 
                    validacion.getMensajeError());
            }
            
            // Guardar o actualizar
            if (esNueva) {
                hojaVidaRepo.guardar(hojaVida);
                estudiante.setHojaDeVida(hojaVida);
            } else {
                hojaVidaRepo.actualizar(hojaVida);
            }
            
            entityManager.getTransaction().commit();
            
            return ResultadoOperacion.exitoConDatos("Hoja de vida guardada exitosamente", hojaVida);
            
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return ResultadoOperacion.error("Error al guardar hoja de vida: " + e.getMessage());
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
     * Obtiene todos los grupos válidos con estudiantes para el directivo
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

    /**
     * Obtiene los estudiantes de un acudiente
     */
    public ResultadoOperacion obtenerEstudiantesDeAcudiente(Acudiente acudiente) {
        try {
            Acudiente acudienteCompleto = acudienteRepo.buscarConEstudiantes(acudiente.getIdUsuario());
            
            if (acudienteCompleto == null) {
                return ResultadoOperacion.error("Acudiente no encontrado");
            }
            
            return ResultadoOperacion.exitoConDatos("Estudiantes obtenidos", 
                acudienteCompleto.getEstudiantes());
            
        } catch (Exception e) {
            return ResultadoOperacion.error("Error al obtener estudiantes: " + e.getMessage());
        }
    }
}