package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Paciente;
import com.novasalud.backend.models.Rol;
import com.novasalud.backend.models.Usuario;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    private final DatosMock datos;

    public PacienteController(DatosMock datos) {
        this.datos = datos;
    }

    private Rol rolPaciente() {
        return datos.roles.stream()
                .filter(r -> "PACIENTE".equalsIgnoreCase(r.getNombre()))
                .findFirst().orElse(null);
    }

    /* ================== GET ================== */

    @GetMapping
    public List<Paciente> listar() {
        return datos.pacientes;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Paciente> obtener(@PathVariable Integer id) {
        return datos.pacientes.stream()
                .filter(p -> p.getIdUsuario().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Paciente> buscar(@RequestParam String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();
        return datos.pacientes.stream().filter(p -> coincide(p, t)).toList();
    }

    /* ================== POST ================== */

    @PostMapping
    public ResponseEntity<Paciente> crear(@RequestBody Paciente paciente) {

        if (paciente.getCorreo() == null || paciente.getNombres() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean dup = datos.usuarios.stream()
                .anyMatch(u -> u.getCorreo().equalsIgnoreCase(paciente.getCorreo()));
        if (dup) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Usuario u = new Usuario();
        u.setIdUsuario(datos.seqUsuario.incrementAndGet());
        u.setNombres(paciente.getNombres());
        u.setApellidos(paciente.getApellidos());
        u.setDni(paciente.getDni());
        u.setTelefono(paciente.getTelefono());
        u.setCorreo(paciente.getCorreo());
        u.setContrasena(
                paciente.getContrasena() != null && !paciente.getContrasena().isBlank()
                        ? paciente.getContrasena() : "123456"
        );
        u.setEstado(Boolean.TRUE.equals(paciente.getActivo()) ? "ACTIVO" : "INACTIVO");
        u.setRequiereCambioContrasena(true);

        Rol rol = rolPaciente();
        if (rol != null) u.setRoles(List.of(rol));

        datos.usuarios.add(u);

        paciente.setIdUsuario(u.getIdUsuario());
        if (paciente.getContrasena() == null || paciente.getContrasena().isBlank()) {
            paciente.setContrasena("123456");
        }
        if (paciente.getActivo() == null) paciente.setActivo(true);

        datos.pacientes.add(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
    }

    /* ================== PUT (merge) ================== */

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizar(@PathVariable Integer id,
                                               @RequestBody Paciente nuevo) {

        Paciente actual = datos.pacientes.stream()
                .filter(p -> p.getIdUsuario().equals(id))
                .findFirst().orElse(null);
        if (actual == null) return ResponseEntity.notFound().build();

        if (nuevo.getNombres()   != null) actual.setNombres(nuevo.getNombres());
        if (nuevo.getApellidos() != null) actual.setApellidos(nuevo.getApellidos());
        if (nuevo.getDni()       != null) actual.setDni(nuevo.getDni());
        if (nuevo.getTelefono()  != null) actual.setTelefono(nuevo.getTelefono());
        if (nuevo.getCorreo()    != null) actual.setCorreo(nuevo.getCorreo());
        if (nuevo.getActivo()    != null) actual.setActivo(nuevo.getActivo());
        if (nuevo.getFechaNac()  != null) actual.setFechaNac(nuevo.getFechaNac());
        if (nuevo.getDireccion() != null) actual.setDireccion(nuevo.getDireccion());
        if (nuevo.getContrasena() != null && !nuevo.getContrasena().isBlank()) {
            actual.setContrasena(nuevo.getContrasena());
        }

        datos.usuarios.stream()
                .filter(u -> u.getIdUsuario().equals(id))
                .findFirst()
                .ifPresent(u -> {
                    u.setNombres(actual.getNombres());
                    u.setApellidos(actual.getApellidos());
                    u.setDni(actual.getDni());
                    u.setTelefono(actual.getTelefono());
                    u.setCorreo(actual.getCorreo());
                    u.setContrasena(actual.getContrasena());
                    u.setEstado(Boolean.TRUE.equals(actual.getActivo()) ? "ACTIVO" : "INACTIVO");
                });

        return ResponseEntity.ok(actual);
    }

    /* ================== PATCH estado ================== */

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Paciente> cambiarEstado(@PathVariable Integer id,
                                                  @RequestParam Boolean activo) {
        Paciente p = datos.pacientes.stream()
                .filter(x -> x.getIdUsuario().equals(id))
                .findFirst().orElse(null);
        if (p == null) return ResponseEntity.notFound().build();

        p.setActivo(activo);
        datos.usuarios.stream()
                .filter(u -> u.getIdUsuario().equals(id))
                .findFirst()
                .ifPresent(u -> u.setEstado(activo ? "ACTIVO" : "INACTIVO"));

        return ResponseEntity.ok(p);
    }

    /* ================== DELETE ================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.pacientes.removeIf(p -> p.getIdUsuario().equals(id));
        if (!ok) return ResponseEntity.notFound().build();
        datos.usuarios.removeIf(u -> u.getIdUsuario().equals(id));
        return ResponseEntity.noContent().build();
    }

    private boolean coincide(Paciente p, String texto) {
        if (texto.isEmpty()) return true;
        String n = p.getNombres()   == null ? "" : p.getNombres().toLowerCase();
        String a = p.getApellidos() == null ? "" : p.getApellidos().toLowerCase();
        String d = p.getDni()       == null ? "" : p.getDni().toLowerCase();
        return n.contains(texto) || a.contains(texto)
                || (n + " " + a).contains(texto)
                || d.contains(texto);
    }
}