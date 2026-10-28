package com.novasalud.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.novasalud.backend.enums.EstadoCita;

public record HistorialCitaResponse(
        Long id,
        Long citaId,
        Long usuarioId,
        String usuario,
        String accion,
        LocalDateTime fechaHora,
        EstadoCita estadoAnterior,
        EstadoCita estadoNuevo,
        LocalDate fechaAnterior,
        LocalTime horaAnterior,
        LocalDate fechaNueva,
        LocalTime horaNueva,
        String observacion) {
}