package com.novasalud.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.Sede;

public interface SedeRepository extends JpaRepository<Sede, Long> {
    boolean existsByNombre(String nombre);
    
}
