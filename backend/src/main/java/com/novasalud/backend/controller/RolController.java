package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.RolRequest;
import com.novasalud.backend.dto.RolResponse;
import com.novasalud.backend.service.RolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@Validated
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) { this.rolService = rolService; }

    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(request));
    }

    @GetMapping
    public List<RolResponse> listar() { return rolService.listar(); }
}
