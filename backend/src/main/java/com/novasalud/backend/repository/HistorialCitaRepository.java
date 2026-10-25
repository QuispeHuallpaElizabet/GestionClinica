package com.novasalud.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.novasalud.backend.entity.HistorialCita;

public interface HistorialCitaRepository extends JpaRepository<HistorialCita, Long>{
    
} 