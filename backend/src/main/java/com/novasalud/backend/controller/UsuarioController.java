package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Usuario;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final DatosMock datos;

    public UsuarioController(DatosMock datos) {
        this.datos = datos;
    }

    /* ================== GET ================== */

    @GetMapping
    public List<Usuario> listar() {
        return datos.usuarios;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Usuario> obtener(@PathVariable Integer id) {
        return datos.usuarios.stream()
                .filter(u -> u.getIdUsuario().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Usuario> buscar(@RequestParam String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();

        return datos.usuarios.stream()
                .filter(u -> coincide(u, t))
                .toList();
    }

    /* ================== POST ================== */

    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {

        if (usuario.getCorreo() == null || usuario.getContrasena() == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean correoDuplicado = datos.usuarios.stream()
                .anyMatch(u -> u.getCorreo().equalsIgnoreCase(usuario.getCorreo()));

        if (correoDuplicado) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        usuario.setIdUsuario(datos.seqUsuario.incrementAndGet());

        if (usuario.getEstado() == null || usuario.getEstado().isBlank()) {
            usuario.setEstado("ACTIVO");
        }

        datos.usuarios.add(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /* ================== PUT (merge, no reemplazo) ================== */

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Integer id, @RequestBody Usuario nuevo) {

        for (Usuario actual : datos.usuarios) {

            if (!actual.getIdUsuario().equals(id)) continue;

            if (nuevo.getNombres()    != null) actual.setNombres(nuevo.getNombres());
            if (nuevo.getApellidos()  != null) actual.setApellidos(nuevo.getApellidos());
            if (nuevo.getDni()        != null) actual.setDni(nuevo.getDni());
            if (nuevo.getTelefono()   != null) actual.setTelefono(nuevo.getTelefono());
            if (nuevo.getCorreo()     != null) actual.setCorreo(nuevo.getCorreo());
            if (nuevo.getEstado()     != null) actual.setEstado(nuevo.getEstado());

            if (nuevo.getContrasena() != null && !nuevo.getContrasena().isBlank()) {
                actual.setContrasena(nuevo.getContrasena());
            }

            if (nuevo.getRoles() != null && !nuevo.getRoles().isEmpty()) {
                actual.setRoles(nuevo.getRoles());
            }

            actual.setRequiereCambioContrasena(nuevo.isRequiereCambioContrasena());

            return ResponseEntity.ok(actual);
        }

        return ResponseEntity.notFound().build();
    }

    /* ================== PATCH estado (activar / desactivar) ================== */

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstado(@PathVariable Integer id,@RequestParam String valor) {

        for (Usuario actual : datos.usuarios) {
            if (actual.getIdUsuario().equals(id)) {
                actual.setEstado(valor);
                return ResponseEntity.ok(actual);
            }
        }

        return ResponseEntity.notFound().build();
    }

    /* ================== DELETE ================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = datos.usuarios.removeIf(u -> u.getIdUsuario().equals(id));
        return eliminado
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /* ================== Helper de búsqueda ================== */

    private boolean coincide(Usuario u, String texto) {
        if (texto.isEmpty()) return true;

        String nombres   = u.getNombres()   == null ? "" : u.getNombres().toLowerCase();
        String apellidos = u.getApellidos() == null ? "" : u.getApellidos().toLowerCase();
        String dni       = u.getDni()       == null ? "" : u.getDni().toLowerCase();
        String correo    = u.getCorreo()    == null ? "" : u.getCorreo().toLowerCase();

        return nombres.contains(texto)
                || apellidos.contains(texto)
                || (nombres + " " + apellidos).contains(texto)
                || dni.contains(texto)
                || correo.contains(texto);
    }
}