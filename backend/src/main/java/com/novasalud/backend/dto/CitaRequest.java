package com.novasalud.backend.dto;
/**
 * DTO utilizado para agendar o reprogramar una cita médica.
 * Recibe las llaves foráneas (IDs) de las entidades relacionadas, junto con la fecha, hora y motivo.
 * No incluye el estado, ya que toda cita nueva nace como "PENDIENTE".
 */
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CitaRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long medicoId;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;

    @NotNull(message = "El ID de la sede es obligatorio")
    private Long sedeId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDate fechaCita;

    @NotNull(message = "La hora de la cita es obligatoria")
    private LocalTime horaCita;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Size(max = 500, message = "El motivo no debe exceder 500 caracteres")
    private String motivoCita;

    public CitaRequest() { }

    public CitaRequest(Long pacienteId, Long medicoId, Long especialidadId, Long sedeId, 
                        LocalDate fechaCita, LocalTime horaCita, String motivoCita) {
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.especialidadId = especialidadId;
        this.sedeId = sedeId;
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.motivoCita = motivoCita;
    }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public Long getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Long especialidadId) { this.especialidadId = especialidadId; }

    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public LocalTime getHoraCita() { return horaCita; }
    public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }

    public String getMotivoCita() { return motivoCita; }
    public void setMotivoCita(String motivoCita) { this.motivoCita = motivoCita; }
}