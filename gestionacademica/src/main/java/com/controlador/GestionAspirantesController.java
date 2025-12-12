package com.controlador;

import com.aplicacion.JPAUtil;
import com.modelo.AsignadorGrupos;
import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import com.modelo.ServicioCorreo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.*;

public class GestionAspirantesController {
    
    private final EntityManager entityManager;
    private final PreinscripcionRepositorio repoPreinscripcion;
    private final RepositorioGenerico<Estudiante> repoEstudiante;
    private final AcudienteRepositorio repoAcudiente;
    private final GrupoRepositorio repoGrupo;
    private final GradoRepositorio repoGrado;
    private final RolRepositorio repoRol;
    
    public GestionAspirantesController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.repoPreinscripcion = new PreinscripcionRepositorio(entityManager);
        this.repoEstudiante = new RepositorioGenerico<>(entityManager, Estudiante.class);
        this.repoAcudiente = new AcudienteRepositorio(entityManager);
        this.repoGrupo = new GrupoRepositorio(entityManager);
        this.repoGrado = new GradoRepositorio(entityManager);
        this.repoRol = new RolRepositorio(entityManager);
    }
    
    /**
     * Obtiene la lista de aspirantes como una lista de mapas simples
     */
    public ResultadoOperacion obtenerListaAspirantes() {
        try {
            List<Preinscripcion> preinscripciones = repoPreinscripcion.buscarTodos();
            
            if (preinscripciones.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            // Lista para almacenar los datos de los aspirantes
            List<Map<String, Object>> listaAspirantes = new ArrayList<>();
            
            for (Preinscripcion preinscripcion : preinscripciones) {
                Acudiente acudiente = preinscripcion.getAcudiente();
                if (acudiente == null) {
                    continue;
                }
                
                // Lista para estudiantes pendientes de esta preinscripción
                List<Map<String, Object>> estudiantesPendientes = new ArrayList<>();
                
                for (Estudiante estudiante : preinscripcion.getEstudiantes()) {
                    if (estudiante.getEstado() == Estado.Pendiente) {
                        Map<String, Object> datosEstudiante = new HashMap<>();
                        datosEstudiante.put("idEstudiante", estudiante.getIdEstudiante());
                        datosEstudiante.put("nombreCompleto", construirNombreCompleto(
                            estudiante.getPrimerNombre(),
                            estudiante.getSegundoNombre(),
                            estudiante.getPrimerApellido(),
                            estudiante.getSegundoApellido()
                        ));
                        datosEstudiante.put("nombreGrado", 
                            estudiante.getGradoAspira() != null ? 
                            estudiante.getGradoAspira().getNombreGrado() : "Sin grado");
                        datosEstudiante.put("estado", estudiante.getEstado());
                        
                        estudiantesPendientes.add(datosEstudiante);
                    }
                }
                
                // Solo agregar si tiene estudiantes pendientes
                if (!estudiantesPendientes.isEmpty()) {
                    Map<String, Object> datosAspirante = new HashMap<>();
                    datosAspirante.put("idPreinscripcion", preinscripcion.getIdPreinscripcion());
                    datosAspirante.put("idAcudiente", acudiente.getIdUsuario());
                    datosAspirante.put("nombreAcudiente", acudiente.obtenerNombreCompleto());
                    datosAspirante.put("estudiantes", estudiantesPendientes);
                    
                    listaAspirantes.add(datosAspirante);
                }
            }
            
            if (listaAspirantes.isEmpty()) {
                return ResultadoOperacion.error("VACIA");
            }
            
            // Ordenar por ID de preinscripción
            listaAspirantes.sort((a1, a2) -> 
                ((Integer) a1.get("idPreinscripcion")).compareTo((Integer) a2.get("idPreinscripcion")));
            
            return ResultadoOperacion.exitoConDatos("Lista obtenida", listaAspirantes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error(
                "Error al acceder a la base de datos, inténtelo nuevamente");
        }
    }
    
    /**
     * Obtiene rol de acudiente
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
            
            Optional<Rol> rolAcudienteOpt = obtenerRolAcudiente();
            if (rolAcudienteOpt.isEmpty()) {
                transaction.rollback();
                return ResultadoOperacion.error(
                    "Error: El rol 'acudiente' no está configurado en el sistema");
            }
            
            Rol rolAcudiente = rolAcudienteOpt.get();
            
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
            
            ResultadoOperacion resultadoAprobacion = preinscripcion.aprobarEstudiante(
                idEstudiante, rolAcudiente);
            
            if (!resultadoAprobacion.isExitoso()) {
                transaction.rollback();
                return resultadoAprobacion;
            }
            
            ResultadoOperacion resultadoAsignacion = asignarEstudianteAGrupo(estudiante);
            if (!resultadoAsignacion.isExitoso()) {
                transaction.rollback();
                return resultadoAsignacion;
            }
            
            repoPreinscripcion.guardar(preinscripcion);
            repoAcudiente.guardar(preinscripcion.getAcudiente());
            repoEstudiante.guardar(estudiante);
            
            Acudiente acudiente = preinscripcion.getAcudiente();
            long pendientesDespues = preinscripcion.getEstudiantes().stream()
                .filter(e -> e.getEstado() == Estado.Pendiente)
                .count();
            
            boolean noHayMasPendientes = (pendientesDespues == 0);
            
            boolean correoEnviado = false;
            if (preinscripcion.getEstado() == Estado.Aprobada && 
                acudiente.getTokenAccess() != null &&
                noHayMasPendientes) {
                
                correoEnviado = ServicioCorreo.enviarCredenciales(
                    acudiente.getCorreoElectronico(),
                    acudiente.obtenerNombreCompleto(),
                    acudiente.getTokenAccess().getNombreUsuario(),
                    acudiente.getTokenAccess().getContrasena(),
                    "Acudiente"
                );
            }
            
            transaction.commit();
            
            StringBuilder mensajeBuilder = new StringBuilder("¡Listo! El estudiante fue aprobado con éxito");
            
            if (preinscripcion.getEstado() == Estado.Aprobada && 
                acudiente.getTokenAccess() != null &&
                noHayMasPendientes) {
                
                if (correoEnviado) {
                    mensajeBuilder.append("\n\n✓ ¡CREDENCIALES GENERADAS Y ENVIADAS! ")
                        .append("Se han enviado las credenciales de acceso al correo electrónico del acudiente.");
                } else {
                    mensajeBuilder.append("\n\n⚠ ¡CREDENCIALES GENERADAS! ")
                        .append("Sin embargo, no se pudo enviar el correo electrónico. ")
                        .append("Por favor, entregue las credenciales manualmente.");
                }
            }
            else if (pendientesDespues > 0) {
                mensajeBuilder.append("\n\nAún hay ").append(pendientesDespues)
                    .append(" estudiante(s) pendiente(s) en esta preinscripción.");
            }
            else if (preinscripcion.getEstado() == Estado.Aprobada && 
                    acudiente.getTokenAccess() == null) {
                mensajeBuilder.append("\n\nAdvertencia: Preinscripción aprobada pero no se generaron credenciales.");
            }
            
            return ResultadoOperacion.exito(mensajeBuilder.toString());
            
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
            
            ResultadoOperacion resultadoRechazo = preinscripcion.rechazarEstudiante(idEstudiante);
            
            if (!resultadoRechazo.isExitoso()) {
                transaction.rollback();
                return resultadoRechazo;
            }
            
            if (preinscripcion.todosEstudiantesRechazados()) {
                preinscripcion.setEstado(Estado.Rechazada);
                Acudiente acudiente = preinscripcion.getAcudiente();
                acudiente.setEstadoAprobacion(Estado.Rechazada);
                
                if (acudiente.getTokenAccess() != null) {
                    acudiente.setTokenAccess(null);
                }
            }
            
            Acudiente acudiente = preinscripcion.getAcudiente();
            
            repoPreinscripcion.guardar(preinscripcion);
            repoAcudiente.guardar(acudiente);
            repoEstudiante.guardar(estudiante);
            
            long pendientesDespues = preinscripcion.getEstudiantes().stream()
                .filter(e -> e.getEstado() == Estado.Pendiente)
                .count();
            
            boolean noHayMasPendientes = (pendientesDespues == 0);
            
            boolean correoEnviado = false;
            if (noHayMasPendientes && 
                preinscripcion.getEstado() == Estado.Aprobada && 
                acudiente.getTokenAccess() != null) {
                
                correoEnviado = ServicioCorreo.enviarCredenciales(
                    acudiente.getCorreoElectronico(),
                    acudiente.obtenerNombreCompleto(),
                    acudiente.getTokenAccess().getNombreUsuario(),
                    acudiente.getTokenAccess().getContrasena(),
                    "Acudiente"
                );
            }
            
            transaction.commit();
            
            if (preinscripcion.todosEstudiantesRechazados()) {
                return ResultadoOperacion.exito(
                    "¡Listo! El estudiante fue rechazado.\n\n" +
                    "TODOS los estudiantes de esta preinscripción están ahora rechazados.\n" +
                    "El acudiente ha sido marcado como RECHAZADO y NO se le generó token de acceso."
                );
            } else {
                StringBuilder mensaje = new StringBuilder("¡Listo! El estudiante fue rechazado con éxito");
                
                if (noHayMasPendientes && 
                    preinscripcion.getEstado() == Estado.Aprobada && 
                    acudiente.getTokenAccess() != null) {
                    
                    if (correoEnviado) {
                        mensaje.append("\n\n✓ ¡CREDENCIALES GENERADAS Y ENVIADAS! ")
                            .append("Se han enviado las credenciales de acceso al correo electrónico del acudiente.");
                    } else {
                        mensaje.append("\n\n⚠ ¡CREDENCIALES GENERADAS! ")
                            .append("Sin embargo, no se pudo enviar el correo electrónico. ")
                            .append("Por favor, entregue las credenciales manualmente.");
                    }
                }
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

    private ResultadoOperacion asignarEstudianteAGrupo(Estudiante estudiante) {
        try {
            if (estudiante.getGradoAspira() == null) {
                return ResultadoOperacion.error("El estudiante no tiene grado asignado");
            }
            
            Integer idGrado = estudiante.getGradoAspira().getIdGrado();
            
            List<Grupo> gruposDelGrado = repoGrupo.buscarPorGrado(idGrado);
            
            Grupo grupoDisponible = AsignadorGrupos.encontrarGrupoParaEstudiante(gruposDelGrado);
            
            if (grupoDisponible != null) {
                boolean asignado = grupoDisponible.agregarEstudiante(estudiante);
                if (asignado) {
                    repoGrupo.guardar(grupoDisponible);
                    
                    String mensaje = String.format(
                        "Estudiante asignado al grupo '%s' (%d/%d estudiantes)",
                        grupoDisponible.getNombreGrupo(),
                        grupoDisponible.getCantidadEstudiantes(),
                        grupoDisponible.getMAXESTUDIANTES()
                    );
                    return ResultadoOperacion.exito(mensaje);
                }
            }
            
            Grupo nuevoGrupo = crearNuevoGrupo(idGrado);
            boolean asignado = nuevoGrupo.agregarEstudiante(estudiante);
            
            if (asignado) {
                repoGrupo.guardar(nuevoGrupo);
                String mensaje = String.format(
                    "Nuevo grupo '%s' creado y estudiante asignado",
                    nuevoGrupo.getNombreGrupo()
                );
                return ResultadoOperacion.exito(mensaje);
            }
            
            return ResultadoOperacion.error("No se pudo asignar estudiante a ningún grupo");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al asignar grupo: " + e.getMessage());
        }
    }
    
    private Grupo crearNuevoGrupo(Integer idGrado) throws Exception {
        Optional<Grado> gradoOpt = repoGrado.buscarPorId(idGrado);
        if (gradoOpt.isEmpty()) {
            throw new Exception("Grado no encontrado con ID: " + idGrado);
        }
        
        Grado grado = gradoOpt.get();
        
        Long cantidadGrupos = repoGrupo.contarGruposPorGrado(idGrado);
        
        String nombreGrupo = AsignadorGrupos.generarNombreNuevoGrupo(grado, cantidadGrupos);
        
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombreGrupo(nombreGrupo);
        nuevoGrupo.setEstado(false);
        nuevoGrupo.setGrado(grado);
        nuevoGrupo.setEstudiantes(new HashSet<>());
        
        return nuevoGrupo;
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