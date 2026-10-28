package com.novasalud.backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.HistorialCitaResponse;
import com.novasalud.backend.entity.Cita;
import com.novasalud.backend.entity.HistorialCita;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.enums.EstadoCita;
import com.novasalud.backend.repository.CitaRepository;
import com.novasalud.backend.repository.HistorialCitaRepository;


@Service
public class HistorialCitaService {

    private final HistorialCitaRepository historialCitaRepository;
    private final CitaRepository citaRepository;

    public HistorialCitaService(HistorialCitaRepository historialCitaRepository,
                                CitaRepository citaRepository) {
        this.historialCitaRepository = historialCitaRepository;
        this.citaRepository = citaRepository;
    }

    @Transactional
    public HistorialCita registrar(Cita cita, Usuario usuario, String accion,
                                   EstadoCita estadoAnterior, EstadoCita estadoNuevo,
                                   LocalDate fechaAnterior, LocalTime horaAnterior,
                                   LocalDate fechaNueva, LocalTime horaNueva,
                                   String observacion) {

        String obs = (observacion == null || observacion.isBlank()) ? null : observacion.trim();

        HistorialCita historial = new HistorialCita(cita, usuario, accion, estadoNuevo, obs);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setFechaAnterior(fechaAnterior);
        historial.setHoraAnterior(horaAnterior);
        historial.setFechaNueva(fechaNueva);
        historial.setHoraNueva(horaNueva);

        return historialCitaRepository.save(historial);
    }

    @Transactional(readOnly = true)
    public List<HistorialCitaResponse> listarPorCita(Long citaId) {
        if (!citaRepository.existsById(citaId)) {
            throw new RuntimeException("Cita no encontrada con ID: " + citaId);
        }
        return historialCitaRepository.buscarPorCita(citaId).stream()
                .map(this::toResponse)
                .toList();
    }

    private HistorialCitaResponse toResponse(HistorialCita h) {
        Usuario u = h.getUsuario();
        return new HistorialCitaResponse(
                h.getIdHistorial(),
                h.getCita().getIdCita(),
                u.getId(),
                u.getNombres() + " " + u.getApellidos(),
                h.getAccion(),
                h.getFechaHora(),
                h.getEstadoAnterior(),
                h.getEstadoNuevo(),
                h.getFechaAnterior(),
                h.getHoraAnterior(),
                h.getFechaNueva(),
                h.getHoraNueva(),
                h.getObservacion());
    }
}