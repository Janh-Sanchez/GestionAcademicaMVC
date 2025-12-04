package com.servicios;

import com.dominio.*;
import com.persistencia.repositorios.*;
import com.persistencia.mappers.DominioAPersistenciaMapper;
import com.persistencia.entidades.*;
import com.aplicacion.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

/**
 * Servicio de gestión de usuarios - Capa de Servicios
 * Responsabilidad: Casos de uso, orquestación transaccional, coordinación de entidades
 */
public class GestionUsuariosService {
    private final EntityManager em;
    private final TokenService tokenService;
    private final UsuarioRepositorio repositorioUsuario;
    private final RolRepositorio repositorioRol;

    public GestionUsuariosService() {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
        this.em = emf.createEntityManager();
        this.repositorioUsuario = new UsuarioRepositorio(em);
        this.repositorioRol = new RolRepositorio(em);
        this.tokenService = new TokenService(em);
    }
    
    /**
     * CU 2.3 - Crear usuario con transacción única
     */
    public ResultadoOperacion crearUsuario(Usuario usuario, String nombreRol) {
        try {
            em.getTransaction().begin();
            
            try {
                // 1. Validaciones de negocio (dentro de la transacción)
                
                // 1.1 Validar rol
                Optional<RolEntity> rolEntityOpt = repositorioRol.buscarPorNombreRol(nombreRol.toLowerCase());
                if (rolEntityOpt.isEmpty()) {
                    em.getTransaction().rollback();
                    return ResultadoOperacion.error("El rol '" + nombreRol + "' no existe en el sistema");
                }
                
                // 1.2 Validar duplicados
                if (repositorioUsuario.existePorCorreo(usuario.getCorreoElectronico())) {
                    em.getTransaction().rollback();
                    return ResultadoOperacion.error("Ya existe un usuario con ese correo electrónico");
                }
                
                if (repositorioUsuario.existePorTelefono(usuario.getTelefono())) {
                    em.getTransaction().rollback();
                    return ResultadoOperacion.error("Ya existe un usuario con ese número de teléfono");
                }
                
                // 2. Crear entidad de usuario según el tipo
                UsuarioEntity usuarioEntity = null;
                TokenUsuarioEntity tokenEntity = null;
                String tipoUsuario = usuario.getClass().getSimpleName();

                switch (tipoUsuario) {
                    case "Profesor":
                        Profesor profesor = (Profesor) usuario;
                        ProfesorEntity profesorEntity = DominioAPersistenciaMapper.toEntity(profesor);
                        
                        // 3. Generar y persistir token
                        tokenEntity = tokenService.generarYPersistirToken(profesorEntity, nombreRol);
                        
                        // 4. Persistir usuario (ya con el token asignado)
                        repositorioUsuario.guardar(profesorEntity);
                        
                        usuarioEntity = profesorEntity;
                        usuario.setIdUsuario(profesorEntity.getIdUsuario());
                        break;
                        
                    case "Directivo":
                        Directivo directivo = (Directivo) usuario;
                        DirectivoEntity directivoEntity = DominioAPersistenciaMapper.toEntity(directivo);
                        
                        // 3. Generar y persistir token
                        tokenEntity = tokenService.generarYPersistirToken(directivoEntity, nombreRol);
                        
                        // 4. Persistir usuario (ya con el token asignado)
                        repositorioUsuario.guardar(directivoEntity);
                        
                        usuarioEntity = directivoEntity;
                        usuario.setIdUsuario(directivoEntity.getIdUsuario());
                        break;
                        
                    case "Acudiente":
                        Acudiente acudiente = (Acudiente) usuario;
                        AcudienteEntity acudienteEntity = DominioAPersistenciaMapper.toEntity(acudiente);
                        
                        // 3. Generar y persistir token
                        tokenEntity = tokenService.generarYPersistirToken(acudienteEntity, nombreRol);
                        
                        // 4. Persistir usuario (ya con el token asignado)
                        repositorioUsuario.guardar(acudienteEntity);
                        
                        usuarioEntity = acudienteEntity;
                        usuario.setIdUsuario(acudienteEntity.getIdUsuario());
                        break;
                        
                    case "Administrador":
                        Administrador administrador = (Administrador) usuario;
                        AdministradorEntity administradorEntity = DominioAPersistenciaMapper.toEntity(administrador);
                        
                        // 3. Generar y persistir token
                        tokenEntity = tokenService.generarYPersistirToken(administradorEntity, nombreRol);
                        
                        // 4. Persistir usuario (ya con el token asignado)
                        repositorioUsuario.guardar(administradorEntity);
                        
                        usuarioEntity = administradorEntity;
                        usuario.setIdUsuario(administradorEntity.getIdUsuario());
                        break;
                        
                    default:
                        em.getTransaction().rollback();
                        return ResultadoOperacion.error("Tipo de usuario no soportado: " + tipoUsuario);
                }

                // 5. CONFIRMAR TRANSACCIÓN
                em.getTransaction().commit();
                
                // 6. Enviar credenciales por email (fuera de transacción)
                if (tokenEntity != null && usuarioEntity != null) {
                    tokenService.enviarCredencialesPorEmail(usuarioEntity, tokenEntity);
                }
                
                // 7. Devolver usuario con token en dominio
                if (tokenEntity != null) {
                    TokenUsuario token = DominioAPersistenciaMapper.toDomain(tokenEntity);
                    usuario.setTokenAccess(token);
                }
                
                return ResultadoOperacion.exito("Usuario creado exitosamente", usuario);
                
            } catch (Exception e) {
                // Rollback si hay error en la lógica de negocio
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al crear usuario: " + e.getMessage());
        }
    }
    /**
     * CU 2.4 - Consultar información de usuario (solo lectura)
     */
    public ResultadoOperacion consultarUsuario(Integer usuarioId) {
        try {
            if (usuarioId == null) {
                return ResultadoOperacion.error("ID de usuario no válido");
            }
            
            Optional<UsuarioEntity> usuarioEntityOpt = repositorioUsuario.buscarPorId(usuarioId);
            
            if (usuarioEntityOpt.isEmpty()) {
                return ResultadoOperacion.error("Usuario no encontrado");
            }
            
            Usuario usuario = mapearEntidadADominio(usuarioEntityOpt.get());
            return ResultadoOperacion.exito("Consulta exitosa", usuario);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoOperacion.error("Error al consultar usuario: " + e.getMessage());
        }
    }

    /**
     * CU 2.4 - Consultar información del usuario autenticado
     */
    public ResultadoOperacion consultarMiInformacion(Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            return ResultadoOperacion.error("Usuario no autenticado");
        }
        
        if (usuarioAutenticado.getIdUsuario() == null) {
            return ResultadoOperacion.error("ID de usuario no válido");
        }
        
        return consultarUsuario(usuarioAutenticado.getIdUsuario());
    }
    
    /**
     * Método para cerrar el EntityManager cuando ya no se necesite
     * Útil para el constructor por defecto
     */
    public void cerrar() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    private Usuario mapearEntidadADominio(UsuarioEntity usuarioEntity) {
        if (usuarioEntity == null) {
            return null;
        }
        
        try {
            if (usuarioEntity instanceof ProfesorEntity) {
                return DominioAPersistenciaMapper.toDomainComplete((ProfesorEntity) usuarioEntity);
            } else if (usuarioEntity instanceof DirectivoEntity) {
                return DominioAPersistenciaMapper.toDomain((DirectivoEntity) usuarioEntity);
            } else if (usuarioEntity instanceof AdministradorEntity) {
                return DominioAPersistenciaMapper.toDomain((AdministradorEntity) usuarioEntity);
            } else if (usuarioEntity instanceof AcudienteEntity) {
                return DominioAPersistenciaMapper.toDomainComplete((AcudienteEntity) usuarioEntity);
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

