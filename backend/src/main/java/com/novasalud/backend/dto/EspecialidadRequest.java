package com.novasalud.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EspecialidadRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "La duración de la cita es obligatoria")
    @Min(value = 5, message = "La duración mínima de la cita es 5 minutos")
    @Max(value = 240, message = "La duración máxima de la cita es 240 minutos")
    private Integer duracionCitaMin;

    public EspecialidadRequest() { }

    public EspecialidadRequest(String nombre, Integer duracionCitaMin) {
        this.nombre = nombre;
        this.duracionCitaMin = duracionCitaMin;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getDuracionCitaMin() { return duracionCitaMin; }
    public void setDuracionCitaMin(Integer duracionCitaMin) { this.duracionCitaMin = duracionCitaMin; }
}