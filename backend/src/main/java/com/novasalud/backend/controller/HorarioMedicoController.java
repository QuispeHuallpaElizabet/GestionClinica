package com.novasalud.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.HorarioMedico;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "*")
public class HorarioMedicoController {

    private final DatosMock datos;

    public HorarioMedicoController(DatosMock datos) {
        this.datos = datos;
    }

    @GetMapping
    public List<HorarioMedico> listar() {
        return datos.horarios;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<HorarioMedico> obtener(@PathVariable Integer id) {
        return datos.horarios.stream()
                .filter(h -> h.getIdHorario().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medico/{idMedico}")
    public List<HorarioMedico> listarPorMedico(@PathVariable Integer idMedico) {
        return datos.horarios.stream()
                .filter(h -> h.getIdMedico().equals(idMedico))
                .toList();
    }

    @PostMapping
    public ResponseEntity<HorarioMedico> crear(@RequestBody HorarioMedico horario) {

        if (horario.getIdMedico() == null
                || horario.getDiaSemana() == null
                || horario.getHoraInicio() == null
                || horario.getHoraFin() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
            return ResponseEntity.badRequest().build();
        }
        if (horario.getHoraFin().compareTo(horario.getHoraInicio()) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        horario.setIdHorario(datos.seqHorario.incrementAndGet());
        if (horario.getEstado() == null) horario.setEstado(true);

        datos.horarios.add(horario);
        return ResponseEntity.status(HttpStatus.CREATED).body(horario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioMedico> actualizar(@PathVariable Integer id,
                                                    @RequestBody HorarioMedico nuevo) {
        for (HorarioMedico actual : datos.horarios) {
            if (!actual.getIdHorario().equals(id)) continue;

            if (nuevo.getIdMedico()  != null) actual.setIdMedico(nuevo.getIdMedico());
            if (nuevo.getDiaSemana() != null) actual.setDiaSemana(nuevo.getDiaSemana());
            if (nuevo.getHoraInicio()!= null) actual.setHoraInicio(nuevo.getHoraInicio());
            if (nuevo.getHoraFin()   != null) actual.setHoraFin(nuevo.getHoraFin());
            if (nuevo.getEstado()    != null) actual.setEstado(nuevo.getEstado());

            return ResponseEntity.ok(actual);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<HorarioMedico> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean activo) {
        return datos.horarios.stream()
                .filter(h -> h.getIdHorario().equals(id))
                .findFirst()
                .map(h -> {
                    h.setEstado(activo);
                    return ResponseEntity.ok(h);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.horarios.removeIf(h -> h.getIdHorario().equals(id));
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}