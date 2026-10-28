package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.CitaResponse;
import com.novasalud.backend.dto.PacienteRegistroRequest;
import com.novasalud.backend.dto.PacienteRequest;
import com.novasalud.backend.dto.PacienteResponse;
import com.novasalud.backend.service.PacienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping("/registro")
    public ResponseEntity<PacienteResponse> registrar(@Valid @RequestBody PacienteRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.registrarPaciente(request));
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crearPerfil(@Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crearPerfilClinico(request));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteResponse>> buscar(
            @RequestParam(name = "dni", required = false) String dni,
            @RequestParam(name = "nombre", required = false) String nombre) {
        return ResponseEntity.ok(pacienteService.buscar(dni, nombre));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<CitaResponse>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerHistorial(id));
    }
}