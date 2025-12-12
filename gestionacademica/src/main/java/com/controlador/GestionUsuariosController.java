package com.controlador;

import com.modelo.dominio.*;
import com.modelo.dtos.UsuarioDTO;
import com.modelo.persistencia.repositorios.*;
import com.aplicacion.JPAUtil;
import com.modelo.ServicioCorreo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Optional;

public class GestionUsuariosController {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final EntityManager entityManager;
    
    public GestionUsuariosController() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.usuarioRepositorio = new UsuarioRepositorio(entityManager);
        this.rolRepositorio = new RolRepositorio(entityManager);
    }
    
    /**
     * CU 2.2 - Crear usuario SIMPLIFICADO
     * Ahora el dominio maneja su propia creación Y envía correo
     */
    public ResultadoOperacion crearUsuario(UsuarioDTO datos) {
        if (datos == null) {
            return ResultadoOperacion.error("Los datos del usuario son obligatorios");
        }
        
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 1. Buscar rol (validación temprana)
            Optional<Rol> rolOpt = rolRepositorio.buscarPorNombreRol(datos.nombreRol);
            if (rolOpt.isEmpty()) {
                return ResultadoOperacion.errorValidacion("rol", 
                    "El rol especificado no existe");
            }
            
            Rol rol = rolOpt.get();
            
            // 2. Crear usuario del tipo correcto (el dominio decide el tipo)
            Usuario usuario = crearInstanciaSegunRol(datos);
            
            // 3. Validar duplicados ANTES de intentar crear
            ResultadoOperacion validacionDuplicados = validarDuplicados(datos);
            if (!validacionDuplicados.isExitoso()) {
                return validacionDuplicados;
            }
            
            // 4. DELEGAR AL DOMINIO la creación completa
            ResultadoValidacionDominio creacion = usuario.crearUsuarioCompleto(rol);
            if (!creacion.isValido()) {
                return ResultadoOperacion.errorValidacion(
                    creacion.getCampoInvalido(),
                    creacion.getMensajeError()
                );
            }
            
            // 5. Guardar (el token ya está generado y asignado por el dominio)
            usuarioRepositorio.guardar(usuario);
            
            // 6. Refrescar para obtener IDs generados
            entityManager.flush();
            entityManager.refresh(usuario);
            
            transaction.commit();
            
            // 7. ENVIAR CORREO CON CREDENCIALES
            boolean correoEnviado = false;
            if (usuario.getTokenAccess() != null) {
                correoEnviado = ServicioCorreo.enviarCredenciales(
                    usuario.getCorreoElectronico(),
                    usuario.obtenerNombreCompleto(),
                    usuario.getTokenAccess().getNombreUsuario(),
                    usuario.getTokenAccess().getContrasena(),
                    datos.nombreRol
                );
            }
            
            // 8. Preparar mensaje de resultado
            String mensajeExito = "Usuario creado exitosamente";
            if (correoEnviado) {
                mensajeExito += "\n✓ Credenciales enviadas por correo electrónico";
            } else if (usuario.getTokenAccess() != null) {
                mensajeExito += "\n⚠ Advertencia: No se pudo enviar el correo con las credenciales";
            }
            
            return ResultadoOperacion.exitoConDatos(mensajeExito, usuario);
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return ResultadoOperacion.error(
                "Error al crear el usuario: " + e.getMessage()
            );
        }
    }
    
    /**
     * Método auxiliar para crear instancia según rol
     */
    private Usuario crearInstanciaSegunRol(UsuarioDTO datos) {
        Usuario usuario;
        
        switch (datos.nombreRol.toLowerCase()) {
            case "profesor":
                usuario = new Profesor();
                break;
            case "directivo":
                usuario = new Directivo();
                break;
            default:
                throw new IllegalArgumentException(
                    "Rol no soportado: " + datos.nombreRol);
        }
        
        // Mapear datos comunes
        usuario.setNuipUsuario(datos.nuip);
        usuario.setPrimerNombre(datos.primerNombre);
        usuario.setSegundoNombre(datos.segundoNombre);
        usuario.setPrimerApellido(datos.primerApellido);
        usuario.setSegundoApellido(datos.segundoApellido);
        usuario.setEdad(datos.edad);
        usuario.setCorreoElectronico(datos.correoElectronico);
        usuario.setTelefono(datos.telefono);
        
        return usuario;
    }
    
    /**
     * Validación de duplicados separada
     */
    private ResultadoOperacion validarDuplicados(UsuarioDTO datos) {
        try {
            if (usuarioRepositorio.existePorNuip(datos.nuip)) {
                return ResultadoOperacion.errorValidacion("nuip",
                    "Ya existe un usuario registrado con este NUIP");
            }
            
            if (usuarioRepositorio.existePorCorreo(datos.correoElectronico)) {
                return ResultadoOperacion.errorValidacion("correoElectronico",
                    "Ya existe un usuario registrado con este correo electrónico");
            }
            
            if (usuarioRepositorio.existePorTelefono(datos.telefono)) {
                return ResultadoOperacion.errorValidacion("telefono",
                    "Ya existe un usuario registrado con este teléfono");
            }
            
            return ResultadoOperacion.exito("Sin duplicados");
            
        } catch (Exception e) {
            return ResultadoOperacion.error(
                "Error al verificar duplicados: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validación previa (para UI)
     */
    public ResultadoOperacion validarDatosUsuario(UsuarioDTO datos) {
        // Validación básica de estructura
        if (datos.nombreRol == null || datos.nombreRol.trim().isEmpty()) {
            return ResultadoOperacion.errorValidacion("rol", "El rol es obligatorio");
        }
        
        // Validar rol existe
        Optional<Rol> rolOpt = rolRepositorio.buscarPorNombreRol(datos.nombreRol);
        if (rolOpt.isEmpty()) {
            return ResultadoOperacion.errorValidacion("rol", "El rol especificado no existe");
        }
        
        // Crear instancia temporal para validación del dominio
        Usuario usuario = crearInstanciaSegunRol(datos);
        
        // Delegar validación al dominio
        ResultadoValidacionDominio validacion = usuario.validarDatosBasicos();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // Validar duplicados (sin guardar)
        return validarDuplicados(datos);
    }

    /**
     * Obtiene los roles disponibles para asignar
     */
    public ResultadoOperacion obtenerRolesDisponibles() {
        try {
            var roles = rolRepositorio.buscarTodos();
            return ResultadoOperacion.exitoConDatos(
                "Roles obtenidos correctamente",
                roles
            );
        } catch (Exception e) {
            return ResultadoOperacion.error(
                "Error al obtener los roles: " + e.getMessage()
            );
        }
    }

    /**
     * CU 2.4 - Consultar mi información
     * Obtiene la información completa del usuario autenticado
     */
    public ResultadoOperacion consultarMiInformacion(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            return ResultadoOperacion.error("Usuario no válido");
        }
        
        try {
            // Buscar usuario actualizado en BD para asegurar datos frescos
            Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorId(usuario.getIdUsuario());
            
            if (usuarioOpt.isEmpty()) {
                return ResultadoOperacion.error(
                    "No se pudo acceder a la información del usuario");
            }
            
            return ResultadoOperacion.exitoConDatos(
                "Información obtenida correctamente",
                usuarioOpt.get()
            );
            
        } catch (Exception e) {
            return ResultadoOperacion.error(
                "Error al consultar la información: " + e.getMessage()
            );
        }
    }
}