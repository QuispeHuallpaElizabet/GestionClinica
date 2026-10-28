package com.novasalud.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.novasalud.backend.entity.HistorialCita;

public interface HistorialCitaRepository extends JpaRepository<HistorialCita, Long>{

     @Query("select h from HistorialCita h where h.cita.idCita = :citaId "
         + "order by h.fechaHora asc, h.idHistorial asc")
    List<HistorialCita> buscarPorCita(@Param("citaId") Long citaId);
    
} 