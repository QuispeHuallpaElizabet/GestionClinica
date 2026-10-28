package com.novasalud.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MedicoActualizacionRequest {
    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;
    @NotNull(message = "El ID de la sede es obligatorio")
    private Long sedeId;
    @NotBlank(message = "El número de colegiatura es obligatorio")
    @Size(min = 5, max = 20, message = "La colegiatura debe tener entre 5 y 20 caracteres")
    private String numeroColegiatura;
    public Long getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Long especialidadId) { this.especialidadId = especialidadId; }
    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }
    public String getNumeroColegiatura() { return numeroColegiatura; }
    public void setNumeroColegiatura(String numeroColegiatura) { this.numeroColegiatura = numeroColegiatura; }
}
