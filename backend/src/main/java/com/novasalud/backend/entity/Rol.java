package com.novasalud.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity 
@Table (name="rol")
public class Rol {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @OneToMany (mappedBy = "rol")
    private List<UsuarioRol> usuarioRoles = new ArrayList<>();

    public Rol() { }
    
    public Rol(String nombre) {
        this.nombre = nombre;
    }  

    public Long getId() { return id;}
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<UsuarioRol> getUsuarioRoles() { return usuarioRoles; }
    public void setUsuarioRoles(List<UsuarioRol> usuarioRoles) { this.usuarioRoles = usuarioRoles; }
    
}
