package com.novasalud.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.Medico;

public interface MedicoRepository extends JpaRepository<Medico,Long>{
    Optional<Medico> findByUsuarioId(Long usuarioId);
    Optional<Medico> findByNumeroColegiatura(String numeroColegiatura);
    boolean existsByNumeroColegiatura(String numeroColegiatura);
    
} 
