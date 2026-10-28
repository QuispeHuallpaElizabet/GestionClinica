package com.novasalud.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.CitaRequest;
import com.novasalud.backend.dto.CitaResponse;
import com.novasalud.backend.dto.HistorialCitaResponse;
import com.novasalud.backend.service.CitaService;
import com.novasalud.backend.service.HistorialCitaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final HistorialCitaService historialCitaService;

    public CitaController(CitaService citaService, HistorialCitaService historialCitaService) {
        this.citaService = citaService;
        this.historialCitaService = historialCitaService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(
            @Valid @RequestBody CitaRequest request,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(request, usuarioId));
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<CitaResponse> reprogramar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        return ResponseEntity.ok(citaService.reprogramarCita(id, request, usuarioId));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelar(
            @PathVariable Long id,
            @RequestParam(name = "usuarioId") Long usuarioId,
            @RequestParam(name = "observacion", required = false) String observacion) {
        return ResponseEntity.ok(citaService.cancelarCita(id, usuarioId, observacion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam(name = "valor") String valor,
            @RequestParam(name = "usuarioId") Long usuarioId,
            @RequestParam(name = "observacion", required = false) String observacion) {
        return ResponseEntity.ok(citaService.cambiarEstado(id, valor, usuarioId, observacion));
    }

    @GetMapping
    public ResponseEntity<List<CitaResponse>> listar(
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "medicoId", required = false) Long medicoId,
            @RequestParam(name = "pacienteId", required = false) Long pacienteId,
            @RequestParam(name = "sedeId", required = false) Long sedeId,
            @RequestParam(name = "especialidadId", required = false) Long especialidadId,
            @RequestParam(name = "estado", required = false) String estado) {
        return ResponseEntity.ok(
                citaService.listarCitas(fecha, medicoId, pacienteId, sedeId, especialidadId, estado));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialCitaResponse>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(historialCitaService.listarPorCita(id));
    }
}