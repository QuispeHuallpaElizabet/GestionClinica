package com.novasalud.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.novasalud.backend.enums.EstadoCita;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String paciente,
        String pacienteDni,
        Long medicoId,
        String medico,
        Long especialidadId,
        String especialidad,
        Long sedeId,
        String sede,
        LocalDate fechaCita,
        LocalTime horaCita,
        EstadoCita estado,
        String motivoCita,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {
}
