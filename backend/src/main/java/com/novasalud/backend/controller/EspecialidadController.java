package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.EspecialidadRequest;
import com.novasalud.backend.dto.EspecialidadResponse;
import com.novasalud.backend.service.EspecialidadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/especialidades")
@Validated
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(request));
    }

    @GetMapping
    public List<EspecialidadResponse> listar() { return especialidadService.listar(); }

    @PutMapping("/{id}")
    public EspecialidadResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody EspecialidadRequest request) {
        return especialidadService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public EspecialidadResponse cambiarEstado(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean activo) {
        return especialidadService.cambiarEstado(id, activo);
    }
}
