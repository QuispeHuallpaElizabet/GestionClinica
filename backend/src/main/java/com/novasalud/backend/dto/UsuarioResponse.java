package com.novasalud.backend.dto;

import java.time.LocalDate;
import java.util.List;

import com.novasalud.backend.enums.EstadoGenerico;

public record UsuarioResponse(Long idUsuario, String nombres, String apellidos, String dni,
        String telefono, String correo, LocalDate fechaNacimiento, EstadoGenerico estado,
        List<String> roles) { }
