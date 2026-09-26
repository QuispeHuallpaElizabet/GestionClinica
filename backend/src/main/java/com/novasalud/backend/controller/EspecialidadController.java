package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Especialidad;

@RestController
@RequestMapping("/api/especialidades")
@CrossOrigin(origins = "*")
public class EspecialidadController {

    private final DatosMock datos;

    public EspecialidadController(DatosMock datos) {
        this.datos = datos;
    }

    /* ================== GET ================== */

    @GetMapping
    public List<Especialidad> listar() {
        return datos.especialidades;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Especialidad> obtener(@PathVariable Integer id) {
        return datos.especialidades.stream()
                .filter(e -> e.getIdEspecialidad().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Especialidad> buscar(@RequestParam String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();
        return datos.especialidades.stream()
                .filter(e -> e.getNombre() != null && e.getNombre().toLowerCase().contains(t))
                .toList();
    }

    /* ================== POST ================== */

    @PostMapping
    public ResponseEntity<Especialidad> crear(@RequestBody Especialidad especialidad) {

        if (especialidad.getNombre() == null || especialidad.getNombre().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (especialidad.getDuracionCitaMin() == null || especialidad.getDuracionCitaMin() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        boolean duplicado = datos.especialidades.stream()
                .anyMatch(e -> e.getNombre().equalsIgnoreCase(especialidad.getNombre()));
        if (duplicado) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        especialidad.setIdEspecialidad(datos.seqEspecialidad.incrementAndGet());
        if (especialidad.getEstado() == null) {
            especialidad.setEstado(true);
        }

        datos.especialidades.add(especialidad);
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidad);
    }

    /* ================== PUT (merge) ================== */

    @PutMapping("/{id}")
    public ResponseEntity<Especialidad> actualizar(@PathVariable Integer id,@RequestBody Especialidad nuevo) {
        for (Especialidad actual : datos.especialidades) {
            if (!actual.getIdEspecialidad().equals(id)) continue;
            if (nuevo.getNombre()          != null) actual.setNombre(nuevo.getNombre());
            if (nuevo.getDuracionCitaMin() != null) actual.setDuracionCitaMin(nuevo.getDuracionCitaMin());
            if (nuevo.getEstado()          != null) actual.setEstado(nuevo.getEstado());

            return ResponseEntity.ok(actual);
        }

        return ResponseEntity.notFound().build();
    }

    /* ================== PATCH estado ================== */

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Especialidad> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean activo) {
        return datos.especialidades.stream()
                .filter(e -> e.getIdEspecialidad().equals(id))
                .findFirst()
                .map(e -> {
                    e.setEstado(activo);
                    return ResponseEntity.ok(e);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* ================== DELETE ================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.especialidades.removeIf(e -> e.getIdEspecialidad().equals(id));
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}