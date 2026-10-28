package com.novasalud.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.dto.EmpleadoRegistroRequest;
import com.novasalud.backend.dto.UsuarioResponse;
import com.novasalud.backend.dto.UsuarioRolRequest;
import com.novasalud.backend.dto.UsuarioRolResponse;
import com.novasalud.backend.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @PostMapping("/empleado")
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody EmpleadoRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearEmpleado(request));
    }

    @PostMapping("/rol")
    public ResponseEntity<UsuarioRolResponse> asignarRol(@Valid @RequestBody UsuarioRolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.asignarRol(request));
    }
}
