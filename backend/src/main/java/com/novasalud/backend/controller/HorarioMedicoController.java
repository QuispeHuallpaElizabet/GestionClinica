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

import com.novasalud.backend.dto.HorarioMedicoRequest;
import com.novasalud.backend.dto.HorarioMedicoResponse;
import com.novasalud.backend.service.HorarioMedicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horarios")
@Validated
public class HorarioMedicoController {

    private final HorarioMedicoService horarioService;

    public HorarioMedicoController(HorarioMedicoService horarioService) { this.horarioService = horarioService; }

    @PostMapping
    public ResponseEntity<HorarioMedicoResponse> crear(@Valid @RequestBody HorarioMedicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.crear(request));
    }

    @GetMapping("/medico/{medicoId}")
    public List<HorarioMedicoResponse> listarPorMedico(@PathVariable Long medicoId) {
        return horarioService.listarPorMedico(medicoId);
    }

    @PutMapping("/{id}")
    public HorarioMedicoResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody HorarioMedicoRequest request) {
        return horarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public HorarioMedicoResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return horarioService.cambiarEstado(id, activo);
    }
}
