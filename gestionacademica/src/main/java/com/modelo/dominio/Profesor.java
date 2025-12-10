package com.modelo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity(name = "profesor")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Profesor extends Usuario{
    @OneToOne(mappedBy = "profesor", fetch = FetchType.LAZY, optional = true)
    private Grupo grupoAsignado;

    public Profesor(Integer idUsuario, String nuipUsuario, String primerNombre, String segundoNombre, 
                   String primerApellido, String segundoApellido, int edad, String correoElectronico, 
                   String telefono, TokenUsuario tokenAccess, Grupo grupoAsignado){
        super(idUsuario, nuipUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, 
              edad, correoElectronico, telefono, tokenAccess);
        this.grupoAsignado = grupoAsignado;
    }

    public Profesor(){
        super();
    }

    // ============================================
    // MÉTODOS DE NEGOCIO (LÓGICA DE DOMINIO)
    // ============================================
    
    /**
     * Verifica si el profesor tiene un grupo asignado
     */
    public boolean tieneGrupoAsignado(){
        return grupoAsignado != null;
    }

    /**
     * Asigna un grupo al profesor con validación de negocio
     * Regla: Un profesor solo puede tener un grupo asignado
     */
    public ResultadoOperacion asignarGrupo(Grupo grupo){
        if (grupo == null) {
            return ResultadoOperacion.error("El grupo no puede ser nulo");
        }
        
        if (tieneGrupoAsignado()) {
            return ResultadoOperacion.error(
                "El profesor ya tiene asignado el grupo: " + grupoAsignado.getNombreGrupo());
        }
        
        if (!grupo.estaListo()) {
            return ResultadoOperacion.error(
                "El grupo " + grupo.getNombreGrupo() + " aún está en formación");
        }
        
        if (grupo.tieneProfesorAsignado()) {
            return ResultadoOperacion.error(
                "El grupo ya tiene un profesor asignado");
        }
        
        this.grupoAsignado = grupo;
        grupo.setProfesor(this);
        
        return ResultadoOperacion.exito("Grupo asignado correctamente");
    }

    /**
     * Remueve la asignación del grupo actual
     */
    public ResultadoOperacion removerGrupo(){
        if (!tieneGrupoAsignado()) {
            return ResultadoOperacion.error("El profesor no tiene grupo asignado");
        }
        
        Grupo grupoAnterior = this.grupoAsignado;
        this.grupoAsignado = null;
        grupoAnterior.setProfesor(null);
        
        return ResultadoOperacion.exito("Grupo removido correctamente");
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================
    
    public Grupo getGrupoAsignado(){
        return grupoAsignado;
    }

    public void setGrupo(Grupo grupo){
        this.grupoAsignado = grupo;
    }
}