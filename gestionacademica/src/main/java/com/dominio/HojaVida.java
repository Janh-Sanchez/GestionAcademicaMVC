package com.dominio;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class HojaVida {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idHojaVida;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante", unique = true)
    private Estudiante estudiante;

    @ElementCollection
    private Set<String> alergias;

    @ElementCollection
    private Set<String> aspectosRelevantes;

    @ElementCollection
    private Set<String> enfermedades;

    public HojaVida(){

    }

    public void generarHojaDeVida(){

    }
}//end HojaVida