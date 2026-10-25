package com.novasalud.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.novasalud.backend.entity.Rol;
import com.novasalud.backend.repository.RolRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    public DataInitializer(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (rolRepository.count() == 0) {
            
            rolRepository.save(new Rol("ADMINISTRADOR")); // ID 1
            rolRepository.save(new Rol("MEDICO"));        // ID 2
            rolRepository.save(new Rol("PACIENTE"));      // ID 3
            rolRepository.save(new Rol("RECEPCIONISTA")); // ID 4
            
            System.out.println("Roles iniciales creados en la base de datos correctamente.");
        } else {
            System.out.println("Los roles ya existen. Saltando inicialización.");
        }
    }
}