package com.aplicacion;

import com.modelo.dominio.*;
import com.modelo.persistencia.repositorios.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.*;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DEL SISTEMA ===\n");
        
        EntityManager em = null;
        try {
            em = JPAUtil.getEntityManagerFactory().createEntityManager();
            
            // 1. Limpiar datos existentes (para pruebas limpias)
            limpiarDatos(em);
            
            // 2. Crear permisos y roles necesarios
            crearPermisosYRoles(em);
            
            // 3. Crear grados disponibles (incluyendo Párvulos, Caminadores, Pre-Jardín)
            crearGrados(em);
            
            // 4. Crear acudientes con datos válidos - TODOS PENDIENTES
            List<Acudiente> acudientes = crearAcudientes(em);
            
            // 5. Crear estudiantes para diferentes grados y estados
            List<Estudiante> estudiantesPrimero = crearEstudiantesPrimero(em, acudientes);
            List<Estudiante> estudiantesOtrosGrados = crearEstudiantesOtrosGrados(em, acudientes);
            
            // 5b. Crear más acudientes específicamente para pruebas de aspirantes
            List<Acudiente> acudientesAspirantes = crearAcudientesAspirantes(em);
            acudientes.addAll(acudientesAspirantes);
            
            // Combinar todos los estudiantes
            List<Estudiante> todosEstudiantes = new ArrayList<>();
            todosEstudiantes.addAll(estudiantesPrimero);
            todosEstudiantes.addAll(estudiantesOtrosGrados);
            
            // 6. Crear preinscripciones - TODAS PENDIENTES
            crearPreinscripciones(em, acudientes, todosEstudiantes);
            
            // 7. Aprobar algunos estudiantes PERO NO LOS ACUDIENTES
            aprobarAlgunosEstudiantes(em, todosEstudiantes);
            
            // 8. Crear grupos para diferentes grados con estudiantes aprobados
            crearGruposParaGrados(em, todosEstudiantes);
            
            // 9. Crear profesores y directivos
            crearProfesoresYDirectivos(em);
            
            // 10. Mostrar resumen de datos creados
            mostrarResumen(em);
            
            // 11. Probar lógica de negocio específica
            probarLogicaNegocio(em, todosEstudiantes);
            
            System.out.println("\n=== PRUEBAS COMPLETADAS EXITOSAMENTE ===");
            
        } catch (Exception e) {
            System.err.println("Error en las pruebas: " + e.getMessage());
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
            
            // Eliminar en orden correcto (evitar violaciones de FK)
            System.out.println("Limpiando datos existentes...");
            
            // Primero eliminar relaciones ManyToMany
            em.createNativeQuery("DELETE FROM rol_permiso").executeUpdate();
            
            // Luego las entidades
            em.createQuery("DELETE FROM Estudiante").executeUpdate();
            em.createQuery("DELETE FROM Preinscripcion").executeUpdate();
            em.createQuery("DELETE FROM Acudiente").executeUpdate();
            em.createQuery("DELETE FROM grupo").executeUpdate();
            em.createQuery("DELETE FROM Profesor").executeUpdate();
            em.createQuery("DELETE FROM Directivo").executeUpdate();
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
            
            System.out.println("Creando permisos del sistema...");
            
            // Crear permisos para acudiente
            Permiso consultarEstudiantes = new Permiso("CONSULTAR_ESTUDIANTES", 
                "Permite consultar información de los estudiantes a cargo");
            Permiso verBoletines = new Permiso("VER_BOLETINES", 
                "Permite visualizar los boletines académicos de los estudiantes");
            Permiso realizarPreinscripcion = new Permiso("REALIZAR_PREINSCRIPCION", 
                "Permite realizar el proceso de preinscripción de nuevos estudiantes");
            
            // Crear permisos para profesor
            Permiso gestionarGrupos = new Permiso("GESTIONAR_GRUPOS", 
                "Permite gestionar los grupos asignados al profesor");
            Permiso calificarEstudiantes = new Permiso("CALIFICAR_ESTUDIANTES", 
                "Permite calificar a los estudiantes en las diferentes asignaturas");
            Permiso generarBoletines = new Permiso("GENERAR_BOLETINES", 
                "Permite generar y publicar boletines académicos");
            Permiso asignarLogros = new Permiso("ASIGNAR_LOGROS", 
                "Permite asignar logros académicos a los estudiantes");
            
            // Crear permisos para directivo
            Permiso gestionarUsuarios = new Permiso("GESTIONAR_USUARIOS", 
                "Permite crear, modificar y eliminar usuarios del sistema");
            Permiso aprobarAspirantes = new Permiso("APROBAR_ASPIRANTES", 
                "Permite aprobar o rechazar aspirantes al sistema educativo");
            Permiso asignarProfesores = new Permiso("ASIGNAR_PROFESORES", 
                "Permite asignar profesores a los diferentes grupos");
            Permiso generarReportes = new Permiso("GENERAR_REPORTES", 
                "Permite generar reportes estadísticos del sistema");
            Permiso configurarSistema = new Permiso("CONFIGURAR_SISTEMA", 
                "Permite configurar parámetros del sistema educativo");
            
            // Persistir todos los permisos
            em.persist(consultarEstudiantes);
            em.persist(verBoletines);
            em.persist(realizarPreinscripcion);
            em.persist(gestionarGrupos);
            em.persist(calificarEstudiantes);
            em.persist(generarBoletines);
            em.persist(asignarLogros);
            em.persist(gestionarUsuarios);
            em.persist(aprobarAspirantes);
            em.persist(asignarProfesores);
            em.persist(generarReportes);
            em.persist(configurarSistema);
            
            System.out.println("✓ 12 permisos creados exitosamente");
            
            System.out.println("\nCreando roles del sistema...");
            
            // Rol Acudiente
            Rol rolAcudiente = new Rol("acudiente");
            Set<Permiso> permisosAcudiente = new HashSet<>();
            permisosAcudiente.add(consultarEstudiantes);
            permisosAcudiente.add(verBoletines);
            permisosAcudiente.add(realizarPreinscripcion);
            rolAcudiente.setPermisos(permisosAcudiente);
            em.persist(rolAcudiente);
            
            // Rol Profesor
            Rol rolProfesor = new Rol("profesor");
            Set<Permiso> permisosProfesor = new HashSet<>();
            permisosProfesor.add(gestionarGrupos);
            permisosProfesor.add(calificarEstudiantes);
            permisosProfesor.add(generarBoletines);
            permisosProfesor.add(asignarLogros);
            rolProfesor.setPermisos(permisosProfesor);
            em.persist(rolProfesor);
            
            // Rol Directivo
            Rol rolDirectivo = new Rol("directivo");
            Set<Permiso> permisosDirectivo = new HashSet<>();
            permisosDirectivo.add(gestionarUsuarios);
            permisosDirectivo.add(aprobarAspirantes);
            permisosDirectivo.add(asignarProfesores);
            permisosDirectivo.add(generarReportes);
            permisosDirectivo.add(configurarSistema);
            rolDirectivo.setPermisos(permisosDirectivo);
            em.persist(rolDirectivo);
            
            tx.commit();
            System.out.println("✓ Roles creados: acudiente, profesor, directivo\n");
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
            
            // Grados según tu base de datos
            String[] nombresGrados = {
                "Párvulos", "Caminadores", "Pre-Jardín",
                "Primero", "Segundo", "Tercero", 
                "Cuarto", "Quinto", "Sexto", "Séptimo"
            };
            
            for (String nombre : nombresGrados) {
                Grado grado = new Grado();
                grado.setNombreGrado(nombre);
                em.persist(grado);
                System.out.println("  - " + nombre);
            }
            
            tx.commit();
            System.out.println("✓ 10 grados creados exitosamente\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Acudiente> crearAcudientes(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        List<Acudiente> acudientes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando acudientes de prueba (TODOS PENDIENTES)...");
            
            // Datos de prueba válidos (cumplen todas las validaciones)
            Object[][] datosAcudientes = {
                // primerNombre, segundoNombre, primerApellido, segundoApellido, edad, nuip, correo, telefono
                {"María", "Isabel", "González", "Pérez", 35, "1234567890", "maria.gonzalez@email.com", "3101234567"},
                {"Carlos", "Alberto", "Rodríguez", "López", 40, "2345678901", "carlos.rodriguez@email.com", "3112345678"},
                {"Ana", "Sofía", "Martínez", "García", 38, "3456789012", "ana.martinez@email.com", "3123456789"},
                {"Luis", "Fernando", "Hernández", "Díaz", 42, "4567890123", "luis.hernandez@email.com", "3134567890"}
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
                
                // ¡IMPORTANTE! TODOS PENDIENTES inicialmente
                acudiente.setEstadoAprobacion(Estado.Pendiente);
                
                // Validar datos antes de persistir
                ResultadoValidacionDominio validacion = acudiente.validar();
                if (!validacion.isValido()) {
                    throw new RuntimeException("Error validando acudiente " + contador + 
                        ": " + validacion.getMensajeError());
                }
                
                em.persist(acudiente);
                acudientes.add(acudiente);
                
                System.out.println("  ✓ Acudiente " + contador + ": " + 
                    acudiente.obtenerNombreCompleto() + " (Estado: " + acudiente.getEstadoAprobacion() + ")");
                contador++;
            }
            
            tx.commit();
            System.out.println("✓ " + acudientes.size() + " acudientes creados (todos pendientes)\n");
            return acudientes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Acudiente> crearAcudientesAspirantes(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        List<Acudiente> acudientes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando acudientes aspirantes adicionales (para administración)...");
            
            // 6 acudientes adicionales específicamente para probar la administración de aspirantes
            Object[][] datosAcudientesAspirantes = {
                // primerNombre, segundoNombre, primerApellido, segundoApellido, edad, nuip, correo, telefono
                {"Laura", "Patricia", "Sánchez", "Ramírez", 36, "5678901234", "laura.sanchez@email.com", "3145678901"},
                {"Roberto", "Antonio", "Jiménez", "Vargas", 45, "6789012345", "roberto.jimenez@email.com", "3156789012"},
                {"Carmen", "Elena", "Morales", "Castaño", 32, "7890123456", "carmen.morales@email.com", "3167890123"},
                {"Javier", "Andrés", "Ortiz", "Vega", 41, "8901234567", "javier.ortiz@email.com", "3178901234"},
                {"Patricia", "Lucía", "Castro", "Mendoza", 39, "9012345678", "patricia.castro@email.com", "3189012345"},
                {"Fernando", "José", "Navarro", "Ríos", 44, "0123456789", "fernando.navarro@email.com", "3190123456"}
            };
            
            int contador = 1;
            for (Object[] datos : datosAcudientesAspirantes) {
                Acudiente acudiente = new Acudiente();
                acudiente.setPrimerNombre((String) datos[0]);
                acudiente.setSegundoNombre((String) datos[1]);
                acudiente.setPrimerApellido((String) datos[2]);
                acudiente.setSegundoApellido((String) datos[3]);
                acudiente.setEdad((Integer) datos[4]);
                acudiente.setNuipUsuario((String) datos[5]);
                acudiente.setCorreoElectronico((String) datos[6]);
                acudiente.setTelefono((String) datos[7]);
                
                // ¡IMPORTANTE! TODOS PENDIENTES inicialmente
                acudiente.setEstadoAprobacion(Estado.Pendiente);
                
                // Validar datos antes de persistir
                ResultadoValidacionDominio validacion = acudiente.validar();
                if (!validacion.isValido()) {
                    throw new RuntimeException("Error validando acudiente aspirante " + contador + 
                        ": " + validacion.getMensajeError());
                }
                
                em.persist(acudiente);
                acudientes.add(acudiente);
                
                System.out.println("  ✓ Acudiente aspirante " + contador + ": " + 
                    acudiente.obtenerNombreCompleto() + " (Estado: " + acudiente.getEstadoAprobacion() + ")");
                contador++;
            }
            
            tx.commit();
            System.out.println("✓ " + acudientes.size() + " acudientes aspirantes adicionales creados (todos pendientes)\n");
            return acudientes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Estudiante> crearEstudiantesPrimero(EntityManager em, List<Acudiente> acudientes) {
        EntityTransaction tx = em.getTransaction();
        List<Estudiante> estudiantes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando estudiantes para grado Primero (6 estudiantes - todos pendientes)...");
            
            GradoRepositorio gradoRepo = new GradoRepositorio(em);
            Grado gradoPrimero = gradoRepo.buscarPornombreGrado("Primero")
                .orElseThrow(() -> new RuntimeException("Grado 'Primero' no encontrado"));
            
            // 6 estudiantes para el mismo grado (todos pendientes)
            Object[][] datosEstudiantes = {
                // primerNombre, segundoNombre, primerApellido, segundoApellido, edad, nuip, estado
                {"Juan", "David", "González", "López", 6, "1000000001", Estado.Pendiente},
                {"Sofía", "Camila", "González", "López", 7, "1000000002", Estado.Pendiente},
                {"Mateo", "Alejandro", "Rodríguez", "Méndez", 6, "1000000003", Estado.Pendiente},
                {"Valentina", "Isabella", "Rodríguez", "Méndez", 7, "1000000004", Estado.Pendiente},
                {"Samuel", "Esteban", "Martínez", "Fernández", 6, "1000000005", Estado.Pendiente},
                {"Emily", "Nicole", "Martínez", "Fernández", 7, "1000000006", Estado.Pendiente}
            };
            
            int contador = 1;
            int indiceAcudiente = 0;
            
            for (Object[] datos : datosEstudiantes) {
                Estudiante estudiante = new Estudiante();
                estudiante.setPrimerNombre((String) datos[0]);
                estudiante.setSegundoNombre((String) datos[1]);
                estudiante.setPrimerApellido((String) datos[2]);
                estudiante.setSegundoApellido((String) datos[3]);
                estudiante.setEdad((Integer) datos[4]);
                estudiante.setNuip((String) datos[5]);
                estudiante.setEstado((Estado) datos[6]);
                estudiante.setGradoAspira(gradoPrimero);
                estudiante.setGrupo(null); // IMPORTANTE: Grupo null para pendientes
                
                // Asignar acudiente (distribuir estudiantes entre acudientes)
                Acudiente acudiente = acudientes.get(indiceAcudiente);
                estudiante.setAcudiente(acudiente);
                
                // Validar datos antes de persistir
                ResultadoValidacionDominio validacion = estudiante.validar();
                if (!validacion.isValido()) {
                    throw new RuntimeException("Error validando estudiante " + contador + 
                        ": " + validacion.getMensajeError());
                }
                
                em.persist(estudiante);
                estudiantes.add(estudiante);
                
                System.out.println("  ✓ Estudiante " + contador + ": " + 
                    estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() +
                    " (Estado: " + estudiante.getEstado() + ", Grupo: null)");
                
                contador++;
                indiceAcudiente = (indiceAcudiente + 1) % acudientes.size();
            }
            
            tx.commit();
            System.out.println("✓ " + estudiantes.size() + " estudiantes para Primero creados exitosamente\n");
            return estudiantes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static List<Estudiante> crearEstudiantesOtrosGrados(EntityManager em, List<Acudiente> acudientes) {
        EntityTransaction tx = em.getTransaction();
        List<Estudiante> estudiantes = new ArrayList<>();
        
        try {
            tx.begin();
            
            System.out.println("Creando estudiantes para otros grados (Párvulos, Caminadores, Pre-Jardín)...");
            
            GradoRepositorio gradoRepo = new GradoRepositorio(em);
            
            // Obtener grados
            Grado gradoParvulos = gradoRepo.buscarPornombreGrado("Párvulos")
                .orElseThrow(() -> new RuntimeException("Grado 'Párvulos' no encontrado"));
            Grado gradoCaminadores = gradoRepo.buscarPornombreGrado("Caminadores")
                .orElseThrow(() -> new RuntimeException("Grado 'Caminadores' no encontrado"));
            Grado gradoPreJardin = gradoRepo.buscarPornombreGrado("Pre-Jardín")
                .orElseThrow(() -> new RuntimeException("Grado 'Pre-Jardín' no encontrado"));
            
            // Estudiantes para Párvulos (3 años aprox.)
            Object[][] datosParvulos = {
                {"Lucas", "Andrés", "Díaz", "Gómez", 3, "2000000001", Estado.Pendiente},
                {"Emma", "Victoria", "Díaz", "Gómez", 4, "2000000002", Estado.Pendiente},
                {"Daniel", "José", "Torres", "Ruiz", 3, "2000000003", Estado.Pendiente},
                {"Olivia", "María", "Torres", "Ruiz", 4, "2000000004", Estado.Pendiente}
            };
            
            // Estudiantes para Caminadores (4-5 años aprox.) - 6 estudiantes
            Object[][] datosCaminadores = {
                {"Diego", "Alejandro", "Castro", "Molina", 4, "3000000001", Estado.Aprobada},
                {"Isabella", "Carolina", "Castro", "Molina", 5, "3000000002", Estado.Aprobada},
                {"Sebastián", "Felipe", "Ortega", "Silva", 4, "3000000003", Estado.Aprobada},
                {"Camila", "Alejandra", "Ortega", "Silva", 5, "3000000004", Estado.Aprobada},
                {"Nicolás", "Gabriel", "Rojas", "Peña", 4, "3000000005", Estado.Aprobada},
                {"Mariana", "Fernanda", "Rojas", "Peña", 5, "3000000006", Estado.Aprobada}
            };
            
            // Estudiantes para Pre-Jardín (5-6 años aprox.) - 6 estudiantes
            Object[][] datosPreJardin = {
                {"Miguel", "Ángel", "Vargas", "Herrera", 5, "4000000001", Estado.Aprobada},
                {"Valeria", "Antonella", "Vargas", "Herrera", 6, "4000000002", Estado.Aprobada},
                {"David", "Esteban", "Flores", "Cruz", 5, "4000000003", Estado.Aprobada},
                {"Gabriela", "Isabel", "Flores", "Cruz", 6, "4000000004", Estado.Aprobada},
                {"Julián", "Ricardo", "Santos", "Romero", 5, "4000000005", Estado.Aprobada},
                {"Paula", "Andrea", "Santos", "Romero", 6, "4000000006", Estado.Aprobada}
            };
            
            int contador = 1;
            int indiceAcudiente = 0;
            
            // Crear estudiantes Párvulos
            System.out.println("\n  Creando 4 estudiantes para Párvulos (pendientes):");
            for (Object[] datos : datosParvulos) {
                Estudiante estudiante = crearEstudianteDesdeDatos(datos, gradoParvulos, acudientes.get(indiceAcudiente));
                em.persist(estudiante);
                estudiantes.add(estudiante);
                System.out.println("    - " + estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() +
                    " (Estado: " + estudiante.getEstado() + ", Grupo: null)");
                contador++;
                indiceAcudiente = (indiceAcudiente + 1) % acudientes.size();
            }
            
            // Crear estudiantes Caminadores
            System.out.println("\n  Creando 6 estudiantes para Caminadores (aprobados):");
            for (Object[] datos : datosCaminadores) {
                Estudiante estudiante = crearEstudianteDesdeDatos(datos, gradoCaminadores, acudientes.get(indiceAcudiente));
                em.persist(estudiante);
                estudiantes.add(estudiante);
                System.out.println("    - " + estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() +
                    " (Estado: " + estudiante.getEstado() + ", Grupo: por asignar)");
                contador++;
                indiceAcudiente = (indiceAcudiente + 1) % acudientes.size();
            }
            
            // Crear estudiantes Pre-Jardín
            System.out.println("\n  Creando 6 estudiantes para Pre-Jardín (aprobados):");
            for (Object[] datos : datosPreJardin) {
                Estudiante estudiante = crearEstudianteDesdeDatos(datos, gradoPreJardin, acudientes.get(indiceAcudiente));
                em.persist(estudiante);
                estudiantes.add(estudiante);
                System.out.println("    - " + estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() +
                    " (Estado: " + estudiante.getEstado() + ", Grupo: por asignar)");
                contador++;
                indiceAcudiente = (indiceAcudiente + 1) % acudientes.size();
            }
            
            tx.commit();
            System.out.println("\n✓ " + estudiantes.size() + " estudiantes para otros grados creados exitosamente\n");
            return estudiantes;
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static Estudiante crearEstudianteDesdeDatos(Object[] datos, Grado grado, Acudiente acudiente) {
        Estudiante estudiante = new Estudiante();
        estudiante.setPrimerNombre((String) datos[0]);
        estudiante.setSegundoNombre((String) datos[1]);
        estudiante.setPrimerApellido((String) datos[2]);
        estudiante.setSegundoApellido((String) datos[3]);
        estudiante.setEdad((Integer) datos[4]);
        estudiante.setNuip((String) datos[5]);
        estudiante.setEstado((Estado) datos[6]);
        estudiante.setGradoAspira(grado);
        estudiante.setAcudiente(acudiente);
        
        // Solo los pendientes tienen grupo null, los aprobados se asignarán después
        if (estudiante.getEstado() == Estado.Pendiente) {
            estudiante.setGrupo(null);
        }
        
        // Validar datos
        ResultadoValidacionDominio validacion = estudiante.validar();
        if (!validacion.isValido()) {
            throw new RuntimeException("Error validando estudiante: " + validacion.getMensajeError());
        }
        
        return estudiante;
    }
    
    private static void crearPreinscripciones(EntityManager em, List<Acudiente> acudientes, List<Estudiante> estudiantes) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Creando preinscripciones (TODAS PENDIENTES)...");
            
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
                
                // ¡IMPORTANTE! TODAS las preinscripciones PENDIENTES inicialmente
                // El directivo debe aprobarlas manualmente
                preinscripcion.setEstado(Estado.Pendiente);
                
                preinscripcion.setAcudiente(acudiente);
                preinscripcion.setEstudiantes(new HashSet<>(estudiantesAcudiente));
                
                // Establecer relación bidireccional
                for (Estudiante estudiante : estudiantesAcudiente) {
                    estudiante.setPreinscripcion(preinscripcion);
                }
                
                em.persist(preinscripcion);
                
                // Contar estudiantes aprobados en esta preinscripción
                long estudiantesAprobados = estudiantesAcudiente.stream()
                    .filter(e -> e.getEstado() == Estado.Aprobada)
                    .count();
                
                System.out.println("  ✓ Preinscripción " + contador + ": Acudiente " + 
                    acudiente.getPrimerNombre() + " " + acudiente.getPrimerApellido() + 
                    " - " + estudiantesAcudiente.size() + " estudiantes (" + 
                    estudiantesAprobados + " aprobados)" + 
                    " (Estado: " + preinscripcion.getEstado() + ")");
                contador++;
            }
            
            tx.commit();
            System.out.println("✓ " + estudiantesPorAcudiente.size() + " preinscripciones creadas (TODAS PENDIENTES)\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void aprobarAlgunosEstudiantes(EntityManager em, List<Estudiante> estudiantes) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Aprobando algunos estudiantes para crear grupos...");
            
            // Solo aprobar estudiantes de Caminadores y Pre-Jardín (estos ya están como Aprobada en datos)
            // ¡IMPORTANTE! NO APROBAR ACUDIENTES AUTOMÁTICAMENTE
            int aprobados = 0;
            
            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getEstado() == Estado.Aprobada) {
                    aprobados++;
                    // NOTA: NO APROBAMOS AL ACUDIENTE aquí
                    // El directivo debe aprobarlo manualmente en la interfaz
                }
            }
            
            tx.commit();
            System.out.println("✓ " + aprobados + " estudiantes ya aprobados (acudientes permanecen pendientes)\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearGruposParaGrados(EntityManager em, List<Estudiante> estudiantes) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Creando grupos para diferentes grados...");
            
            GradoRepositorio gradoRepo = new GradoRepositorio(em);
            
            // Obtener grados
            Grado gradoCaminadores = gradoRepo.buscarPornombreGrado("Caminadores")
                .orElseThrow(() -> new RuntimeException("Grado 'Caminadores' no encontrado"));
            Grado gradoPreJardin = gradoRepo.buscarPornombreGrado("Pre-Jardín")
                .orElseThrow(() -> new RuntimeException("Grado 'Pre-Jardín' no encontrado"));
            
            // Filtrar estudiantes aprobados por grado
            List<Estudiante> estudiantesCaminadores = new ArrayList<>();
            List<Estudiante> estudiantesPreJardin = new ArrayList<>();
            
            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getEstado() == Estado.Aprobada && estudiante.getGradoAspira() != null) {
                    if (estudiante.getGradoAspira().getNombreGrado().equals("Caminadores")) {
                        estudiantesCaminadores.add(estudiante);
                    } else if (estudiante.getGradoAspira().getNombreGrado().equals("Pre-Jardín")) {
                        estudiantesPreJardin.add(estudiante);
                    }
                }
            }
            
            // Crear grupo para Caminadores
            if (!estudiantesCaminadores.isEmpty()) {
                Grupo grupoCaminadores = new Grupo();
                grupoCaminadores.setNombreGrupo("Caminadores-A");
                grupoCaminadores.setEstado(false); // Inactivo hasta tener 5 estudiantes
                grupoCaminadores.setGrado(gradoCaminadores);
                grupoCaminadores.setEstudiantes(new HashSet<>());
                
                em.persist(grupoCaminadores);
                
                int asignados = 0;
                for (Estudiante estudiante : estudiantesCaminadores) {
                    if (grupoCaminadores.agregarEstudiante(estudiante)) {
                        estudiante.setGrupo(grupoCaminadores);
                        em.merge(estudiante);
                        asignados++;
                    }
                }
                
                // Activar grupo si tiene suficientes estudiantes
                grupoCaminadores.activar();
                em.merge(grupoCaminadores);
                
                System.out.println("  ✓ Grupo 'Caminadores-A' creado con " + asignados + " estudiantes");
                System.out.println("    • Estado: " + (grupoCaminadores.isEstado() ? "ACTIVO" : "INACTIVO"));
                System.out.println("    • Estudiantes: " + grupoCaminadores.getCantidadEstudiantes() + "/" + grupoCaminadores.getMAXESTUDIANTES());
            }
            
            // Crear grupo para Pre-Jardín
            if (!estudiantesPreJardin.isEmpty()) {
                Grupo grupoPreJardin = new Grupo();
                grupoPreJardin.setNombreGrupo("Pre-Jardín-A");
                grupoPreJardin.setEstado(false);
                grupoPreJardin.setGrado(gradoPreJardin);
                grupoPreJardin.setEstudiantes(new HashSet<>());
                
                em.persist(grupoPreJardin);
                
                int asignados = 0;
                for (Estudiante estudiante : estudiantesPreJardin) {
                    if (grupoPreJardin.agregarEstudiante(estudiante)) {
                        estudiante.setGrupo(grupoPreJardin);
                        em.merge(estudiante);
                        asignados++;
                    }
                }
                
                // Activar grupo si tiene suficientes estudiantes
                grupoPreJardin.activar();
                em.merge(grupoPreJardin);
                
                System.out.println("  ✓ Grupo 'Pre-Jardín-A' creado con " + asignados + " estudiantes");
                System.out.println("    • Estado: " + (grupoPreJardin.isEstado() ? "ACTIVO" : "INACTIVO"));
                System.out.println("    • Estudiantes: " + grupoPreJardin.getCantidadEstudiantes() + "/" + grupoPreJardin.getMAXESTUDIANTES());
            }
            
            // Crear un grupo vacío para Primero (sin estudiantes aún)
            Grado gradoPrimero = gradoRepo.buscarPornombreGrado("Primero")
                .orElseThrow(() -> new RuntimeException("Grado 'Primero' no encontrado"));
            
            Grupo grupoPrimero = new Grupo();
            grupoPrimero.setNombreGrupo("Primero-A");
            grupoPrimero.setEstado(false); // Inactivo (sin estudiantes)
            grupoPrimero.setGrado(gradoPrimero);
            grupoPrimero.setEstudiantes(new HashSet<>());
            em.persist(grupoPrimero);
            
            System.out.println("  ✓ Grupo 'Primero-A' creado (vacío, para futuras asignaciones)");
            System.out.println("    • Estado: " + (grupoPrimero.isEstado() ? "ACTIVO" : "INACTIVO"));
            System.out.println("    • Estudiantes: 0/" + grupoPrimero.getMAXESTUDIANTES());
            
            tx.commit();
            System.out.println("\n✓ Grupos creados exitosamente\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void crearProfesoresYDirectivos(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            System.out.println("Creando profesores y directivos...");
            
            RolRepositorio rolRepo = new RolRepositorio(em);
            Rol rolProfesor = rolRepo.buscarPorNombreRol("profesor")
                .orElseThrow(() -> new RuntimeException("Rol profesor no encontrado"));
            Rol rolDirectivo = rolRepo.buscarPorNombreRol("directivo")
                .orElseThrow(() -> new RuntimeException("Rol directivo no encontrado"));
            
            // Crear profesor 1 - Teléfono único
            Profesor profesor1 = new Profesor();
            profesor1.setPrimerNombre("Laura");
            profesor1.setSegundoNombre("Patricia");
            profesor1.setPrimerApellido("Sánchez");
            profesor1.setSegundoApellido("Ramírez");
            profesor1.setEdad(45);
            profesor1.setNuipUsuario("5000000001");
            profesor1.setCorreoElectronico("laura.sanchez@colegio.edu");
            profesor1.setTelefono("3156789013");
            
            TokenUsuario tokenProfesor1 = TokenUsuario.generarTokenDesdeUsuario(
                profesor1.getPrimerNombre(),
                profesor1.getSegundoNombre(),
                profesor1.getPrimerApellido(),
                profesor1.getSegundoApellido(),
                rolProfesor
            );
            em.persist(tokenProfesor1);
            profesor1.setTokenAccess(tokenProfesor1);
            em.persist(profesor1);
            
            System.out.println("  ✓ Profesor 1 creado: " + profesor1.obtenerNombreCompleto() +
                " (Usuario: " + tokenProfesor1.getNombreUsuario() + ")");
            
            // Crear profesor 2
            Profesor profesor2 = new Profesor();
            profesor2.setPrimerNombre("Carlos");
            profesor2.setSegundoNombre("Eduardo");
            profesor2.setPrimerApellido("Mendoza");
            profesor2.setSegundoApellido("Pérez");
            profesor2.setEdad(42);
            profesor2.setNuipUsuario("5000000002");
            profesor2.setCorreoElectronico("carlos.mendoza@colegio.edu");
            profesor2.setTelefono("5000000002");
            
            TokenUsuario tokenProfesor2 = TokenUsuario.generarTokenDesdeUsuario(
                profesor2.getPrimerNombre(),
                profesor2.getSegundoNombre(),
                profesor2.getPrimerApellido(),
                profesor2.getSegundoApellido(),
                rolProfesor
            );
            em.persist(tokenProfesor2);
            profesor2.setTokenAccess(tokenProfesor2);
            em.persist(profesor2);
            
            System.out.println("  ✓ Profesor 2 creado: " + profesor2.obtenerNombreCompleto() +
                " (Usuario: " + tokenProfesor2.getNombreUsuario() + ")");
            
            // Crear profesor 3
            Profesor profesor3 = new Profesor();
            profesor3.setPrimerNombre("Ana");
            profesor3.setSegundoNombre("María");
            profesor3.setPrimerApellido("Gómez");
            profesor3.setSegundoApellido("Torres");
            profesor3.setEdad(38);
            profesor3.setNuipUsuario("5000000003");
            profesor3.setCorreoElectronico("ana.gomez@colegio.edu");
            profesor3.setTelefono("5000000003");
            
            TokenUsuario tokenProfesor3 = TokenUsuario.generarTokenDesdeUsuario(
                profesor3.getPrimerNombre(),
                profesor3.getSegundoNombre(),
                profesor3.getPrimerApellido(),
                profesor3.getSegundoApellido(),
                rolProfesor
            );
            em.persist(tokenProfesor3);
            profesor3.setTokenAccess(tokenProfesor3);
            em.persist(profesor3);
            
            System.out.println("  ✓ Profesor 3 creado: " + profesor3.obtenerNombreCompleto() +
                " (Usuario: " + tokenProfesor3.getNombreUsuario() + ")");
            
            // Crear directivo
            Directivo directivo = new Directivo();
            directivo.setPrimerNombre("Roberto");
            directivo.setSegundoNombre("Antonio");
            directivo.setPrimerApellido("Jiménez");
            directivo.setSegundoApellido("Vargas");
            directivo.setEdad(50);
            directivo.setNuipUsuario("6000000001");
            directivo.setCorreoElectronico("roberto.jimenez@colegio.edu");
            directivo.setTelefono("6000000001");
            
            TokenUsuario tokenDirectivo = TokenUsuario.generarTokenDesdeUsuario(
                directivo.getPrimerNombre(),
                directivo.getSegundoNombre(),
                directivo.getPrimerApellido(),
                directivo.getSegundoApellido(),
                rolDirectivo
            );
            em.persist(tokenDirectivo);
            directivo.setTokenAccess(tokenDirectivo);
            em.persist(directivo);
            
            System.out.println("  ✓ Directivo creado: " + directivo.obtenerNombreCompleto() +
                " (Usuario: " + tokenDirectivo.getNombreUsuario() + ")");
            
            tx.commit();
            System.out.println("✓ Personal académico creado exitosamente (3 profesores, 1 directivo)\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
    
    private static void mostrarResumen(EntityManager em) {
        System.out.println("=== RESUMEN DE DATOS CREADOS ===");
        
        try {
            // Contar registros
            long countUsuarios = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
            long countAcudientes = em.createQuery("SELECT COUNT(a) FROM Acudiente a", Long.class).getSingleResult();
            long countEstudiantes = em.createQuery("SELECT COUNT(e) FROM Estudiante e", Long.class).getSingleResult();
            long countGrupos = em.createQuery("SELECT COUNT(g) FROM grupo g", Long.class).getSingleResult();
            long countPreinscripciones = em.createQuery("SELECT COUNT(p) FROM Preinscripcion p", Long.class).getSingleResult();
            long countRoles = em.createQuery("SELECT COUNT(r) FROM Rol r", Long.class).getSingleResult();
            long countPermisos = em.createQuery("SELECT COUNT(p) FROM Permiso p", Long.class).getSingleResult();
            long countProfesores = em.createQuery("SELECT COUNT(p) FROM Profesor p", Long.class).getSingleResult();
            
            System.out.println("Usuarios totales: " + countUsuarios);
            System.out.println("Acudientes: " + countAcudientes);
            System.out.println("Estudiantes: " + countEstudiantes);
            System.out.println("Grupos: " + countGrupos);
            System.out.println("Preinscripciones: " + countPreinscripciones);
            System.out.println("Profesores: " + countProfesores);
            System.out.println("Tokens de acceso: " + 
                em.createQuery("SELECT COUNT(t) FROM token_usuario t", Long.class).getSingleResult());
            System.out.println("Roles: " + countRoles);
            System.out.println("Permisos: " + countPermisos);
            
            // Mostrar estudiantes por estado
            long pendientes = em.createQuery("SELECT COUNT(e) FROM Estudiante e WHERE e.estado = :estado", Long.class)
                .setParameter("estado", Estado.Pendiente)
                .getSingleResult();
            long aprobados = em.createQuery("SELECT COUNT(e) FROM Estudiante e WHERE e.estado = :estado", Long.class)
                .setParameter("estado", Estado.Aprobada)
                .getSingleResult();
            
            System.out.println("\nEstudiantes por estado:");
            System.out.println("  • Pendientes: " + pendientes + " (sin grupo asignado)");
            System.out.println("  • Aprobados: " + aprobados + " (con/sin grupo asignado)");
            
            // Mostrar estudiantes con grupo asignado
            long conGrupo = em.createQuery("SELECT COUNT(e) FROM Estudiante e WHERE e.grupo IS NOT NULL", Long.class)
                .getSingleResult();
            System.out.println("  • Con grupo asignado: " + conGrupo);
            
            // Mostrar ACUDIENTES por estado (¡IMPORTANTE!)
            long acudientesPendientes = em.createQuery("SELECT COUNT(a) FROM Acudiente a WHERE a.estadoAprobacion = :estado", Long.class)
                .setParameter("estado", Estado.Pendiente)
                .getSingleResult();
            long acudientesAprobados = em.createQuery("SELECT COUNT(a) FROM Acudiente a WHERE a.estadoAprobacion = :estado", Long.class)
                .setParameter("estado", Estado.Aprobada)
                .getSingleResult();
            
            System.out.println("\n¡ACUDIENTES PARA ADMINISTRAR!:");
            System.out.println("  • Pendientes: " + acudientesPendientes + " (para administrar por el directivo)");
            System.out.println("  • Aprobados: " + acudientesAprobados);
            
            // Mostrar PREINSCRIPCIONES por estado
            long preinscripcionesPendientes = em.createQuery("SELECT COUNT(p) FROM Preinscripcion p WHERE p.estado = :estado", Long.class)
                .setParameter("estado", Estado.Pendiente)
                .getSingleResult();
            long preinscripcionesAprobadas = em.createQuery("SELECT COUNT(p) FROM Preinscripcion p WHERE p.estado = :estado", Long.class)
                .setParameter("estado", Estado.Aprobada)
                .getSingleResult();
            
            System.out.println("\nPreinscripciones por estado:");
            System.out.println("  • Pendientes: " + preinscripcionesPendientes + " (para administrar por el directivo)");
            System.out.println("  • Aprobadas: " + preinscripcionesAprobadas);
            
            // Mostrar grupos con información detallada
            System.out.println("\nGrupos creados:");
            List<Grupo> grupos = em.createQuery("SELECT g FROM grupo g ORDER BY g.nombreGrupo", Grupo.class).getResultList();
            for (Grupo grupo : grupos) {
                String gradoNombre = grupo.getGrado() != null ? grupo.getGrado().getNombreGrado() : "N/A";
                System.out.println("  • " + grupo.getNombreGrupo() + 
                    " (Grado: " + gradoNombre + 
                    ", Estado: " + (grupo.isEstado() ? "ACTIVO" : "INACTIVO") + 
                    ", Estudiantes: " + grupo.getCantidadEstudiantes() + ")");
            }
            
            // Mostrar profesores disponibles
            System.out.println("\nProfesores disponibles para asignar:");
            List<Profesor> profesores = em.createQuery("SELECT p FROM Profesor p", Profesor.class).getResultList();
            for (Profesor profesor : profesores) {
                System.out.println("  • " + profesor.obtenerNombreCompleto() + 
                    " (Usuario: " + (profesor.getTokenAccess() != null ? profesor.getTokenAccess().getNombreUsuario() : "Sin token") + ")");
            }
            
            // Mostrar algunos acudientes pendientes como ejemplo
            System.out.println("\nAlgunos acudientes pendientes (para administrar):");
            List<Acudiente> acudientesPendientesList = em.createQuery(
                "SELECT a FROM Acudiente a WHERE a.estadoAprobacion = :estado", Acudiente.class)
                .setParameter("estado", Estado.Pendiente)
                .setMaxResults(5)
                .getResultList();
            
            for (Acudiente acudiente : acudientesPendientesList) {
                System.out.println("  • " + acudiente.obtenerNombreCompleto() + 
                    " (Tel: " + acudiente.getTelefono() + ", Email: " + acudiente.getCorreoElectronico() + ")");
            }
            
            if (acudientesPendientes > 5) {
                System.out.println("  • ... y otros " + (acudientesPendientes - 5) + " acudientes más");
            }
            
        } catch (Exception e) {
            System.out.println("Error al generar resumen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void probarLogicaNegocio(EntityManager em, List<Estudiante> estudiantes) {
        System.out.println("\n=== PROBANDO LÓGICA DE NEGOCIO ===");
        
        try {
            // 1. Probar validaciones de dominio
            System.out.println("1. Probando validaciones de dominio:");
            
            Estudiante primerEstudiante = estudiantes.get(0);
            ResultadoValidacionDominio validacionEstudiante = primerEstudiante.validar();
            System.out.println("   • Validación estudiante: " + 
                (validacionEstudiante.isValido() ? "✓ VÁLIDO" : "✗ INVÁLIDO: " + validacionEstudiante.getMensajeError()));
            
            // 2. Probar que estudiantes pendientes tienen grupo null
            System.out.println("\n2. Verificando que estudiantes pendientes tienen grupo null:");
            int pendientesConGrupoNull = 0;
            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getEstado() == Estado.Pendiente && estudiante.getGrupo() == null) {
                    pendientesConGrupoNull++;
                }
            }
            System.out.println("   • Estudiantes pendientes con grupo null: " + pendientesConGrupoNull + "/" + 
                estudiantes.stream().filter(e -> e.getEstado() == Estado.Pendiente).count());
            
            // 3. Probar límite de estudiantes por acudiente
            System.out.println("\n3. Probando límite de estudiantes por acudiente:");
            Acudiente primerAcudiente = estudiantes.get(0).getAcudiente();
            System.out.println("   • Acudiente: " + primerAcudiente.obtenerNombreCompleto());
            System.out.println("   • Estudiantes actuales: " + primerAcudiente.getEstudiantes().size());
            System.out.println("   • ¿Puede agregar más estudiantes? " + primerAcudiente.puedeAgregarMasEstudiantes());
            System.out.println("   • Cupos restantes: " + primerAcudiente.obtenerCuposRestantes());
            
            // 4. Probar lógica de grupo
            System.out.println("\n4. Probando lógica de grupos:");
            List<Grupo> grupos = em.createQuery("SELECT g FROM grupo g ORDER BY g.nombreGrupo", Grupo.class).getResultList();
            for (Grupo grupo : grupos) {
                System.out.println("   • " + grupo.getNombreGrupo() + ":");
                System.out.println("     - Estudiantes: " + grupo.getCantidadEstudiantes());
                System.out.println("     - ¿Tiene suficientes estudiantes (>=5)? " + grupo.tieneEstudiantesSuficientes());
                System.out.println("     - ¿Está en formación? " + grupo.estaEnFormacion());
                System.out.println("     - ¿Está listo? " + grupo.estaListo());
                System.out.println("     - ¿Tiene disponibilidad? " + grupo.tieneDisponibilidad());
            }
            
            // 5. Probar que hay grupos listos para asignar profesor
            System.out.println("\n5. Grupos listos para asignar profesor:");
            for (Grupo grupo : grupos) {
                if (grupo.estaListo() && !grupo.tieneProfesorAsignado()) {
                    System.out.println("   • " + grupo.getNombreGrupo() + " está listo y necesita profesor");
                }
            }
            
            // 6. Probar token de usuario
            System.out.println("\n6. Probando tokens de usuario:");
            TokenUsuario token = em.createQuery("SELECT t FROM token_usuario t", TokenUsuario.class)
                .setMaxResults(1)
                .getSingleResult();
            System.out.println("   • Usuario generado: " + token.getNombreUsuario());
            System.out.println("   • Contraseña generada: " + token.getContrasena());
            System.out.println("   • Rol asignado: " + token.getRol().getNombre());
            
            // 7. Probar asignación de profesor a grupo
            System.out.println("\n7. Estado de asignación de profesores a grupos:");
            for (Grupo grupo : grupos) {
                if (grupo.tieneProfesorAsignado()) {
                    System.out.println("   • " + grupo.getNombreGrupo() + ": tiene profesor asignado");
                } else if (grupo.estaListo()) {
                    System.out.println("   • " + grupo.getNombreGrupo() + ": LISTO para asignar profesor");
                } else if (grupo.estaEnFormacion()) {
                    System.out.println("   • " + grupo.getNombreGrupo() + ": en formación (" + 
                        grupo.getCantidadEstudiantes() + "/5 estudiantes)");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error en pruebas de lógica: " + e.getMessage());
            e.printStackTrace();
        }
    }
}