package com.novasalud.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import com.novasalud.backend.enums.EstadoCita;

public record AgendaCitaResponse(Long citaId, LocalDate fecha, LocalTime hora,
        String paciente, String dniPaciente, String especialidad, String sede,
        EstadoCita estado, String motivo) { }
