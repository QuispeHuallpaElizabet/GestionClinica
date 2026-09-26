package com.novasalud.backend.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novasalud.backend.data.DatosMock;
import com.novasalud.backend.models.Cita;
import com.novasalud.backend.models.Especialidad;
import com.novasalud.backend.models.HorarioMedico;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    private final DatosMock datos;

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /* Estados válidos y los que "ocupan" el horario de un médico / paciente */
    private static final List<String> ESTADOS_VALIDOS =
            List.of("Pendiente", "Confirmada", "Atendida", "Cancelada", "No asistió");
    private static final List<String> ESTADOS_ACTIVOS =
            List.of("Pendiente", "Confirmada");

    public CitaController(DatosMock datos) {
        this.datos = datos;
    }

    /* ================== GET ================== */

    @GetMapping
    public List<Cita> listar() {
        return datos.citas;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Cita> obtener(@PathVariable Integer id) {
        return datos.citas.stream()
                .filter(c -> c.getIdCita().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Filtros combinados (RF17). Todos opcionales. */
    @GetMapping("/buscar")
    public List<Cita> buscar(@RequestParam(required = false) String  texto,
                             @RequestParam(required = false) String  estado,
                             @RequestParam(required = false) String  fecha,
                             @RequestParam(required = false) Integer idMedico,
                             @RequestParam(required = false) Integer idPaciente,
                             @RequestParam(required = false) Integer idSede,
                             @RequestParam(required = false) Integer idEspecialidad) {

        String t = texto == null ? "" : texto.toLowerCase().trim();

        return datos.citas.stream()
                .filter(c -> estado == null || estado.isBlank()
                          || estado.equalsIgnoreCase(c.getEstado()))
                .filter(c -> fecha == null || fecha.isBlank()
                          || fecha.equals(c.getFechaCita()))
                .filter(c -> idMedico        == null || idMedico.equals(c.getIdMedico()))
                .filter(c -> idPaciente      == null || idPaciente.equals(c.getIdPaciente()))
                .filter(c -> idSede          == null || idSede.equals(c.getIdSede()))
                .filter(c -> idEspecialidad  == null || idEspecialidad.equals(c.getIdEspecialidad()))
                .filter(c -> t.isEmpty() || coincide(c, t))
                .toList();
    }

    /** Agenda de un médico en una fecha (RF12). */
    @GetMapping("/medico/{idMedico}")
    public List<Cita> agendaMedico(@PathVariable Integer idMedico,
                                   @RequestParam(required = false) String fecha,
                                   @RequestParam(required = false) String estado) {
        return datos.citas.stream()
                .filter(c -> c.getIdMedico().equals(idMedico))
                .filter(c -> fecha == null || fecha.isBlank() || fecha.equals(c.getFechaCita()))
                .filter(c -> estado == null || estado.isBlank()
                          || estado.equalsIgnoreCase(c.getEstado()))
                .toList();
    }

    /** Citas de un paciente (RF14). */
    @GetMapping("/paciente/{idPaciente}")
    public List<Cita> citasPaciente(@PathVariable Integer idPaciente) {
        return datos.citas.stream()
                .filter(c -> c.getIdPaciente().equals(idPaciente))
                .toList();
    }

    /**
     * RF08 — Horarios disponibles de un médico en una fecha.
     * Genera slots desde sus horarios activos según la duración de la especialidad,
     * excluyendo los que ya tienen una cita activa (Pendiente | Confirmada).
     */
    @GetMapping("/disponibles")
    public List<String> disponibles(@RequestParam Integer idMedico,
                                    @RequestParam String  fecha) {

        LocalDate dia = LocalDate.parse(fecha);
        int diaSemana = dia.getDayOfWeek().getValue();   // 1=Lunes … 7=Domingo

        List<HorarioMedico> bloques = datos.horarios.stream()
                .filter(h -> h.getIdMedico().equals(idMedico))
                .filter(h -> Boolean.TRUE.equals(h.getEstado()))
                .filter(h -> h.getDiaSemana().equals(diaSemana))
                .toList();

        if (bloques.isEmpty()) return List.of();

        Integer idEspecialidad = datos.medicos.stream()
                .filter(m -> m.getIdUsuario().equals(idMedico))
                .map(m -> m.getIdEspecialidad())
                .findFirst().orElse(null);

        int duracion = datos.especialidades.stream()
                .filter(e -> e.getIdEspecialidad().equals(idEspecialidad))
                .map(Especialidad::getDuracionCitaMin)
                .findFirst().orElse(30);

        List<String> ocupados = datos.citas.stream()
                .filter(c -> c.getIdMedico().equals(idMedico))
                .filter(c -> fecha.equals(c.getFechaCita()))
                .filter(c -> ESTADOS_ACTIVOS.contains(c.getEstado()))
                .map(Cita::getHoraCita)
                .toList();

        List<String> disponibles = new ArrayList<>();
        for (HorarioMedico b : bloques) {
            LocalTime ini = LocalTime.parse(b.getHoraInicio(), HORA_FMT);
            LocalTime fin = LocalTime.parse(b.getHoraFin(), HORA_FMT);
            while (ini.plusMinutes(duracion).compareTo(fin) <= 0) {
                String slot = ini.format(HORA_FMT);
                if (!ocupados.contains(slot)) disponibles.add(slot);
                ini = ini.plusMinutes(duracion);
            }
        }
        return disponibles;
    }

    /* ================== POST ================== */

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Cita cita) {

        String error = validar(cita, null);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", error));
        }

        String ahora = java.time.LocalDateTime.now().toString();
        cita.setIdCita(datos.seqCita.incrementAndGet());
        if (cita.getEstado() == null || cita.getEstado().isBlank()) {
            cita.setEstado("Pendiente");
        }
        cita.setFechaCreacion(ahora);
        cita.setFechaActualizacion(ahora);

        datos.citas.add(cita);
        registrarHistorial(cita, "CREAR", null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(cita);
    }

    /* ================== PUT (edición completa) ================== */

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody Cita nuevo) {

        Cita actual = datos.citas.stream()
                .filter(c -> c.getIdCita().equals(id))
                .findFirst().orElse(null);
        if (actual == null) return ResponseEntity.notFound().build();

        // clon temporal para validar sin haber tocado `actual`
        Cita candidata = clonar(actual);
        if (nuevo.getIdPaciente()     != null) candidata.setIdPaciente(nuevo.getIdPaciente());
        if (nuevo.getIdMedico()       != null) candidata.setIdMedico(nuevo.getIdMedico());
        if (nuevo.getIdEspecialidad() != null) candidata.setIdEspecialidad(nuevo.getIdEspecialidad());
        if (nuevo.getIdSede()         != null) candidata.setIdSede(nuevo.getIdSede());
        if (nuevo.getFechaCita()      != null) candidata.setFechaCita(nuevo.getFechaCita());
        if (nuevo.getHoraCita()       != null) candidata.setHoraCita(nuevo.getHoraCita());
        if (nuevo.getMotivoCita()     != null) candidata.setMotivoCita(nuevo.getMotivoCita());

        String error = validar(candidata, id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", error));
        }

        // aplicar
        actual.setIdPaciente(candidata.getIdPaciente());
        actual.setIdMedico(candidata.getIdMedico());
        actual.setIdEspecialidad(candidata.getIdEspecialidad());
        actual.setIdSede(candidata.getIdSede());
        actual.setFechaCita(candidata.getFechaCita());
        actual.setHoraCita(candidata.getHoraCita());
        actual.setMotivoCita(candidata.getMotivoCita());
        actual.setFechaActualizacion(java.time.LocalDateTime.now().toString());

        registrarHistorial(actual, "EDITAR", null, null);
        return ResponseEntity.ok(actual);
    }

    /* ================== PATCH estado (RF11 + RF13) ================== */

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                           @RequestParam String valor,
                                           @RequestParam(required = false) String nota) {

        if (!ESTADOS_VALIDOS.contains(valor)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Estado no válido."));
        }

        Cita c = datos.citas.stream()
                .filter(x -> x.getIdCita().equals(id))
                .findFirst().orElse(null);
        if (c == null) return ResponseEntity.notFound().build();

        String anterior = c.getEstado();
        c.setEstado(valor);
        if (nota != null) c.setNotaAtencion(nota);
        c.setFechaActualizacion(java.time.LocalDateTime.now().toString());

        registrarHistorial(c, "CAMBIO_ESTADO", anterior, valor);
        return ResponseEntity.ok(c);
    }

    /* ================== PATCH reprogramar (RF15) ================== */

    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<?> reprogramar(@PathVariable Integer id,
                                         @RequestBody Map<String, String> body) {

        String nuevaFecha = body.get("fechaCita");
        String nuevaHora  = body.get("horaCita");

        Cita c = datos.citas.stream()
                .filter(x -> x.getIdCita().equals(id))
                .findFirst().orElse(null);
        if (c == null) return ResponseEntity.notFound().build();

        if (nuevaFecha != null) c.setFechaCita(nuevaFecha);
        if (nuevaHora  != null) c.setHoraCita(nuevaHora);

        String error = validar(c, id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", error));
        }

        c.setEstado("Pendiente"); // reprogramar baja a Pendiente
        c.setFechaActualizacion(java.time.LocalDateTime.now().toString());

        registrarHistorial(c, "REPROGRAMAR", null, null);
        return ResponseEntity.ok(c);
    }

    /* ================== DELETE ================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean ok = datos.citas.removeIf(c -> c.getIdCita().equals(id));
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /* ================== Helpers ================== */

    /** Devuelve null si la cita es válida, o un mensaje de error (RF10). */
    private String validar(Cita cita, Integer idExcluir) {

        if (cita.getIdPaciente() == null || cita.getIdMedico() == null
                || cita.getFechaCita() == null || cita.getHoraCita() == null) {
            return "Faltan datos obligatorios de la cita.";
        }

        boolean medicoOcupado = datos.citas.stream()
                .filter(c -> !c.getIdCita().equals(idExcluir))
                .filter(c -> c.getIdMedico().equals(cita.getIdMedico()))
                .filter(c -> cita.getFechaCita().equals(c.getFechaCita()))
                .filter(c -> cita.getHoraCita().equals(c.getHoraCita()))
                .anyMatch(c -> ESTADOS_ACTIVOS.contains(c.getEstado()));

        if (medicoOcupado) return "El médico ya tiene una cita en ese horario.";

        boolean pacienteOcupado = datos.citas.stream()
                .filter(c -> !c.getIdCita().equals(idExcluir))
                .filter(c -> c.getIdPaciente().equals(cita.getIdPaciente()))
                .filter(c -> cita.getFechaCita().equals(c.getFechaCita()))
                .filter(c -> cita.getHoraCita().equals(c.getHoraCita()))
                .anyMatch(c -> ESTADOS_ACTIVOS.contains(c.getEstado()));

        if (pacienteOcupado) return "El paciente ya tiene una cita en ese horario.";

        return null;
    }

    private void registrarHistorial(Cita c, String accion, String estadoAnt, String estadoNue) {
        java.util.Map<String, Object> h = new java.util.HashMap<>();
        h.put("idHistorial", datos.seqHistorial.incrementAndGet());
        h.put("idCita", c.getIdCita());
        h.put("accion", accion);
        h.put("estadoAnterior", estadoAnt);
        h.put("estadoNuevo", estadoNue);
        h.put("fechaHora", java.time.LocalDateTime.now().toString());
        datos.historialCitas.add(h);
    }

    private Cita clonar(Cita c) {
        Cita x = new Cita();
        x.setIdCita(c.getIdCita());
        x.setIdPaciente(c.getIdPaciente());
        x.setIdMedico(c.getIdMedico());
        x.setIdEspecialidad(c.getIdEspecialidad());
        x.setIdSede(c.getIdSede());
        x.setFechaCita(c.getFechaCita());
        x.setHoraCita(c.getHoraCita());
        x.setEstado(c.getEstado());
        x.setMotivoCita(c.getMotivoCita());
        x.setFechaCreacion(c.getFechaCreacion());
        x.setFechaActualizacion(c.getFechaActualizacion());
        x.setNotaAtencion(c.getNotaAtencion());
        return x;
    }

    private boolean coincide(Cita c, String texto) {
        return String.valueOf(c.getIdCita()).contains(texto)
                || (c.getEstado() != null && c.getEstado().toLowerCase().contains(texto))
                || (c.getMotivoCita() != null && c.getMotivoCita().toLowerCase().contains(texto));
    }
}