package com.novasalud.backend.dto;

import jakarta.validation.constraints.NotNull;

public class UsuarioRolRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del rol es obligatorio")
    private Long rolId;

    public UsuarioRolRequest() { }

    public UsuarioRolRequest(Long usuarioId, Long rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}