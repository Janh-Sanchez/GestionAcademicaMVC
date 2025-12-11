// Archivo: AsignadorGrupos.java
package com.modelo;

import java.util.Comparator;
import java.util.List;

import com.modelo.dominio.Grado;
import com.modelo.dominio.Grupo;

/**
 * Clase simple para manejar la lógica de asignación de estudiantes a grupos
 */
public class AsignadorGrupos {
    
    /**
     * Encuentra el mejor grupo disponible para un estudiante
     * Estrategia simple: Llenar grupos secuencialmente
     */
    public static Grupo encontrarGrupoParaEstudiante(List<Grupo> gruposDelGrado) {
        if (gruposDelGrado == null || gruposDelGrado.isEmpty()) {
            return null;
        }
        
        // Ordenar grupos por:
        // 1. Primero los que no están llenos
        // 2. Luego por cantidad de estudiantes (más llenos primero)
        // 3. Finalmente por ID (más antiguos primero)
        gruposDelGrado.sort(Comparator
            .comparing((Grupo g) -> !g.estaLleno())  // Primero los no llenos
            .thenComparing(Comparator.comparingInt(Grupo::getCantidadEstudiantes).reversed()) // Más llenos primero
            .thenComparing(Grupo::getIdGrupo) // Más antiguos primero
        );
        
        // Tomar el primer grupo que no esté lleno
        for (Grupo grupo : gruposDelGrado) {
            if (!grupo.estaLleno()) {
                return grupo;
            }
        }
        
        // Todos están llenos
        return null;
    }
    
    /**
     * Genera el nombre para un nuevo grupo
     */
    public static String generarNombreNuevoGrupo(Grado grado, long cantidadGruposExistentes) {
        return String.format("%s-%d", grado.getNombreGrado(), cantidadGruposExistentes + 1);
    }
}