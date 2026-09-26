package com.novasalud.backend.models;

public class Especialidad {

    private Integer idEspecialidad;
    private String  nombre;
    private Integer duracionCitaMin;
    private Boolean estado;

    public Especialidad() {
    }

    public Especialidad(Integer idEspecialidad, String nombre,Integer duracionCitaMin, Boolean estado) {
        this.idEspecialidad  = idEspecialidad;
        this.nombre          = nombre;
        this.duracionCitaMin = duracionCitaMin;
        this.estado          = estado;
    }

    public Integer getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(Integer idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDuracionCitaMin() {
        return duracionCitaMin;
    }

    public void setDuracionCitaMin(Integer duracionCitaMin) {
        this.duracionCitaMin = duracionCitaMin;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
}