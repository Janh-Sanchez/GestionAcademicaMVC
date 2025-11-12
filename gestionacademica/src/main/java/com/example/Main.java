package com.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("GestionAcademica");
        System.out.println("Conexión establecida, Hibernate debería haber creado las tablas.");
        emf.close();
    }
}
