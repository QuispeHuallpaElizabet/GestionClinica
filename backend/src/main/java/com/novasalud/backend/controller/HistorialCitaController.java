package com.novasalud.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;

@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = "*")
public class HistorialCitaController {

    private final DatosMock datos;

    public HistorialCitaController(DatosMock datos) {
        this.datos = datos;
    }

    /* ================== Listado completo ================== */

    @GetMapping
    public List<Map<String, Object>> listar() {
        return datos.historialCitas;
    }

    /* ================== Historial de una cita ================== */

    @GetMapping("/cita/{idCita}")
    public List<Map<String, Object>> porCita(@PathVariable Integer idCita) {
        return datos.historialCitas.stream()
                .filter(h -> idCita.equals(h.get("idCita")))
                .collect(Collectors.toList());
    }

    /* ================== Historial de todas las citas de un paciente ================== */

    @GetMapping("/paciente/{idPaciente}")
    public List<Map<String, Object>> porPaciente(@PathVariable Integer idPaciente) {

        // 1) IDs de citas de ese paciente
        List<Integer> idsCitas = datos.citas.stream()
                .filter(c -> c.getIdPaciente().equals(idPaciente))
                .map(c -> c.getIdCita())
                .collect(Collectors.toList());

        // 2) Eventos de esas citas
        return datos.historialCitas.stream()
                .filter(h -> idsCitas.contains(h.get("idCita")))
                .collect(Collectors.toList());
    }

    /* ================== Historial por acción ================== */

    @GetMapping("/accion/{accion}")
    public List<Map<String, Object>> porAccion(@PathVariable String accion) {
        return datos.historialCitas.stream()
                .filter(h -> accion.equalsIgnoreCase(String.valueOf(h.get("accion"))))
                .collect(Collectors.toList());
    }

    /* ================== Búsqueda combinada (RF17) ================== */

    @GetMapping("/buscar")
    public List<Map<String, Object>> buscar(@RequestParam(required = false) Integer idCita,
                                            @RequestParam(required = false) String  accion) {
        return datos.historialCitas.stream()
                .filter(h -> idCita == null || idCita.equals(h.get("idCita")))
                .filter(h -> accion == null || accion.isBlank()|| accion.equalsIgnoreCase(String.valueOf(h.get("accion"))))
                .collect(Collectors.toList());
    }

    /* ================== Limpieza del historial de una cita (admin) ================== */

    @DeleteMapping("/cita/{idCita}")
    public ResponseEntity<Void> eliminarPorCita(@PathVariable Integer idCita) {
        boolean eliminado = datos.historialCitas.removeIf(h -> idCita.equals(h.get("idCita")));
        return eliminado
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}