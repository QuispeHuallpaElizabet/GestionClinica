package com.novasalud.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.HorarioMedico;
import com.novasalud.backend.enums.EstadoGenerico;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico,Long>{
    List<HorarioMedico> findByMedicoId(Long medicoId);
    List<HorarioMedico> findByMedicoIdAndDiaSemana(Long medicoId, Integer diaSemana);
    List<HorarioMedico> findByMedicoIdAndDiaSemanaAndEstado(Long medicoId, Integer diaSemana, EstadoGenerico estado);
    List<HorarioMedico> findByMedicoIdAndEstado(Long medicoId, EstadoGenerico estado);
}
