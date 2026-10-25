package com.novasalud.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.Cita;

public interface CitaRepository extends JpaRepository<Cita,Long> {
    List<Cita> findByMedicoIdAndFechaCita(Long medicoId, LocalDate fechaCita);
    List<Cita> findByPacienteId(Long pacienteId);
    boolean existsByMedicoIdAndFechaCitaAndHoraCita(Long medicoId, LocalDate fechaCita, LocalTime horaCita);
}