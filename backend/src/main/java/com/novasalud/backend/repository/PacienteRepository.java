package com.novasalud.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.novasalud.backend.entity.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente,Long> {
    Optional<Paciente> findByUsuarioId(Long usuarioId);
    Optional<Paciente> findByNumeroHistorialClinica(String numeroHistorialClinica);

    @Query("select p from Paciente p join fetch p.usuario u where u.dni = :dni")
    Optional<Paciente> buscarPorDni(@Param("dni") String dni);

    @Query("select p from Paciente p join fetch p.usuario u "
         + "where lower(concat(u.nombres, ' ', u.apellidos)) like lower(concat('%', :nombre, '%')) "
         + "order by u.apellidos, u.nombres")
    List<Paciente> buscarPorNombre(@Param("nombre") String nombre);
} 
