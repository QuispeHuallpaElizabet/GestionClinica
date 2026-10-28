package com.novasalud.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.LoginRequest;
import com.novasalud.backend.dto.LoginResponse;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public AuthService(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    // Por ahora sin JWT: solo valida credenciales y estado del usuario
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.getCorreo().trim())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        if (!usuario.getContrasena().equals(request.getContrasena())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        if (usuario.getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("El usuario se encuentra inactivo");
        }
        return new LoginResponse("Inicio de sesión exitoso", usuarioService.respuesta(usuario));
    }
}
