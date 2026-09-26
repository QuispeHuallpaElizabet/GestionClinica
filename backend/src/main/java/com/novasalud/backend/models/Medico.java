package com.novasalud.backend.models;

public class Medico {

    private Integer idUsuario;
    private String  nombres;
    private String  apellidos;
    private String  dni;
    private String  telefono;
    private String  correo;
    private String  contrasena;
    private Boolean activo;

    private Integer idEspecialidad;
    private Integer idSede;
    private String  numeroColegiatura;

    public Medico() {
    }

    public Medico(Integer idUsuario, String nombres, String apellidos,String dni, String telefono, String correo, String contrasena,Boolean activo, Integer idEspecialidad, Integer idSede,String numeroColegiatura) {
        this.idUsuario         = idUsuario;
        this.nombres           = nombres;
        this.apellidos         = apellidos;
        this.dni               = dni;
        this.telefono          = telefono;
        this.correo            = correo;
        this.contrasena        = contrasena;
        this.activo            = activo;
        this.idEspecialidad    = idEspecialidad;
        this.idSede            = idSede;
        this.numeroColegiatura = numeroColegiatura;
    }

    public Integer getIdUsuario()                           { return idUsuario; }
    public void    setIdUsuario(Integer idUsuario)          { this.idUsuario = idUsuario; }

    public String  getNombres()                             { return nombres; }
    public void    setNombres(String nombres)               { this.nombres = nombres; }

    public String  getApellidos()                           { return apellidos; }
    public void    setApellidos(String apellidos)           { this.apellidos = apellidos; }

    public String  getDni()                                 { return dni; }
    public void    setDni(String dni)                       { this.dni = dni; }

    public String  getTelefono()                            { return telefono; }
    public void    setTelefono(String telefono)             { this.telefono = telefono; }

    public String  getCorreo()                              { return correo; }
    public void    setCorreo(String correo)                 { this.correo = correo; }

    public String  getContrasena()                          { return contrasena; }
    public void    setContrasena(String contrasena)         { this.contrasena = contrasena; }

    public Boolean getActivo()                              { return activo; }
    public void    setActivo(Boolean activo)                { this.activo = activo; }

    public Integer getIdEspecialidad()                      { return idEspecialidad; }
    public void    setIdEspecialidad(Integer idEspecialidad){ this.idEspecialidad = idEspecialidad; }

    public Integer getIdSede()                              { return idSede; }
    public void    setIdSede(Integer idSede)                { this.idSede = idSede; }

    public String  getNumeroColegiatura()                   { return numeroColegiatura; }
    public void    setNumeroColegiatura(String n)           { this.numeroColegiatura = n; }
}