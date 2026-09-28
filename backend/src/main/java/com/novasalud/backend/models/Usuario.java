package com.novasalud.backend.models;

import java.util.List;

public class Usuario {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String correo;
    private String contrasena;
    private String estado;
    private boolean requiereCambioContrasena;
    private List<Rol> roles;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nombres, String apellidos,
                String dni, String telefono, String correo,
                String contrasena, String estado,
                boolean requiereCambioContrasena,
                List<Rol> roles) {

        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
        this.requiereCambioContrasena = requiereCambioContrasena;
        this.roles = roles;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isRequiereCambioContrasena() {
        return requiereCambioContrasena;
    }

    public void setRequiereCambioContrasena(boolean requiereCambioContrasena) {
        this.requiereCambioContrasena = requiereCambioContrasena;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }
}
