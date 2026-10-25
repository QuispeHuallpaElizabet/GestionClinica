package com.novasalud.backend.dto;
/**
 * DTO compuesto utilizado por el Administrador para crear un Médico de forma integral.
 * Vincula los datos personales base con los datos profesionales (especialidad, sede y colegiatura).
 * La contraseña es generada automáticamente por el backend.
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MedicoRegistroRequest {

    @Valid
    @NotNull(message = "Los datos personales son obligatorios")
    private DatosPersonalesRequest datosPersonales;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;

    @NotNull(message = "El ID de la sede es obligatorio")
    private Long sedeId;

    @NotBlank(message = "El número de colegiatura es obligatorio")
    @Size(min = 5, max = 20, message = "La colegiatura debe tener entre 5 y 20 caracteres")
    private String numeroColegiatura;

    public MedicoRegistroRequest() { }

    public MedicoRegistroRequest(DatosPersonalesRequest datosPersonales, Long especialidadId, 
                                Long sedeId, String numeroColegiatura) {
        this.datosPersonales = datosPersonales;
        this.especialidadId = especialidadId;
        this.sedeId = sedeId;
        this.numeroColegiatura = numeroColegiatura;
    }

    public DatosPersonalesRequest getDatosPersonales() { return datosPersonales; }
    public void setDatosPersonales(DatosPersonalesRequest datosPersonales) { this.datosPersonales = datosPersonales; }

    public Long getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Long especialidadId) { this.especialidadId = especialidadId; }

    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    public String getNumeroColegiatura() { return numeroColegiatura; }
    public void setNumeroColegiatura(String numeroColegiatura) { this.numeroColegiatura = numeroColegiatura; }
}   