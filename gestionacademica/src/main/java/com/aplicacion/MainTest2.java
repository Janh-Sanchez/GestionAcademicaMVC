package com.aplicacion;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.*;

public class MainTest2 {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO CARGA DE DATOS DE PRUEBA (GRADO ÚNICO) ===\n");
        
        EntityManager em = null;
        try {
            em = JPAUtil.getEntityManagerFactory().createEntityManager();
            
            // 1. Limpiar datos existentes
            limpiarDatos(em);
            
            // 2. Crear permisos y roles
            crearPermisosYRoles(em);
            
            // 3. Crear grados (solo 3: Párvulos, Caminadores, Pre-Jardín)
            crearGrados(em);
            
            // 4. Crear administrador del sistema
            crearAdministrador(em);
            
            // 5. Crear directivo
            crearDirectivo(em);
            
            // 6. Crear profesor
            crearProfesor(em);
            
            // 7. Crear acudiente ya aprobado (con token)
            Acudiente acudienteAprobado = crearAcudienteAprobado(em);
            
            // 8. Crear acudientes pendientes (SIN estudiantes aprobados - para pruebas manuales)
            List<Acudiente> acudientesPendientes = crearAcudientesPendientes(em);
            
            // 9. Crear estudiantes PENDIENTES todos para Párvulos
            List<Estudiante> estudiantesPendientes = crearEstudiantesPendientesPárvulos(em, acudientesPendientes);
            
            // 10. Crear preinscripciones PENDIENTES
            crearPreinscripcionesPendientes(em, acudientesPendientes, estudiantesPendientes);
            
            // 11. Crear grupos vacíos para asignación futura
            crearGruposVacios(em);
            
            // 12. Mostrar resumen
            mostrarResumen(em);
            
            System.out.println("\n=== DATOS DE PRUEBA CARGADOS EXITOSAMENTE ===");
            System.out.println("\n⚠️  TODOS LOS ESTUDIANTES SON DE PÁRVULOS (para pruebas específicas)");
            System.out.println("\nAhora puedes usar la interfaz para:");
            System.out.println("  • Aprobar/Rechazar aspirantes manualmente");
            System.out.println("  • Ver cómo se generan los tokens automáticamente");
            System.out.println("  • Probar el flujo completo de preinscripción");
            System.out.println("  • Probar la asignación de cupos por grado específico");
            
        } catch (Exception e) {
            System.err.println("Error cargando datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    private static void limpiarDatos(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Limpiando datos existentes...");
            
            // Eliminar en orden correcto para evitar violaciones de FK
            em.createNativeQuery("DELETE FROM rol_permiso").executeUpdate();
            em.createQuery("DELETE FROM Estudiante").executeUpdate();
            em.createQuery("DELETE FROM Preinscripcion").executeUpdate();
            em.createQuery("DELETE FROM Acudiente").executeUpdate();
            em.createQuery("DELETE FROM grupo").executeUpdate();
            em.createQuery("DELETE FROM Profesor").executeUpdate();
            em.createQuery("DELETE FROM Directivo").executeUpdate();
            em.createQuery("DELETE FROM Administrador").executeUpdate();
            em.createQuery("DELETE FROM token_usuario").executeUpdate();
            em.createQuery("DELETE FROM Usuario").executeUpdate();
            em.createQuery("DELETE FROM grado").executeUpdate();
            em.createQuery("DELETE FROM Rol").executeUpdate();
            em.createQuery("DELETE FROM Permiso").executeUpdate();
            
            tx.commit();
            System.out.println("✓ Datos limpiados correctamente\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("ℹ No había datos existentes para limpiar\n");
        }
    }
    
