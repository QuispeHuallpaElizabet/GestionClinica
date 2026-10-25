package com.novasalud.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank(message = "El número de historial clínico es obligatorio")
    @Size(max = 20, message = "El número de historial clínico no debe exceder 20 caracteres")
    @Column(name = "num_historial_clinica", unique = true, nullable = false, length = 20)
    private String numeroHistorialClinica;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "El grupo sanguíneo debe ser A+, A-, B+, B-, AB+, AB-, O+ u O-")
    @Column(name = "grupo_sanguineo", length = 5)
    private String grupoSanguineo;

    @Pattern(regexp = "\\d{9}", message = "El teléfono de emergencia debe tener exactamente 9 dígitos")
    @Column(name = "contacto_emergencia", length = 9)
    private String contactoEmergencia;

    public Paciente() { }

    public Paciente(Usuario usuario, String numeroHistorialClinica, String grupoSanguineo, String contactoEmergencia) {
        this.usuario = usuario;
        this.numeroHistorialClinica = numeroHistorialClinica;
        this.grupoSanguineo = grupoSanguineo;
        this.contactoEmergencia = contactoEmergencia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNumeroHistorialClinica() { return numeroHistorialClinica; }
    public void setNumeroHistorialClinica(String numeroHistorialClinica) { this.numeroHistorialClinica = numeroHistorialClinica; }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }
}