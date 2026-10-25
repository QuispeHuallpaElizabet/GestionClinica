package com.novasalud.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente,Long> {
    Optional<Paciente> findByUsuarioId(Long usuarioId);
    Optional<Paciente> findByNumeroHistorialClinica(String numeroHistorialClinica);
} 
