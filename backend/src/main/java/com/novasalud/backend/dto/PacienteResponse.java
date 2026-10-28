package com.novasalud.backend.dto;

import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        Long usuarioId,
        String nombres,
        String apellidos,
        String dni,
        String telefono,
        String correo,
        LocalDate fechaNacimiento,
        String numeroHistorialClinica,
        String grupoSanguineo,
        String contactoEmergencia) {
}