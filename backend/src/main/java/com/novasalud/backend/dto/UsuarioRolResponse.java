package com.novasalud.backend.dto;

import com.novasalud.backend.enums.EstadoGenerico;

public record UsuarioRolResponse(Long idUsuarioRol, Long usuarioId, Long rolId, String rol,
        EstadoGenerico estado) { }
