package com.novasalud.backend.dto;

import com.novasalud.backend.enums.EstadoGenerico;

public record MedicoResponse(Long idMedico, Long idUsuario, String nombres, String apellidos,
        String dni, String correo, String telefono, String numeroColegiatura,
        Long especialidadId, String especialidad, Long sedeId, String sede, EstadoGenerico estado) { }
