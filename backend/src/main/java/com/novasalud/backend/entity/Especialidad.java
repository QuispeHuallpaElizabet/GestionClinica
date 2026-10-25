package com.novasalud.backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.novasalud.backend.enums.EstadoGenerico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idEspecialidad;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @NotNull(message = "La duración de la cita es obligatoria")
    @Min(value = 5, message = "La duración mínima de la cita es 5 minutos")
    @Max(value = 240, message = "La duración máxima de la cita es 240 minutos")
    @Column(nullable = false)
    private Integer duracionCitaMin;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    private EstadoGenerico estado = EstadoGenerico.ACTIVO;

    @OneToMany(mappedBy = "especialidad")
    private List<Medico> medicos = new ArrayList<>();

    public Especialidad() { }

    public Especialidad(String nombre, Integer duracionCitaMin, EstadoGenerico estado) {
        this.nombre = nombre;
        this.duracionCitaMin = duracionCitaMin;
        this.estado = estado;
    }

    public Long getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Long idEspecialidad) { this.idEspecialidad = idEspecialidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getDuracionCitaMin() { return duracionCitaMin; }
    public void setDuracionCitaMin(Integer duracionCitaMin) { this.duracionCitaMin = duracionCitaMin; }

    public EstadoGenerico getEstado() { return estado; }
    public void setEstado(EstadoGenerico estado) { this.estado = estado; }
}