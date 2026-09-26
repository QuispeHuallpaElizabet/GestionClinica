package com.novasalud.backend.models;

public class Cita {

    private Integer idCita;
    private Integer idPaciente;
    private Integer idMedico;
    private Integer idEspecialidad;
    private Integer idSede;
    private String  fechaCita;        // "YYYY-MM-DD"
    private String  horaCita;         // "HH:mm"
    private String  estado;           // Pendiente | Confirmada | Atendida | Cancelada | No asistió
    private String  motivoCita;
    private String  fechaCreacion;    // ISO
    private String  fechaActualizacion;
    private String  notaAtencion;     // usado por el panel médico (RF13)

    public Cita() {
    }

    public Cita(Integer idCita, Integer idPaciente, Integer idMedico,
                Integer idEspecialidad, Integer idSede,
                String fechaCita, String horaCita, String estado,
                String motivoCita, String fechaCreacion,
                String fechaActualizacion, String notaAtencion) {
        this.idCita              = idCita;
        this.idPaciente          = idPaciente;
        this.idMedico            = idMedico;
        this.idEspecialidad      = idEspecialidad;
        this.idSede              = idSede;
        this.fechaCita           = fechaCita;
        this.horaCita            = horaCita;
        this.estado              = estado;
        this.motivoCita          = motivoCita;
        this.fechaCreacion       = fechaCreacion;
        this.fechaActualizacion  = fechaActualizacion;
        this.notaAtencion        = notaAtencion;
    }

    public Integer getIdCita()                              { return idCita; }
    public void    setIdCita(Integer idCita)                { this.idCita = idCita; }

    public Integer getIdPaciente()                          { return idPaciente; }
    public void    setIdPaciente(Integer idPaciente)        { this.idPaciente = idPaciente; }

    public Integer getIdMedico()                            { return idMedico; }
    public void    setIdMedico(Integer idMedico)            { this.idMedico = idMedico; }

    public Integer getIdEspecialidad()                      { return idEspecialidad; }
    public void    setIdEspecialidad(Integer idEspecialidad){ this.idEspecialidad = idEspecialidad; }

    public Integer getIdSede()                              { return idSede; }
    public void    setIdSede(Integer idSede)                { this.idSede = idSede; }

    public String  getFechaCita()                           { return fechaCita; }
    public void    setFechaCita(String fechaCita)           { this.fechaCita = fechaCita; }

    public String  getHoraCita()                            { return horaCita; }
    public void    setHoraCita(String horaCita)             { this.horaCita = horaCita; }

    public String  getEstado()                              { return estado; }
    public void    setEstado(String estado)                 { this.estado = estado; }

    public String  getMotivoCita()                          { return motivoCita; }
    public void    setMotivoCita(String motivoCita)         { this.motivoCita = motivoCita; }

    public String  getFechaCreacion()                       { return fechaCreacion; }
    public void    setFechaCreacion(String fechaCreacion)   { this.fechaCreacion = fechaCreacion; }

    public String  getFechaActualizacion()                  { return fechaActualizacion; }
    public void    setFechaActualizacion(String f)          { this.fechaActualizacion = f; }

    public String  getNotaAtencion()                        { return notaAtencion; }
    public void    setNotaAtencion(String notaAtencion)     { this.notaAtencion = notaAtencion; }
}