package com.novasalud.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.novasalud.backend.entity.HorarioMedico;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico,Long>{
    List<HorarioMedico> findByMedicoId(Long medicoId);
    List<HorarioMedico> findByMedicoIdAndDiaSemana(Long medicoId, Integer diaSemana);
}
