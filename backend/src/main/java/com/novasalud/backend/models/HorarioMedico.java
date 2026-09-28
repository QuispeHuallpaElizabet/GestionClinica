package com.novasalud.backend.models;

public class HorarioMedico {

    private Integer idHorario;
    private Integer idMedico;
    private Integer diaSemana;   // 1 = Lunes … 7 = Domingo
    private String  horaInicio;  // "HH:mm"
    private String  horaFin;     // "HH:mm"
    private Boolean estado;

    public HorarioMedico() {
    }

    public HorarioMedico(Integer idHorario, Integer idMedico, Integer diaSemana,
                         String horaInicio, String horaFin, Boolean estado) {
        this.idHorario  = idHorario;
        this.idMedico   = idMedico;
        this.diaSemana  = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin    = horaFin;
        this.estado     = estado;
    }

    public Integer getIdHorario()                       { return idHorario; }
    public void    setIdHorario(Integer idHorario)      { this.idHorario = idHorario; }

    public Integer getIdMedico()                        { return idMedico; }
    public void    setIdMedico(Integer idMedico)        { this.idMedico = idMedico; }

    public Integer getDiaSemana()                       { return diaSemana; }
    public void    setDiaSemana(Integer diaSemana)      { this.diaSemana = diaSemana; }

    public String  getHoraInicio()                      { return horaInicio; }
    public void    setHoraInicio(String horaInicio)     { this.horaInicio = horaInicio; }

    public String  getHoraFin()                         { return horaFin; }
    public void    setHoraFin(String horaFin)           { this.horaFin = horaFin; }

    public Boolean getEstado()                          { return estado; }
    public void    setEstado(Boolean estado)            { this.estado = estado; }
}