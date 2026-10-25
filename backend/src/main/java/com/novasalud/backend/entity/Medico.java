package com.novasalud.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity 
@Table (name = "medico")
public class Medico {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn (name="usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "especialidad_id")
    private Especialidad especialidad;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn (name="sede_id")
    private Sede sede;

    @NotBlank(message = "El número de colegiatura es obligatorio")
    @Size(min = 5, max = 20, message = "La colegiatura debe tener entre 5 y 20 caracteres")
    @Column (name = "numero_colegiatura", nullable = false, unique = true, length = 20)
    private String  numeroColegiatura;

    public Medico() {
    }

    public Medico(Usuario usuario, Especialidad especialidad, Sede sede, String numeroColegiatura) {
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.sede = sede;
        this.numeroColegiatura = numeroColegiatura;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Sede getSede() { return sede; }
    public void setSede(Sede sede) { this.sede = sede; }

    public String getNumeroColegiatura() { return numeroColegiatura; }
    public void setNumeroColegiatura(String numeroColegiatura) { this.numeroColegiatura = numeroColegiatura; } 
}