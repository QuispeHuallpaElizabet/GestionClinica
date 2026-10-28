package com.novasalud.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.RolRequest;
import com.novasalud.backend.dto.RolResponse;
import com.novasalud.backend.entity.Rol;
import com.novasalud.backend.repository.RolRepository;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) { this.rolRepository = rolRepository; }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional
    public RolResponse crear(RolRequest request) {
        String nombre = request.getNombre().trim().toUpperCase();
        if (rolRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Ya existe un rol con el nombre " + nombre);
        }
        return respuesta(rolRepository.save(new Rol(nombre)));
    }

    private RolResponse respuesta(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombre());
    }
}
