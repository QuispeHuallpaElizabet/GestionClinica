package com.novasalud.backend.models;

public class Paciente {

    private Integer idUsuario;
    private String  nombres;
    private String  apellidos;
    private String  dni;
    private String  telefono;
    private String  correo;
    private String  contrasena;
    private Boolean activo;

    private String  fechaNac;   // "YYYY-MM-DD"
    private String  direccion;

    public Paciente() {
    }

    public Paciente(Integer idUsuario, String nombres, String apellidos,
                    String dni, String telefono, String correo, String contrasena,
                    Boolean activo, String fechaNac, String direccion) {
        this.idUsuario  = idUsuario;
        this.nombres    = nombres;
        this.apellidos  = apellidos;
        this.dni        = dni;
        this.telefono   = telefono;
        this.correo     = correo;
        this.contrasena = contrasena;
        this.activo     = activo;
        this.fechaNac   = fechaNac;
        this.direccion  = direccion;
    }

    public Integer getIdUsuario()                       { return idUsuario; }
    public void    setIdUsuario(Integer idUsuario)      { this.idUsuario = idUsuario; }

    public String  getNombres()                         { return nombres; }
    public void    setNombres(String nombres)           { this.nombres = nombres; }

    public String  getApellidos()                       { return apellidos; }
    public void    setApellidos(String apellidos)       { this.apellidos = apellidos; }

    public String  getDni()                             { return dni; }
    public void    setDni(String dni)                   { this.dni = dni; }

    public String  getTelefono()                        { return telefono; }
    public void    setTelefono(String telefono)         { this.telefono = telefono; }

    public String  getCorreo()                          { return correo; }
    public void    setCorreo(String correo)             { this.correo = correo; }

    public String  getContrasena()                      { return contrasena; }
    public void    setContrasena(String contrasena)     { this.contrasena = contrasena; }

    public Boolean getActivo()                          { return activo; }
    public void    setActivo(Boolean activo)            { this.activo = activo; }

    public String  getFechaNac()                        { return fechaNac; }
    public void    setFechaNac(String fechaNac)         { this.fechaNac = fechaNac; }

    public String  getDireccion()                       { return direccion; }
    public void    setDireccion(String direccion)       { this.direccion = direccion; }
}