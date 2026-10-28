package com.novasalud.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.novasalud.backend.entity.Cita;
import com.novasalud.backend.enums.EstadoCita;

public interface CitaRepository extends JpaRepository<Cita,Long> {
    List<Cita> findByMedicoIdAndFechaCita(Long medicoId, LocalDate fechaCita);
    List<Cita> findByMedicoIdAndFechaCitaBetween(Long medicoId, LocalDate desde, LocalDate hasta);
    List<Cita> findByPacienteId(Long pacienteId);
    boolean existsByMedicoIdAndFechaCitaAndHoraCita(Long medicoId, LocalDate fechaCita, LocalTime horaCita);

    List<Cita> findByPacienteIdOrderByFechaCitaDescHoraCitaDesc(Long pacienteId);

    @Query("select count(c) from Cita c where c.medico.id = :medicoId and c.fechaCita = :fecha "
         + "and c.horaCita = :hora and c.estadoCita <> :excluido and c.idCita <> :excluirCitaId")
    long contarCitasDelMedico(@Param("medicoId") Long medicoId,
                              @Param("fecha") LocalDate fecha,
                              @Param("hora") LocalTime hora,
                              @Param("excluido") EstadoCita excluido,
                              @Param("excluirCitaId") Long excluirCitaId);

    @Query("select count(c) from Cita c where c.paciente.id = :pacienteId and c.fechaCita = :fecha "
         + "and c.horaCita = :hora and c.estadoCita <> :excluido and c.idCita <> :excluirCitaId")
    long contarCitasDelPaciente(@Param("pacienteId") Long pacienteId,
                                @Param("fecha") LocalDate fecha,
                                @Param("hora") LocalTime hora,
                                @Param("excluido") EstadoCita excluido,
                                @Param("excluirCitaId") Long excluirCitaId);
}
