package com.controlador;

import com.modelo.dominio.*;
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
    public ResultadoOperacion crearUsuario(
            String nuip,
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            Integer edad,
            String correoElectronico,
            String telefono,
            String nombreRol) {
        
        if (nombreRol == null || nombreRol.trim().isEmpty()) {
            return ResultadoOperacion.error("El rol es obligatorio");
        }
        
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // 1. Buscar rol (validación temprana)
            Optional<Rol> rolOpt = rolRepositorio.buscarPorNombreRol(nombreRol);
            if (rolOpt.isEmpty()) {
                return ResultadoOperacion.errorValidacion("rol", 
                    "El rol especificado no existe");
            }
            
            Rol rol = rolOpt.get();
            
            // 2. Validar duplicados ANTES de intentar crear
            ResultadoOperacion validacionDuplicados = validarDuplicados(
                nuip, correoElectronico, telefono);
            if (!validacionDuplicados.isExitoso()) {
                return validacionDuplicados;
            }
            
            // 3. Crear usuario del tipo correcto (el dominio decide el tipo)
            Usuario usuario = crearInstanciaSegunRol(
                nuip, primerNombre, segundoNombre, primerApellido, 
                segundoApellido, edad, correoElectronico, telefono, nombreRol);
            
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
                    nombreRol
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
    private Usuario crearInstanciaSegunRol(
            String nuip,
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            Integer edad,
            String correoElectronico,
            String telefono,
            String nombreRol) {
        
        Usuario usuario;
        
        switch (nombreRol.toLowerCase()) {
            case "profesor":
                usuario = new Profesor();
                break;
            case "directivo":
                usuario = new Directivo();
                break;
            default:
                throw new IllegalArgumentException(
                    "Rol no soportado: " + nombreRol);
        }
        
        // Mapear datos comunes
        usuario.setNuipUsuario(nuip);
        usuario.setPrimerNombre(primerNombre);
        usuario.setSegundoNombre(segundoNombre);
        usuario.setPrimerApellido(primerApellido);
        usuario.setSegundoApellido(segundoApellido);
        usuario.setEdad(edad);
        usuario.setCorreoElectronico(correoElectronico);
        usuario.setTelefono(telefono);
        
        return usuario;
    }
    
    /**
     * Validación de duplicados separada
     */
    private ResultadoOperacion validarDuplicados(
            String nuip,
            String correoElectronico,
            String telefono) {
        
        try {
            if (usuarioRepositorio.existePorNuip(nuip)) {
                return ResultadoOperacion.errorValidacion("nuip",
                    "Ya existe un usuario registrado con este NUIP");
            }
            
            if (usuarioRepositorio.existePorCorreo(correoElectronico)) {
                return ResultadoOperacion.errorValidacion("correoElectronico",
                    "Ya existe un usuario registrado con este correo electrónico");
            }
            
            if (usuarioRepositorio.existePorTelefono(telefono)) {
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
    public ResultadoOperacion validarDatosUsuario(
            String nuip,
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            Integer edad,
            String correoElectronico,
            String telefono,
            String nombreRol) {
        
        // Validación básica de estructura
        if (nombreRol == null || nombreRol.trim().isEmpty()) {
            return ResultadoOperacion.errorValidacion("rol", "El rol es obligatorio");
        }
        
        // Validar rol existe
        Optional<Rol> rolOpt = rolRepositorio.buscarPorNombreRol(nombreRol);
        if (rolOpt.isEmpty()) {
            return ResultadoOperacion.errorValidacion("rol", "El rol especificado no existe");
        }
        
        // Crear instancia temporal para validación del dominio
        Usuario usuario = crearInstanciaSegunRol(
            nuip, primerNombre, segundoNombre, primerApellido,
            segundoApellido, edad, correoElectronico, telefono, nombreRol);
        
        // Delegar validación al dominio
        ResultadoValidacionDominio validacion = usuario.validarDatosBasicos();
        
        if (!validacion.isValido()) {
            return ResultadoOperacion.errorValidacion(
                validacion.getCampoInvalido(),
                validacion.getMensajeError()
            );
        }
        
        // Validar duplicados (sin guardar)
        return validarDuplicados(nuip, correoElectronico, telefono);
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