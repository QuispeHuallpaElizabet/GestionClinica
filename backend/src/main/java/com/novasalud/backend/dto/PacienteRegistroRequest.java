package com.novasalud.backend.dto;
/**
 * DTO utilizado para el auto-registro público de pacientes.
 * Compone los datos personales base junto con la contraseña que el propio paciente elige.
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PacienteRegistroRequest {

    @Valid
    @NotNull(message = "Los datos personales son obligatorios")
    private DatosPersonalesRequest datosPersonales;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String contrasena;

    public PacienteRegistroRequest() { }

    public PacienteRegistroRequest  (DatosPersonalesRequest datosPersonales, String contrasena) {
        this.datosPersonales = datosPersonales;
        this.contrasena = contrasena;
    }

    public DatosPersonalesRequest getDatosPersonales() { return datosPersonales; }
    public void setDatosPersonales(DatosPersonalesRequest datosPersonales) { this.datosPersonales = datosPersonales; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}