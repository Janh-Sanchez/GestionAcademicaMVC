package com.controlador;

import com.modelo.dominio.*;
import com.modelo.dtos.UsuarioDTO;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Optional;

/**
 * Controlador para la gestión de usuarios (CU 2.1, 2.2, 2.4)
 * Coordina entre la Vista y el Modelo según patrón MVC
 */
public class GestionUsuariosController {
    private final RepositorioGenerico<Usuario> repoUsuario;
    private final RepositorioGenerico<TokenUsuario> repoToken;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final EntityManager entityManager;
    
    public GestionUsuariosController(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.repoUsuario = new RepositorioGenerico<>(entityManager, Usuario.class);
        this.repoToken = new RepositorioGenerico<>(entityManager, TokenUsuario.class);
        this.usuarioRepositorio = new UsuarioRepositorio(entityManager);
        this.rolRepositorio = new RolRepositorio(entityManager);
    }
    
    /**
     * CU 2.2 - Crear usuario
     * Valida y registra un nuevo usuario en el sistema
     */
    public ResultadoOperacion crearUsuario(UsuarioDTO datos) {
        // 1. Validación básica de entrada
        if (datos == null) {
            return ResultadoOperacion.error("Los datos del usuario son obligatorios");
        }
        
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 2. Crear instancia del usuario según tipo
            Usuario usuario = construirUsuarioSegunTipo(datos);
            
            // 3. Validar datos básicos (delegado al modelo)
            ResultadoValidacionDominio validacion = usuario.validarDatosBasicos();
            if (!validacion.isValido()) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion(
                    validacion.getCampoInvalido(),
                    validacion.getMensajeError()
                );
            }
            
            // 4. Verificar duplicados (usando repositorios)
            if (usuarioRepositorio.existePorNuip(datos.nuip)) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion("nuip",
                    "Ya existe un usuario registrado con este NUIP");
            }
            
            if (usuarioRepositorio.existePorCorreo(datos.correoElectronico)) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion("correoElectronico",
                    "Ya existe un usuario registrado con este correo electrónico");
            }
            
            if (usuarioRepositorio.existePorTelefono(datos.telefono)) {
                transaction.rollback();
                return ResultadoOperacion.errorValidacion("telefono",
                    "Ya existe un usuario registrado con este teléfono");
            }
            
            // 5. Crear token de acceso automáticamente
            if (datos.nombreRol != null && !datos.nombreRol.trim().isEmpty()) {
                ResultadoOperacion resultadoToken = crearTokenUsuario(datos);
                if (!resultadoToken.isExitoso()) {
                    transaction.rollback();
                    return resultadoToken;
                }
                TokenUsuario token = (TokenUsuario) resultadoToken.getDatos();
                usuario.setTokenAccess(token);
            }
            
            // 6. Guardar usuario
            repoUsuario.guardar(usuario);
            entityManager.refresh(usuario);
            
            transaction.commit();
            
            return ResultadoOperacion.exitoConDatos(
                "Usuario creado exitosamente",
                usuario
            );
            
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
    
    /**
     * Validación previa de datos de usuario
     * Permite validar antes de intentar guardar
     */
    public ResultadoOperacion validarDatosUsuario(UsuarioDTO datos) {
        // Crear instancia temporal para validar
        Usuario usuario = construirUsuarioSegunTipo(datos);
        
        // Delegar validación al modelo
        ResultadoValidacionDominio validacion = usuario.validarDatosBasicos();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // Verificar duplicados (sin transacción)
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
        } catch (Exception e) {
            return ResultadoOperacion.error(
                "Error al verificar duplicados: " + e.getMessage()
            );
        }
        
        return ResultadoOperacion.exito("Datos válidos");
    }
    
    /**
     * Crea un token de usuario con validaciones
     */
    private ResultadoOperacion crearTokenUsuario(UsuarioDTO datos) {        
        // Buscar el rol
        Optional<Rol> rolOpt = rolRepositorio.buscarPorNombreRol(datos.nombreRol);
        if (rolOpt.isEmpty()) {
            return ResultadoOperacion.errorValidacion("rol",
                "El rol especificado no existe");
        }
        
        Rol rol = rolOpt.get();
        
        // Validar que el rol sea válido
        if (!rol.esValido()) {
            return ResultadoOperacion.error(
                "El rol seleccionado no es válido o no tiene permisos asignados");
        }
        
        // Crear token NUEVO cada vez
        TokenUsuario token = TokenUsuario.generarTokenDesdeUsuario(
            datos.primerNombre,
            datos.segundoNombre, 
            datos.primerApellido, 
            datos.segundoApellido, 
            rol
        );
        
        repoToken.guardar(token);
        
        return ResultadoOperacion.exitoConDatos("Token creado", token);
    }
    
    private Usuario construirUsuarioSegunTipo(UsuarioDTO datos) {
        Usuario usuario;
        
        switch (datos.nombreRol.toLowerCase()) {
            case "directivo":
                usuario = new Directivo();
                break;
            case "profesor":
                usuario = new Profesor();
                break;
            default:
                throw new IllegalArgumentException(
                    "Tipo de usuario no válido. Solo se permiten: Profesor, Directivo");
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
}