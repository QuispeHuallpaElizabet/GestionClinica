package com.novasalud.backend.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.SedeRequest;
import com.novasalud.backend.entity.Sede;
import com.novasalud.backend.service.SedeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sedes")
@Validated
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) { this.sedeService = sedeService; }

    @PostMapping
    public ResponseEntity<Sede> crear(@Valid @RequestBody SedeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sedeService.crearSede(request));
    }

    @GetMapping
    public List<Sede> listar() { return sedeService.listarSedes(); }

    @PutMapping("/{id}")
    public Sede actualizar(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return sedeService.actualizarSede(id, request);
    }

    @PatchMapping("/{id}/estado")
    public Map<String, String> desactivar(@PathVariable Long id) {
        sedeService.desactivarSede(id);
        return Map.of("mensaje", "Sede desactivada correctamente");
    }
}
