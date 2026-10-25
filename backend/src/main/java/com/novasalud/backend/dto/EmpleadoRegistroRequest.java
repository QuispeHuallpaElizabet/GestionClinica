package com.novasalud.backend.dto;
/**
 * DTO utilizado por el Administrador para registrar personal no médico (Recepcionistas, Admins)
 * Compone los datos personales base junto con el ID del rol que tendrá el empleado.
 * La contraseña es generada automáticamente por el backend.
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class EmpleadoRegistroRequest {

    @Valid
    @NotNull(message = "Los datos personales son obligatorios")
    private DatosPersonalesRequest datosPersonales;

    @NotNull(message = "El ID del rol es obligatorio")
    private Long rolId;

    public EmpleadoRegistroRequest() { }

    public EmpleadoRegistroRequest(DatosPersonalesRequest datosPersonales, Long rolId) {
        this.datosPersonales = datosPersonales;
        this.rolId = rolId;
    }

    public DatosPersonalesRequest getDatosPersonales() { return datosPersonales; }
    public void setDatosPersonales(DatosPersonalesRequest datosPersonales) { this.datosPersonales = datosPersonales; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}