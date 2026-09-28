package com.novasalud.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Usuario;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final DatosMock datos;

    public AuthController(DatosMock datos) {
        this.datos = datos;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

        String correo    = body.get("correo");
        String contrasena = body.get("contrasena");

        if (correo == null || contrasena == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Correo y contraseña son obligatorios."));
        }

        for (Usuario u : datos.usuarios) {

            if (u.getCorreo().equalsIgnoreCase(correo)
                    && u.getContrasena().equals(contrasena)) {

                if (!"ACTIVO".equalsIgnoreCase(u.getEstado())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("mensaje", "El usuario se encuentra inactivo."));
                }

                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Inicio de sesión exitoso.");
                respuesta.put("idUsuario", u.getIdUsuario());
                respuesta.put("nombres",   u.getNombres());
                respuesta.put("apellidos", u.getApellidos());
                respuesta.put("correo",    u.getCorreo());
                respuesta.put("roles",     u.getRoles());
                respuesta.put("requiereCambioContrasena", u.isRequiereCambioContrasena());

                return ResponseEntity.ok(respuesta);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Correo o contraseña incorrectos."));
    }

    /* ================== Cambiar contraseña (post-login forzado) ================== */
    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@RequestBody Map<String, String> body) {

        String correo     = body.get("correo");
        String contrasena = body.get("contrasena");

        if (correo == null || contrasena == null || contrasena.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La contraseña debe tener al menos 6 caracteres."));
        }

        for (Usuario u : datos.usuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo)) {

                u.setContrasena(contrasena);
                u.setRequiereCambioContrasena(false);

                Map<String, Object> r = new HashMap<>();
                r.put("mensaje", "Contraseña actualizada.");
                r.put("idUsuario", u.getIdUsuario());
                r.put("nombres",   u.getNombres());
                r.put("apellidos", u.getApellidos());
                r.put("correo",    u.getCorreo());
                r.put("roles",     u.getRoles());
                r.put("requiereCambioContrasena", false);

                return ResponseEntity.ok(r);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", "Usuario no encontrado."));
    }
}