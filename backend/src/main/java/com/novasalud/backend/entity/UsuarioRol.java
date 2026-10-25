package com.novasalud.backend.entity;

import com.novasalud.backend.enums.EstadoGenerico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name="usuario_rol")
public class UsuarioRol {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name="usuario_id")
    private Usuario usuario;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name="rol_id")
    private Rol rol;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false, length = 20)
    private EstadoGenerico estado = EstadoGenerico.ACTIVO;

    public UsuarioRol(){ }

    public UsuarioRol(Usuario usuario, Rol rol, EstadoGenerico estado) {
        this.usuario = usuario;
        this.rol = rol;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public EstadoGenerico getEstado() { return estado; }
    public void setEstado(EstadoGenerico estado) { this.estado = estado; }
    
}
