package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Medico;
import com.novasalud.backend.models.Rol;
import com.novasalud.backend.models.Usuario;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "*")
public class MedicoController {

    private final DatosMock datos;

    public MedicoController(DatosMock datos) {
        this.datos = datos;
    }

    /* ================== Rol MEDICO (ya cargado en DatosMock) ================== */
    private Rol rolMedico() {
        return datos.roles.stream()
                .filter(r -> "MEDICO".equalsIgnoreCase(r.getNombre()))
                .findFirst()
                .orElse(null);
    }

    /* ================== GET ================== */

    @GetMapping
    public List<Medico> listar() {
        return datos.medicos;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Medico> obtener(@PathVariable Integer id) {
        return datos.medicos.stream()
                .filter(m -> m.getIdUsuario().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Medico> buscar(@RequestParam String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();
        return datos.medicos.stream()
                .filter(m -> coincide(m, t))
                .toList();
    }

    /** Filtro combinado para RF08: especialidad + sede. */
    @GetMapping("/filtrar")
    public List<Medico> filtrar(@RequestParam(required = false) Integer especialidad,
                                @RequestParam(required = false) Integer sede) {
        return datos.medicos.stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .filter(m -> especialidad == null || especialidad.equals(m.getIdEspecialidad()))
                .filter(m -> sede        == null || sede.equals(m.getIdSede()))
                .toList();
    }

    /* ================== POST ================== */

    @PostMapping
    public ResponseEntity<Medico> crear(@RequestBody Medico medico) {

        if (medico.getCorreo() == null || medico.getNombres() == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean correoDup = datos.usuarios.stream()
                .anyMatch(u -> u.getCorreo().equalsIgnoreCase(medico.getCorreo()));
        if (correoDup) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        /* --- Crear Usuario base --- */
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setIdUsuario(datos.seqUsuario.incrementAndGet());
        nuevoUsuario.setNombres(medico.getNombres());
        nuevoUsuario.setApellidos(medico.getApellidos());
        nuevoUsuario.setDni(medico.getDni());
        nuevoUsuario.setTelefono(medico.getTelefono());
        nuevoUsuario.setCorreo(medico.getCorreo());
        nuevoUsuario.setContrasena(
                medico.getContrasena() != null && !medico.getContrasena().isBlank()
                        ? medico.getContrasena()
                        : "123456"
        );
        nuevoUsuario.setEstado(Boolean.TRUE.equals(medico.getActivo()) ? "ACTIVO" : "INACTIVO");
        nuevoUsuario.setRequiereCambioContrasena(true);

        Rol rol = rolMedico();
        if (rol != null) {
            nuevoUsuario.setRoles(List.of(rol));
        }

        datos.usuarios.add(nuevoUsuario);

        /* --- Crear Medico --- */
        medico.setIdUsuario(nuevoUsuario.getIdUsuario());
        if (medico.getContrasena() == null || medico.getContrasena().isBlank()) {
            medico.setContrasena("123456");
        }
        if (medico.getActivo() == null) {
            medico.setActivo(true);
        }
        if (medico.getNumeroColegiatura() == null || medico.getNumeroColegiatura().isBlank()) {
            medico.setNumeroColegiatura("CMP-" + (10000 + medico.getIdUsuario()));
        }

        datos.medicos.add(medico);

        return ResponseEntity.status(HttpStatus.CREATED).body(medico);
    }

    /* ================== PUT (merge en ambas listas) ================== */

    @PutMapping("/{id}")
    public ResponseEntity<Medico> actualizar(@PathVariable Integer id,@RequestBody Medico nuevo) {

        Medico actual = datos.medicos.stream()
                .filter(m -> m.getIdUsuario().equals(id))
                .findFirst()
                .orElse(null);

        if (actual == null) return ResponseEntity.notFound().build();

        if (nuevo.getNombres()          != null) actual.setNombres(nuevo.getNombres());
        if (nuevo.getApellidos()        != null) actual.setApellidos(nuevo.getApellidos());
        if (nuevo.getDni()              != null) actual.setDni(nuevo.getDni());
        if (nuevo.getTelefono()         != null) actual.setTelefono(nuevo.getTelefono());
        if (nuevo.getCorreo()           != null) actual.setCorreo(nuevo.getCorreo());
        if (nuevo.getIdEspecialidad()   != null) actual.setIdEspecialidad(nuevo.getIdEspecialidad());
        if (nuevo.getIdSede()           != null) actual.setIdSede(nuevo.getIdSede());
        if (nuevo.getNumeroColegiatura()!= null) actual.setNumeroColegiatura(nuevo.getNumeroColegiatura());
        if (nuevo.getActivo()           != null) actual.setActivo(nuevo.getActivo());

        if (nuevo.getContrasena() != null && !nuevo.getContrasena().isBlank()) {
            actual.setContrasena(nuevo.getContrasena());
        }

        /* Sincronizar Usuario base */
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
    public ResponseEntity<Medico> cambiarEstado(@PathVariable Integer id,
                                                @RequestParam Boolean activo) {

        Medico m = datos.medicos.stream()
                .filter(x -> x.getIdUsuario().equals(id))
                .findFirst()
                .orElse(null);

        if (m == null) return ResponseEntity.notFound().build();

        m.setActivo(activo);
        datos.usuarios.stream()
                .filter(u -> u.getIdUsuario().equals(id))
                .findFirst()
                .ifPresent(u -> u.setEstado(activo ? "ACTIVO" : "INACTIVO"));

        return ResponseEntity.ok(m);
    }

    /* ================== DELETE ================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.medicos.removeIf(m -> m.getIdUsuario().equals(id));
        if (!ok) return ResponseEntity.notFound().build();

        // También quitamos el usuario asociado para no dejar huérfanos
        datos.usuarios.removeIf(u -> u.getIdUsuario().equals(id));
        return ResponseEntity.noContent().build();
    }

    /* ================== Helper ================== */

    private boolean coincide(Medico m, String texto) {
        if (texto.isEmpty()) return true;
        String nombres   = m.getNombres()   == null ? "" : m.getNombres().toLowerCase();
        String apellidos = m.getApellidos() == null ? "" : m.getApellidos().toLowerCase();
        String dni       = m.getDni()       == null ? "" : m.getDni().toLowerCase();
        String correo    = m.getCorreo()    == null ? "" : m.getCorreo().toLowerCase();
        String colegio   = m.getNumeroColegiatura() == null ? "" : m.getNumeroColegiatura().toLowerCase();

        return nombres.contains(texto)
                || apellidos.contains(texto)
                || (nombres + " " + apellidos).contains(texto)
                || dni.contains(texto)
                || correo.contains(texto)
                || colegio.contains(texto);
    }
}