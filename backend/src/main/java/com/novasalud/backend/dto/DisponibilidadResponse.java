package com.novasalud.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadResponse(Long medicoId, String medico, Long especialidadId,
        String especialidad, Long sedeId, String sede, LocalDate fecha,
        LocalTime horaInicio, LocalTime horaFin) { }
