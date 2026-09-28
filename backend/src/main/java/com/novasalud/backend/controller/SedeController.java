package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Sede;

@RestController
@RequestMapping("/api/sedes")
@CrossOrigin(origins = "*")
public class SedeController {

    private final DatosMock datos;

    public SedeController(DatosMock datos) {
        this.datos = datos;
    }

    @GetMapping
    public List<Sede> listar() {
        return datos.sedes;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sede> obtener(@PathVariable Integer id) {
        return datos.sedes.stream()
                .filter(s -> s.getIdSede().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Sede> buscar(@RequestParam String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();
        return datos.sedes.stream()
                .filter(s -> s.getNombre().toLowerCase().contains(t)|| s.getDireccion().toLowerCase().contains(t))
                .toList();
    }

    @PostMapping
    public ResponseEntity<Sede> crear(@RequestBody Sede sede) {
        sede.setIdSede(datos.seqSede.incrementAndGet());
        if (sede.getEstado() == null) sede.setEstado(true);
        datos.sedes.add(sede);
        return ResponseEntity.status(HttpStatus.CREATED).body(sede);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sede> actualizar(@PathVariable Integer id, @RequestBody Sede nuevo) {
        for (int i = 0; i < datos.sedes.size(); i++) {
            Sede actual = datos.sedes.get(i);
            if (actual.getIdSede().equals(id)) {
                if (nuevo.getNombre() != null)      actual.setNombre(nuevo.getNombre());
                if (nuevo.getDireccion() != null)   actual.setDireccion(nuevo.getDireccion());
                if (nuevo.getTelefono() != null)    actual.setTelefono(nuevo.getTelefono());
                if (nuevo.getEstado() != null)      actual.setEstado(nuevo.getEstado());
                return ResponseEntity.ok(actual);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Sede> cambiarEstado(@PathVariable Integer id,@RequestParam Boolean activo) {
        return datos.sedes.stream()
                .filter(s -> s.getIdSede().equals(id))
                .findFirst()
                .map(s -> {
                    s.setEstado(activo);
                    return ResponseEntity.ok(s);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.sedes.removeIf(s -> s.getIdSede().equals(id));
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}