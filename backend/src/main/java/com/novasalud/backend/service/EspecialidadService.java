package com.novasalud.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.EspecialidadRequest;
import com.novasalud.backend.dto.EspecialidadResponse;
import com.novasalud.backend.entity.Especialidad;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.EspecialidadRepository;

@Service
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar() {
        return especialidadRepository.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest request) {
        String nombre = request.getNombre().trim();
        if (existsByNombre(nombre, null)) {
            throw new RuntimeException("Ya existe una especialidad con el nombre " + nombre);
        }
        Especialidad especialidad = new Especialidad(nombre, request.getDuracionCitaMin(), EstadoGenerico.ACTIVO);
        return respuesta(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
        String nombre = request.getNombre().trim();
        if (existsByNombre(nombre, id)) {
            throw new RuntimeException("Ya existe otra especialidad con el nombre: " + nombre);
        }
        especialidad.setNombre(nombre);
        especialidad.setDuracionCitaMin(request.getDuracionCitaMin());
        return respuesta(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse cambiarEstado(Long id, boolean activo) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada con ID: " + id));
        especialidad.setEstado(activo ? EstadoGenerico.ACTIVO : EstadoGenerico.INACTIVO);
        return respuesta(especialidadRepository.save(especialidad));
    }

    // EspecialidadRepository no expone existsByNombre: se valida sobre el catálogo (excluyendo el propio id al actualizar)
    private boolean existsByNombre(String nombre, Long idExcluido) {
        return especialidadRepository.findAll().stream()
                .anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre)
                        && !e.getIdEspecialidad().equals(idExcluido));
    }

    private EspecialidadResponse respuesta(Especialidad e) {
        return new EspecialidadResponse(e.getIdEspecialidad(), e.getNombre(), e.getDuracionCitaMin(), e.getEstado());
    }
}
