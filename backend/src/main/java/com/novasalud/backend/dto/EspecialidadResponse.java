package com.novasalud.backend.dto;

import com.novasalud.backend.enums.EstadoGenerico;

public record EspecialidadResponse(Long idEspecialidad, String nombre, Integer duracionCitaMin,
        EstadoGenerico estado) { }
