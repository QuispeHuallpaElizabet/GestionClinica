package com.novasalud.backend.controller;

import java.time.LocalDate;
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

import com.novasalud.backend.dto.AgendaCitaResponse;
import com.novasalud.backend.dto.DisponibilidadResponse;
import com.novasalud.backend.dto.MedicoActualizacionRequest;
import com.novasalud.backend.dto.MedicoRegistroRequest;
import com.novasalud.backend.dto.MedicoResponse;
import com.novasalud.backend.service.MedicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medicos")
@Validated
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) { this.medicoService = medicoService; }

    @PostMapping
    public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.crear(request));
    }

    @GetMapping
    public List<MedicoResponse> listar() { return medicoService.listar(); }

    @GetMapping("/{id}")
    public MedicoResponse obtener(@PathVariable Long id) { return medicoService.obtener(id); }

    @PutMapping("/{id}")
    public MedicoResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody MedicoActualizacionRequest request) {
        return medicoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public MedicoResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return medicoService.cambiarEstado(id, activo);
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadResponse> disponibilidad(@RequestParam LocalDate fecha,
            @RequestParam Long sedeId, @RequestParam Long especialidadId) {
        return medicoService.disponibilidad(fecha, sedeId, especialidadId);
    }

    @GetMapping("/{id}/agenda")
    public List<AgendaCitaResponse> agenda(@PathVariable Long id, @RequestParam LocalDate fecha,
            @RequestParam(defaultValue = "diaria") String vista) {
        return medicoService.agenda(id, fecha, vista);
    }
}
