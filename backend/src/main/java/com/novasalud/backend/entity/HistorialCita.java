package com.novasalud.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import com.novasalud.backend.enums.EstadoCita;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "historial_cita")
public class HistorialCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorial;

    @NotNull(message = "La cita es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @NotNull(message = "El usuario que realiza la acción es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 50, message = "La acción no debe exceder 50 caracteres")
    @Column(nullable = false, length = 50)
    private String accion;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoCita estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoCita estadoNuevo;

    @Column(name = "fecha_anterior")
    private LocalDate fechaAnterior;

    @Column(name = "hora_anterior")
    private LocalTime horaAnterior;

    @Column(name = "fecha_nueva")
    private LocalDate fechaNueva;

    @Column(name = "hora_nueva")
    private LocalTime horaNueva;

    @Size(max = 500, message = "La observación no debe exceder 500 caracteres")
    @Column(length = 500)
    private String observacion;

    public HistorialCita() { }

    public HistorialCita(Cita cita, Usuario usuario, String accion, EstadoCita estadoNuevo, String observacion) {
        this.cita = cita;
        this.usuario = usuario;
        this.accion = accion;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
    }

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public EstadoCita getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoCita estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoCita getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoCita estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public LocalDate getFechaAnterior() { return fechaAnterior; }
    public void setFechaAnterior(LocalDate fechaAnterior) { this.fechaAnterior = fechaAnterior; }

    public LocalTime getHoraAnterior() { return horaAnterior; }
    public void setHoraAnterior(LocalTime horaAnterior) { this.horaAnterior = horaAnterior; }

    public LocalDate getFechaNueva() { return fechaNueva; }
    public void setFechaNueva(LocalDate fechaNueva) { this.fechaNueva = fechaNueva; }

    public LocalTime getHoraNueva() { return horaNueva; }
    public void setHoraNueva(LocalTime horaNueva) { this.horaNueva = horaNueva; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}