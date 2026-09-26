package com.novasalud.backend.models;

public class Sede {

    private Integer idSede;
    private String  nombre;
    private String  direccion;
    private String  telefono;
    private Boolean estado;

    public Sede() {
    }

    public Sede(Integer idSede, String nombre, String direccion, String telefono, Boolean estado) {
        this.idSede    = idSede;
        this.nombre    = nombre;
        this.direccion = direccion;
        this.telefono  = telefono;
        this.estado    = estado;
    }

    public Integer getIdSede() {
        return idSede;
    }

    public void setIdSede(Integer idSede) {
        this.idSede = idSede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    
}