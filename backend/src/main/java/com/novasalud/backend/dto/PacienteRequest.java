package com.novasalud.backend.dto;
/**
 * DTO utilizado para gestionar el perfil clínico del paciente.
 * Se usa para asignar o actualizar el número de historial, grupo sanguíneo y contacto de emergencia.
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PacienteRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El número de historial clínico es obligatorio")
    @Size(max = 20, message = "El número de historial clínico no debe exceder 20 caracteres")
    private String numeroHistorialClinica;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "El grupo sanguíneo debe ser A+, A-, B+, B-, AB+, AB-, O+ u O-")
    private String grupoSanguineo;

    @Pattern(regexp = "\\d{9}", message = "El teléfono de emergencia debe tener exactamente 9 dígitos")
    private String contactoEmergencia;

    public PacienteRequest() { }

    public PacienteRequest(Long usuarioId, String numeroHistorialClinica, String grupoSanguineo, String contactoEmergencia) {
        this.usuarioId = usuarioId;
        this.numeroHistorialClinica = numeroHistorialClinica;
        this.grupoSanguineo = grupoSanguineo;
        this.contactoEmergencia = contactoEmergencia;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNumeroHistorialClinica() { return numeroHistorialClinica; }
    public void setNumeroHistorialClinica(String numeroHistorialClinica) { this.numeroHistorialClinica = numeroHistorialClinica; }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }
}