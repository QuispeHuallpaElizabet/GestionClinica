package com.novasalud.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.HorarioMedicoRequest;
import com.novasalud.backend.dto.HorarioMedicoResponse;
import com.novasalud.backend.entity.HorarioMedico;
import com.novasalud.backend.entity.Medico;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.HorarioMedicoRepository;
import com.novasalud.backend.repository.MedicoRepository;

@Service
public class HorarioMedicoService {

    private final HorarioMedicoRepository horarioRepository;
    private final MedicoRepository medicoRepository;

    public HorarioMedicoService(HorarioMedicoRepository horarioRepository, MedicoRepository medicoRepository) {
        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
    }

    @Transactional
    public HorarioMedicoResponse crear(HorarioMedicoRequest request) {
        validarRango(request);
        Medico medico = medicoActivo(request.getMedicoId());
        validarCruce(medico.getId(), request, null);
        HorarioMedico horario = new HorarioMedico(medico, request.getDiaSemana(),
                request.getHoraInicio(), request.getHoraFin());
        horario.setEstado(EstadoGenerico.ACTIVO);
        return respuesta(horarioRepository.save(horario));
    }

    @Transactional(readOnly = true)
    public List<HorarioMedicoResponse> listarPorMedico(Long medicoId) {
        if (!medicoRepository.existsById(medicoId)) throw new RuntimeException("Médico no encontrado");
        return horarioRepository.findByMedicoId(medicoId).stream().map(this::respuesta).toList();
    }

    @Transactional
    public HorarioMedicoResponse actualizar(Long id, HorarioMedicoRequest request) {
        validarRango(request);
        HorarioMedico horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        Medico medico = medicoActivo(request.getMedicoId());
        validarCruce(medico.getId(), request, id);
        horario.setMedico(medico);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        return respuesta(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioMedicoResponse cambiarEstado(Long id, boolean activo) {
        HorarioMedico horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        if (activo) {
            if (horario.getMedico().getEstado() != EstadoGenerico.ACTIVO) {
                throw new RuntimeException("No se puede activar el horario de un médico inactivo");
            }
            validarCruce(horario.getMedico().getId(), new HorarioMedicoRequest(horario.getMedico().getId(),
                    horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin()), id);
        }
        horario.setEstado(activo ? EstadoGenerico.ACTIVO : EstadoGenerico.INACTIVO);
        return respuesta(horarioRepository.save(horario));
    }

    private Medico medicoActivo(Long id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        if (medico.getEstado() != EstadoGenerico.ACTIVO) throw new RuntimeException("El médico está inactivo");
        return medico;
    }

    private void validarRango(HorarioMedicoRequest request) {
        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validarCruce(Long medicoId, HorarioMedicoRequest request, Long ignorarId) {
        boolean cruce = horarioRepository.findByMedicoIdAndDiaSemanaAndEstado(
                medicoId, request.getDiaSemana(), EstadoGenerico.ACTIVO).stream()
                .filter(h -> ignorarId == null || !h.getIdHorario().equals(ignorarId))
                .anyMatch(h -> request.getHoraInicio().isBefore(h.getHoraFin())
                        && request.getHoraFin().isAfter(h.getHoraInicio()));
        if (cruce) throw new RuntimeException("El horario se cruza con otro bloque activo del médico");
    }

    private HorarioMedicoResponse respuesta(HorarioMedico h) {
        String nombre = h.getMedico().getUsuario().getNombres() + " "
                + h.getMedico().getUsuario().getApellidos();
        return new HorarioMedicoResponse(h.getIdHorario(), h.getMedico().getId(), nombre,
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), h.getEstado());
    }
}
