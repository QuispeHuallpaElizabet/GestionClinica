package com.novasalud.backend.dto;

import java.time.LocalTime;
import com.novasalud.backend.enums.EstadoGenerico;

public record HorarioMedicoResponse(Long idHorario, Long medicoId, String medico,
        Integer diaSemana, LocalTime horaInicio, LocalTime horaFin, EstadoGenerico estado) { }