    private static void crearPermisosYRoles(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando permisos y roles del sistema...");
            
            // Crear permisos
            Permiso gestionarUsuarios = new Permiso("GESTIONAR_USUARIOS", 
                "Permite crear, modificar y eliminar usuarios");
            Permiso aprobarAspirantes = new Permiso("APROBAR_ASPIRANTES", 
                "Permite aprobar o rechazar aspirantes");
            Permiso configurarSistema = new Permiso("CONFIGURAR_SISTEMA", 
                "Permite configurar parámetros del sistema");
            Permiso asignarProfesores = new Permiso("ASIGNAR_PROFESORES", 
                "Permite asignar profesores a grupos");
            Permiso gestionarGrupos = new Permiso("GESTIONAR_GRUPOS", 
                "Permite gestionar grupos");
            Permiso calificarEstudiantes = new Permiso("CALIFICAR_ESTUDIANTES", 
                "Permite calificar estudiantes");
            Permiso consultarEstudiantes = new Permiso("CONSULTAR_ESTUDIANTES", 
                "Permite consultar información de estudiantes");
            Permiso verBoletines = new Permiso("VER_BOLETINES", 
                "Permite ver boletines académicos");
            
            em.persist(gestionarUsuarios);
            em.persist(aprobarAspirantes);
            em.persist(configurarSistema);
            em.persist(asignarProfesores);
            em.persist(gestionarGrupos);
            em.persist(calificarEstudiantes);
            em.persist(consultarEstudiantes);
            em.persist(verBoletines);
            
            // Crear rol Administrador
            Rol rolAdmin = new Rol("administrador");
            Set<Permiso> permisosAdmin = new HashSet<>();
            permisosAdmin.add(gestionarUsuarios);
            permisosAdmin.add(aprobarAspirantes);
            permisosAdmin.add(configurarSistema);
            permisosAdmin.add(asignarProfesores);
            permisosAdmin.add(gestionarGrupos);
            rolAdmin.setPermisos(permisosAdmin);
            em.persist(rolAdmin);
            
            // Crear rol Directivo
            Rol rolDirectivo = new Rol("directivo");
            Set<Permiso> permisosDirectivo = new HashSet<>();
            permisosDirectivo.add(aprobarAspirantes);
            permisosDirectivo.add(asignarProfesores);
            permisosDirectivo.add(gestionarGrupos);
            rolDirectivo.setPermisos(permisosDirectivo);
            em.persist(rolDirectivo);
            
            // Crear rol Profesor
            Rol rolProfesor = new Rol("profesor");
            Set<Permiso> permisosProfesor = new HashSet<>();
            permisosProfesor.add(gestionarGrupos);
            permisosProfesor.add(calificarEstudiantes);
            rolProfesor.setPermisos(permisosProfesor);
            em.persist(rolProfesor);
            
            // Crear rol Acudiente
            Rol rolAcudiente = new Rol("acudiente");
            Set<Permiso> permisosAcudiente = new HashSet<>();
            permisosAcudiente.add(consultarEstudiantes);
            permisosAcudiente.add(verBoletines);
            rolAcudiente.setPermisos(permisosAcudiente);
            em.persist(rolAcudiente);
            
            tx.commit();
            System.out.println("✓ Permisos y roles creados (administrador, directivo, profesor, acudiente)\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearGrados(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando grados disponibles...");
            
            // Solo 3 grados según tu BD
            String[] nombresGrados = {"Párvulos", "Caminadores", "Pre-Jardín"};
            
            for (String nombre : nombresGrados) {
                Grado grado = new Grado();
                grado.setNombreGrado(nombre);
                em.persist(grado);
                System.out.println("  - " + nombre);
            }
            
            tx.commit();
            System.out.println("✓ 3 grados creados exitosamente\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearAdministrador(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando administrador del sistema...");
            
            RolRepositorio rolRepo = new RolRepositorio(em);
            Rol rolAdmin = rolRepo.buscarPorNombreRol("administrador")
                .orElseThrow(() -> new RuntimeException("Rol administrador no encontrado"));
            
            Administrador admin = new Administrador();
            admin.setPrimerNombre("Carlos");
            admin.setSegundoNombre("Andrés");
            admin.setPrimerApellido("Rojas");
            admin.setSegundoApellido("Pérez");
            admin.setEdad(35);
            admin.setNuipUsuario("0123456789");
            admin.setCorreoElectronico("admin@colegio.edu");
            admin.setTelefono("3001234567");
            
            TokenUsuario tokenAdmin = TokenUsuario.generarTokenDesdeUsuario(
                admin.getPrimerNombre(),
                admin.getSegundoNombre(),
                admin.getPrimerApellido(),
                admin.getSegundoApellido(),
                rolAdmin
            );
            
            // Forzar usuario y contraseña específicos
            tokenAdmin.setNombreUsuario("admin");
            tokenAdmin.setContrasena("abc123");
            
            em.persist(tokenAdmin);
            admin.setTokenAccess(tokenAdmin);
            em.persist(admin);
            
            tx.commit();
            System.out.println("✓ Administrador creado:");
            System.out.println("  - Nombre: " + admin.obtenerNombreCompleto());
            System.out.println("  - Usuario: admin");
            System.out.println("  - Contraseña: abc123\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearDirectivo(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando directivo...");
            
            RolRepositorio rolRepo = new RolRepositorio(em);
            Rol rolDirectivo = rolRepo.buscarPorNombreRol("directivo")
                .orElseThrow(() -> new RuntimeException("Rol directivo no encontrado"));
            
            Directivo directivo = new Directivo();
            directivo.setPrimerNombre("Juan");
            directivo.setSegundoNombre(null);
            directivo.setPrimerApellido("Auxiliar");
            directivo.setSegundoApellido("Gómez");
            directivo.setEdad(40);
            directivo.setNuipUsuario("1230456789");
            directivo.setCorreoElectronico("juan_aux@colegio.edu");
            directivo.setTelefono("3007654321");
            
            TokenUsuario tokenDirectivo = TokenUsuario.generarTokenDesdeUsuario(
                directivo.getPrimerNombre(),
                directivo.getSegundoNombre(),
                directivo.getPrimerApellido(),
                directivo.getSegundoApellido(),
                rolDirectivo
            );
            
            // Forzar usuario y contraseña específicos
            tokenDirectivo.setNombreUsuario("juan_aux");
            tokenDirectivo.setContrasena("pass456");
            
            em.persist(tokenDirectivo);
            directivo.setTokenAccess(tokenDirectivo);
            em.persist(directivo);
            
            tx.commit();
            System.out.println("✓ Directivo creado:");
            System.out.println("  - Nombre: " + directivo.obtenerNombreCompleto());
            System.out.println("  - Usuario: juan_aux");
            System.out.println("  - Contraseña: pass456\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearProfesor(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando profesor...");
            
            RolRepositorio rolRepo = new RolRepositorio(em);
            Rol rolProfesor = rolRepo.buscarPorNombreRol("profesor")
                .orElseThrow(() -> new RuntimeException("Rol profesor no encontrado"));
            
            Profesor profesor = new Profesor();
            profesor.setPrimerNombre("María");
            profesor.setSegundoNombre("Elena");
            profesor.setPrimerApellido("Lectora");
            profesor.setSegundoApellido("Ramírez");
            profesor.setEdad(29);
            profesor.setNuipUsuario("2013456789");
            profesor.setCorreoElectronico("maria_lect@colegio.edu");
            profesor.setTelefono("3019876543");
            
            TokenUsuario tokenProfesor = TokenUsuario.generarTokenDesdeUsuario(
                profesor.getPrimerNombre(),
                profesor.getSegundoNombre(),
                profesor.getPrimerApellido(),
                profesor.getSegundoApellido(),
                rolProfesor
            );
            
            // Forzar usuario y contraseña específicos
            tokenProfesor.setNombreUsuario("maria_lect");
            tokenProfesor.setContrasena("xyz789");
            
            em.persist(tokenProfesor);
            profesor.setTokenAccess(tokenProfesor);
            em.persist(profesor);
            
            tx.commit();
            System.out.println("✓ Profesor creado:");
            System.out.println("  - Nombre: " + profesor.obtenerNombreCompleto());
            System.out.println("  - Usuario: maria_lect");
            System.out.println("  - Contraseña: xyz789\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static Acudiente crearAcudienteAprobado(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            System.out.println("Creando acudiente YA APROBADO (con token)...");
            
            RolRepositorio rolRepo = new RolRepositorio(em);
            Rol rolAcudiente = rolRepo.buscarPorNombreRol("acudiente")
                .orElseThrow(() -> new RuntimeException("Rol acudiente no encontrado"));
            
            Acudiente acudiente = new Acudiente();
            acudiente.setPrimerNombre("Laura");
            acudiente.setSegundoNombre(null);
            acudiente.setPrimerApellido("Martínez");
            acudiente.setSegundoApellido("Santos");
            acudiente.setEdad(38);
            acudiente.setNuipUsuario("3012456789");
            acudiente.setCorreoElectronico("laura_acu@colegio.edu");
            acudiente.setTelefono("3021112233");
            acudiente.setEstadoAprobacion(Estado.Aprobada);
            
            TokenUsuario tokenAcudiente = TokenUsuario.generarTokenDesdeUsuario(
                acudiente.getPrimerNombre(),
                acudiente.getSegundoNombre(),
                acudiente.getPrimerApellido(),
                acudiente.getSegundoApellido(),
                rolAcudiente
            );
            
            // Forzar usuario y contraseña específicos
            tokenAcudiente.setNombreUsuario("laura_acu");
            tokenAcudiente.setContrasena("acu123");
            
            em.persist(tokenAcudiente);
            acudiente.setTokenAccess(tokenAcudiente);
            
            ResultadoValidacionDominio validacion = acudiente.validar();
            if (!validacion.isValido()) {
                throw new RuntimeException("Error validando acudiente: " + validacion.getMensajeError());
            }
            
            em.persist(acudiente);
            
            tx.commit();
            System.out.println("✓ Acudiente APROBADO creado:");
            System.out.println("  - Nombre: " + acudiente.obtenerNombreCompleto());
            System.out.println("  - Usuario: laura_acu");
            System.out.println("  - Contraseña: acu123");
            System.out.println("  - Estado: APROBADA\n");
            
            return acudiente;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Acudiente> crearAcudientesPendientes(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        List<Acudiente> acudientes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando acudientes PENDIENTES (para pruebas manuales)...");
            
            // 8 acudientes PENDIENTES con datos únicos
            Object[][] datosAcudientes = {
                {"Roberto", "Antonio", "Jiménez", "Vargas", 41, "4567890123", "roberto.jimenez@email.com", "3145678901"},
                {"Patricia", "Lucía", "Castro", "Mendoza", 36, "5678901234", "patricia.castro@email.com", "3156789012"},
                {"Fernando", "José", "Navarro", "Ríos", 44, "6789012345", "fernando.navarro@email.com", "3167890123"},
                {"Carmen", "Elena", "Morales", "Castaño", 33, "7890123456", "carmen.morales@email.com", "3178901234"},
                {"Javier", "Andrés", "Ortiz", "Vega", 39, "8901234567", "javier.ortiz@email.com", "3189012345"},
                {"Claudia", "Beatriz", "Silva", "Torres", 37, "9012345678", "claudia.silva@email.com", "3190123456"},
                {"Ricardo", "Luis", "Paredes", "Suárez", 42, "0123456780", "ricardo.paredes@email.com", "3201234567"},
                {"Sandra", "Milena", "Gutiérrez", "Molina", 35, "1234567801", "sandra.gutierrez@email.com", "3212345678"}
            };
            
            int contador = 1;
            for (Object[] datos : datosAcudientes) {
                Acudiente acudiente = new Acudiente();
                acudiente.setPrimerNombre((String) datos[0]);
                acudiente.setSegundoNombre((String) datos[1]);
                acudiente.setPrimerApellido((String) datos[2]);
                acudiente.setSegundoApellido((String) datos[3]);
                acudiente.setEdad((Integer) datos[4]);
                acudiente.setNuipUsuario((String) datos[5]);
                acudiente.setCorreoElectronico((String) datos[6]);
                acudiente.setTelefono((String) datos[7]);
                acudiente.setEstadoAprobacion(Estado.Pendiente); // TODOS PENDIENTES
                
                ResultadoValidacionDominio validacion = acudiente.validar();
                if (!validacion.isValido()) {
                    throw new RuntimeException("Error validando acudiente: " + validacion.getMensajeError());
                }
                
                em.persist(acudiente);
                acudientes.add(acudiente);
                
                System.out.println("  " + contador + ". " + acudiente.obtenerNombreCompleto() + 
                    " (Estado: PENDIENTE - sin token)");
                contador++;
            }
            
            tx.commit();
            System.out.println("✓ " + acudientes.size() + " acudientes PENDIENTES creados\n");
            return acudientes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Estudiante> crearEstudiantesPendientesPárvulos(EntityManager em, List<Acudiente> acudientes) {
        EntityTransaction tx = em.getTransaction();
        List<Estudiante> estudiantes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando estudiantes PENDIENTES TODOS PARA PÁRVULOS...");
            
            GradoRepositorio gradoRepo = new GradoRepositorio(em);
            Grado gradoParvulos = gradoRepo.buscarPornombreGrado("Párvulos")
                .orElseThrow(() -> new RuntimeException("Grado 'Párvulos' no encontrado"));
            
            // Todos los estudiantes para Párvulos (24 estudiantes - TODOS PENDIENTES)
            Object[][] datosEstudiantes = {
                // Primeros 8 estudiantes (originales de Párvulos)
                {"Sofía", "Valentina", "Jiménez", "Torres", 3, "1100000001"},
                {"Mateo", "Alejandro", "Castro", "Ruiz", 3, "1100000002"},
                {"Isabella", "María", "Navarro", "Silva", 4, "1100000003"},
                {"Santiago", "José", "Morales", "Pérez", 3, "1100000004"},
                {"Valentina", "Andrea", "Ortiz", "Gómez", 4, "1100000005"},
                {"Emiliano", "David", "Silva", "López", 3, "1100000006"},
                {"Mariana", "Camila", "Paredes", "Díaz", 4, "1100000007"},
                {"Benjamín", "Andrés", "Gutiérrez", "Vargas", 3, "1100000008"},
                
                // Siguientes 8 estudiantes (originales de Caminadores)
                {"Lucas", "Gabriel", "Jiménez", "Mendoza", 5, "2200000001"},
                {"Emma", "Victoria", "Castro", "Ramírez", 4, "2200000002"},
                {"Sebastián", "Felipe", "Navarro", "Santos", 5, "2200000003"},
                {"Camila", "Alejandra", "Morales", "Rojas", 4, "2200000004"},
                {"Diego", "Nicolás", "Ortiz", "Herrera", 5, "2200000005"},
                {"Valeria", "Antonella", "Silva", "Cruz", 4, "2200000006"},
                {"Nicolás", "Esteban", "Paredes", "Flores", 5, "2200000007"},
                {"Gabriela", "Isabel", "Gutiérrez", "Vega", 4, "2200000008"},
                
                // Últimos 8 estudiantes (originales de Pre-Jardín)
                {"Daniel", "Eduardo", "Jiménez", "Castillo", 6, "3300000001"},
                {"Olivia", "Sofía", "Castro", "Ortega", 5, "3300000002"},
                {"Miguel", "Ángel", "Navarro", "Reyes", 6, "3300000003"},
                {"Paula", "Andrea", "Morales", "Soto", 5, "3300000004"},
                {"Samuel", "Ricardo", "Ortiz", "Muñoz", 6, "3300000005"},
                {"Martina", "Fernanda", "Silva", "Acosta", 5, "3300000006"},
                {"Julián", "Alejandro", "Paredes", "Peña", 6, "3300000007"},
                {"Carolina", "Lucía", "Gutiérrez", "Bravo", 5, "3300000008"}
            };
            
            System.out.println("\n  TODOS LOS ESTUDIANTES PARA PÁRVULOS (TODOS PENDIENTES):");
            
            int indiceAcudiente = 0;
            int contador = 1;
            
            // Crear todos los estudiantes para Párvulos
            for (Object[] datos : datosEstudiantes) {
                Estudiante estudiante = crearEstudiante(
                    datos, gradoParvulos, acudientes.get(indiceAcudiente)
                );
                em.persist(estudiante);
                estudiantes.add(estudiante);
                
                System.out.println("    " + contador + ". " + estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() +
                    " (" + estudiante.getEdad() + " años) - Acudiente: " + 
                    acudientes.get(indiceAcudiente).getPrimerNombre() + " " + 
                    acudientes.get(indiceAcudiente).getPrimerApellido());
                
                indiceAcudiente = (indiceAcudiente + 1) % acudientes.size();
                contador++;
            }
            
            tx.commit();
            System.out.println("\n✓ " + estudiantes.size() + " estudiantes PENDIENTES creados TODOS para Párvulos\n");
            return estudiantes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static Estudiante crearEstudiante(Object[] datos, Grado grado, Acudiente acudiente) {
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre((String) datos[0]);
        estudiante.setSegundoNombre((String) datos[1]);
        estudiante.setPrimerApellido((String) datos[2]);
        estudiante.setSegundoApellido((String) datos[3]);
        estudiante.setEdad((Integer) datos[4]);
        estudiante.setNuip((String) datos[5]);
        estudiante.setEstado(Estado.Pendiente); // TODOS PENDIENTES
        estudiante.setGradoAspira(grado);
        estudiante.setGrupo(null); // Sin grupo hasta ser aprobados
        estudiante.setAcudiente(acudiente);
        
        ResultadoValidacionDominio validacion = estudiante.validar();
        if (!validacion.isValido()) {
            throw new RuntimeException("Error validando estudiante: " + validacion.getMensajeError());
        }
        
        return estudiante;
    }
    
    private static void crearPreinscripcionesPendientes(EntityManager em, 
            List<Acudiente> acudientes, List<Estudiante> estudiantes) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Creando preinscripciones PENDIENTES...");
            
            // Agrupar estudiantes por acudiente
            Map<Acudiente, List<Estudiante>> estudiantesPorAcudiente = new HashMap<>();
            for (Estudiante estudiante : estudiantes) {
                Acudiente acudiente = estudiante.getAcudiente();
                estudiantesPorAcudiente.computeIfAbsent(acudiente, k -> new ArrayList<>()).add(estudiante);
            }
            
            // Crear preinscripción para cada acudiente
            int contador = 1;
            for (Map.Entry<Acudiente, List<Estudiante>> entry : estudiantesPorAcudiente.entrySet()) {
                Acudiente acudiente = entry.getKey();
                List<Estudiante> estudiantesAcudiente = entry.getValue();
                
                Preinscripcion preinscripcion = new Preinscripcion();
                preinscripcion.setFechaRegistro(LocalDate.now());
                preinscripcion.setEstado(Estado.Pendiente); // PENDIENTE
                preinscripcion.setAcudiente(acudiente);
                preinscripcion.setEstudiantes(new HashSet<>(estudiantesAcudiente));
                
                // Establecer relación bidireccional
                for (Estudiante estudiante : estudiantesAcudiente) {
                    estudiante.setPreinscripcion(preinscripcion);
                }
                
                em.persist(preinscripcion);
                
                System.out.println("  " + contador + ". Preinscripción para " + 
                    acudiente.obtenerNombreCompleto() + " - " + 
                    estudiantesAcudiente.size() + " estudiantes (Grado: Párvulos, Estado: PENDIENTE)");
                contador++;
            }
            
            tx.commit();
            System.out.println("✓ " + estudiantesPorAcudiente.size() + " preinscripciones PENDIENTES creadas\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearGruposVacios(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Creando grupos vacíos para asignación futura...");
            
            GradoRepositorio gradoRepo = new GradoRepositorio(em);
            
            // Crear un grupo vacío por cada grado
            String[] gradosNombres = {"Párvulos", "Caminadores", "Pre-Jardín"};
            
            for (String nombreGrado : gradosNombres) {
                Grado grado = gradoRepo.buscarPornombreGrado(nombreGrado)
                    .orElseThrow(() -> new RuntimeException("Grado '" + nombreGrado + "' no encontrado"));
                
                Grupo grupo = new Grupo();
                grupo.setNombreGrupo(nombreGrado + "-A");
                grupo.setEstado(false); // Inactivo (sin estudiantes)
                grupo.setGrado(grado);
                grupo.setEstudiantes(new HashSet<>());
                em.persist(grupo);
                
                System.out.println("  - " + grupo.getNombreGrupo() + " (vacío, inactivo)");
            }
            
            tx.commit();
            System.out.println("✓ Grupos vacíos creados (se llenarán al aprobar estudiantes)\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void mostrarResumen(EntityManager em) {
        System.out.println("=== RESUMEN DE DATOS CARGADOS ===\n");
        
        try {
            // Contar registros
            long countUsuarios = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
            long countAcudientes = em.createQuery("SELECT COUNT(a) FROM Acudiente a", Long.class).getSingleResult();
            long countEstudiantes = em.createQuery("SELECT COUNT(e) FROM Estudiante e", Long.class).getSingleResult();
            long countPreinscripciones = em.createQuery("SELECT COUNT(p) FROM Preinscripcion p", Long.class).getSingleResult();
            
            System.out.println("📊 ESTADÍSTICAS:");
            System.out.println("  • Usuarios totales: " + countUsuarios);
            System.out.println("  • Acudientes: " + countAcudientes);
            System.out.println("  • Estudiantes: " + countEstudiantes);
            System.out.println("  • Preinscripciones: " + countPreinscripciones);
            
            // Personal del sistema
            System.out.println("\n👥 PERSONAL DEL SISTEMA:");
            System.out.println("  • Administrador: admin (contraseña: abc123)");
            System.out.println("  • Directivo: juan_aux (contraseña: pass456)");
            System.out.println("  • Profesor: maria_lect (contraseña: xyz789)");
            System.out.println("  • Acudiente aprobado: laura_acu (contraseña: acu123)");
            
            // Acudientes pendientes
            long acudientesPendientes = em.createQuery(
                "SELECT COUNT(a) FROM Acudiente a WHERE a.estadoAprobacion = :estado", Long.class)
                .setParameter("estado", Estado.Pendiente)
                .getSingleResult();
            
            System.out.println("\n⏳ ACUDIENTES PENDIENTES DE APROBACIÓN:");
            System.out.println("  • Total: " + acudientesPendientes);
            
            List<Acudiente> acudientesList = em.createQuery(
                "SELECT a FROM Acudiente a WHERE a.estadoAprobacion = :estado", Acudiente.class)
                .setParameter("estado", Estado.Pendiente)
                .getResultList();
            
            for (Acudiente acu : acudientesList) {
                long numEstudiantes = em.createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.acudiente = :acudiente", Long.class)
                    .setParameter("acudiente", acu)
                    .getSingleResult();
                
                System.out.println("    - " + acu.obtenerNombreCompleto() + 
                    " (" + numEstudiantes + " estudiantes en Párvulos)");
            }
            
            // Estudiantes solo de Párvulos
            System.out.println("\n📚 ESTUDIANTES PENDIENTES (TODOS EN PÁRVULOS):");
            long countParvulos = em.createQuery(
                "SELECT COUNT(e) FROM Estudiante e WHERE e.gradoAspira.nombreGrado = :grado AND e.estado = :estado", 
                Long.class)
                .setParameter("grado", "Párvulos")
                .setParameter("estado", Estado.Pendiente)
                .getSingleResult();
            System.out.println("  • Párvulos: " + countParvulos + " estudiantes (100%)");
            
            // Verificar que no hay estudiantes en otros grados
            long countCaminadores = em.createQuery(
                "SELECT COUNT(e) FROM Estudiante e WHERE e.gradoAspira.nombreGrado = :grado", 
                Long.class)
                .setParameter("grado", "Caminadores")
                .getSingleResult();
            
            long countPreJardin = em.createQuery(
                "SELECT COUNT(e) FROM Estudiante e WHERE e.gradoAspira.nombreGrado = :grado", 
                Long.class)
                .setParameter("grado", "Pre-Jardín")
                .getSingleResult();
            
            System.out.println("  • Caminadores: " + countCaminadores + " estudiantes (debería ser 0)");
            System.out.println("  • Pre-Jardín: " + countPreJardin + " estudiantes (debería ser 0)");
            
            // Distribución de estudiantes por acudiente
            System.out.println("\n👨‍👩‍👧‍👦 DISTRIBUCIÓN DE ESTUDIANTES POR ACUDIENTE:");
            List<Object[]> distribucion = em.createQuery(
                "SELECT a.obtenerNombreCompleto(), COUNT(e) FROM Estudiante e " +
                "JOIN e.acudiente a " +
                "GROUP BY a", Object[].class)
                .getResultList();
            
            for (Object[] resultado : distribucion) {
                System.out.println("  • " + resultado[0] + ": " + resultado[1] + " estudiantes");
            }
            
            // Instrucciones específicas
            System.out.println("\n📋 PARA PROBAR EL SISTEMA (CON TODOS EN PÁRVULOS):");
            System.out.println("  1. Inicia sesión como directivo (juan_aux / pass456)");
            System.out.println("  2. Ve a la sección de gestión de aspirantes");
            System.out.println("  3. Aprueba algunos estudiantes de Párvulos");
            System.out.println("  4. Observa cómo se generan tokens automáticamente al aprobar");
            System.out.println("  5. Verifica que TODOS los estudiantes están en Párvulos");
            System.out.println("  6. Prueba la funcionalidad de asignación por grado único");
            System.out.println("  7. Observa que el grupo 'Párvulos-A' se llenará primero");
            
            System.out.println("\n⚠️  NOTA: Esto es para probar:");
            System.out.println("  • Gestión de cupos por grado específico");
            System.out.println("  • Distribución equitativa de acudientes");
            System.out.println("  • Asignación de estudiantes a grupo por grado");
            
        } catch (Exception e) {
            System.err.println("Error generando resumen: " + e.getMessage());
        }
    }
}