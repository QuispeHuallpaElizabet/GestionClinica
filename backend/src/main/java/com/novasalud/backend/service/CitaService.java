package com.novasalud.backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.CitaRequest;
import com.novasalud.backend.dto.CitaResponse;
import com.novasalud.backend.entity.Cita;
import com.novasalud.backend.entity.Especialidad;
import com.novasalud.backend.entity.HorarioMedico;
import com.novasalud.backend.entity.Medico;
import com.novasalud.backend.entity.Paciente;
import com.novasalud.backend.entity.Sede;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.enums.EstadoCita;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.CitaRepository;
import com.novasalud.backend.repository.EspecialidadRepository;
import com.novasalud.backend.repository.HorarioMedicoRepository;
import com.novasalud.backend.repository.MedicoRepository;
import com.novasalud.backend.repository.PacienteRepository;
import com.novasalud.backend.repository.SedeRepository;
import com.novasalud.backend.repository.UsuarioRepository;
import com.novasalud.backend.repository.UsuarioRolRepository;

@Service
public class CitaService {

    private static final String ROL_RECEPCIONISTA = "RECEPCIONISTA";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private static final Long SIN_EXCLUSION = 0L;

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final SedeRepository sedeRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final HistorialCitaService historialCitaService;

    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       MedicoRepository medicoRepository,
                       EspecialidadRepository especialidadRepository,
                       SedeRepository sedeRepository,
                       HorarioMedicoRepository horarioMedicoRepository,
                       UsuarioRepository usuarioRepository,
                       UsuarioRolRepository usuarioRolRepository,
                       HistorialCitaService historialCitaService) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.especialidadRepository = especialidadRepository;
        this.sedeRepository = sedeRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.historialCitaService = historialCitaService;
    }

 
    @Transactional
    public CitaResponse crearCita(CitaRequest request, Long usuarioId) {
        Paciente paciente = obtenerPaciente(request.getPacienteId());
        Medico medico = obtenerMedico(request.getMedicoId());
        Especialidad especialidad = obtenerEspecialidad(request.getEspecialidadId());
        Sede sede = obtenerSede(request.getSedeId());

        LocalDate fecha = request.getFechaCita();
        LocalTime hora = request.getHoraCita().truncatedTo(ChronoUnit.MINUTES);

        Usuario actor = resolverActor(usuarioId, paciente.getUsuario());
        validarPuedeGestionar(actor, paciente);

        validarCita(paciente, medico, especialidad, sede, fecha, hora, SIN_EXCLUSION);

        Cita cita = new Cita(paciente, medico, especialidad, sede, fecha, hora, request.getMotivoCita().trim());
        cita.setEstadoCita(EstadoCita.PENDIENTE);
        cita = citaRepository.save(cita);

        historialCitaService.registrar(cita, actor, "CREADA",
                null, EstadoCita.PENDIENTE,
                null, null, fecha, hora,
                "Cita registrada");

        return toResponse(cita);
    }

   
    @Transactional
    public CitaResponse reprogramarCita(Long id, CitaRequest request, Long usuarioId) {
        Cita cita = obtenerCita(id);

        if (cita.getEstadoCita() != EstadoCita.PENDIENTE) {
            throw new RuntimeException("Solo se pueden reprogramar citas en estado PENDIENTE (estado actual: "
                    + cita.getEstadoCita() + ")");
        }
        if (!cita.getPaciente().getId().equals(request.getPacienteId())) {
            throw new RuntimeException("No se puede cambiar el paciente de una cita al reprogramarla");
        }

        Paciente paciente = cita.getPaciente();
        Medico medico = obtenerMedico(request.getMedicoId());
        Especialidad especialidad = obtenerEspecialidad(request.getEspecialidadId());
        Sede sede = obtenerSede(request.getSedeId());

        LocalDate fechaNueva = request.getFechaCita();
        LocalTime horaNueva = request.getHoraCita().truncatedTo(ChronoUnit.MINUTES);

        Usuario actor = resolverActor(usuarioId, paciente.getUsuario());
        validarPuedeGestionar(actor, paciente);

        validarCita(paciente, medico, especialidad, sede, fechaNueva, horaNueva, cita.getIdCita());

        LocalDate fechaAnterior = cita.getFechaCita();
        LocalTime horaAnterior = cita.getHoraCita();
        boolean cambioMedico = !cita.getMedico().getId().equals(medico.getId());
        boolean cambioSede = !cita.getSede().getIdSede().equals(sede.getIdSede());

        cita.setMedico(medico);
        cita.setEspecialidad(especialidad);
        cita.setSede(sede);
        cita.setFechaCita(fechaNueva);
        cita.setHoraCita(horaNueva);
        cita.setMotivoCita(request.getMotivoCita().trim());
        cita = citaRepository.save(cita);

        StringBuilder obs = new StringBuilder("Reprogramada de ")
                .append(fechaAnterior).append(" ").append(horaAnterior)
                .append(" a ").append(fechaNueva).append(" ").append(horaNueva);
        if (cambioMedico) obs.append(" | cambio de médico");
        if (cambioSede) obs.append(" | cambio de sede");

        historialCitaService.registrar(cita, actor, "REPROGRAMADA",
                EstadoCita.PENDIENTE, EstadoCita.PENDIENTE,
                fechaAnterior, horaAnterior, fechaNueva, horaNueva,
                obs.toString());

        return toResponse(cita);
    }

    @Transactional
    public CitaResponse cancelarCita(Long id, Long usuarioId, String observacion) {
        Cita cita = obtenerCita(id);
        Usuario actor = obtenerActor(usuarioId);

        boolean esPropietario = esPropietario(actor, cita.getPaciente());
        if (!esPropietario && !tieneRol(actor, ROL_RECEPCIONISTA)) {
            throw new RuntimeException("Solo recepción o el paciente propietario de la cita pueden cancelarla");
        }

        if (cita.getEstadoCita() == EstadoCita.ATENDIDA) {
            throw new RuntimeException("No se puede cancelar una cita que ya fue ATENDIDA");
        }
        if (cita.getEstadoCita() == EstadoCita.CANCELADA) {
            throw new RuntimeException("La cita ya se encuentra CANCELADA");
        }

        EstadoCita estadoAnterior = cita.getEstadoCita();
        cita.setEstadoCita(EstadoCita.CANCELADA);
        cita = citaRepository.save(cita);

        historialCitaService.registrar(cita, actor, "CANCELADA",
                estadoAnterior, EstadoCita.CANCELADA,
                null, null, null, null,
                observacion);

        return toResponse(cita);
    }


    @Transactional
    public CitaResponse cambiarEstado(Long id, String valor, Long usuarioId, String observacion) {
        EstadoCita nuevoEstado = parsearEstado(valor);

        if (nuevoEstado == EstadoCita.CANCELADA) {
            return cancelarCita(id, usuarioId, observacion);
        }

        Cita cita = obtenerCita(id);
        Usuario actor = obtenerActor(usuarioId);

        if (!cita.getMedico().getUsuario().getId().equals(actor.getId())) {
            throw new RuntimeException("Solo el médico asignado a la cita puede cambiar su estado");
        }

        EstadoCita estadoAnterior = cita.getEstadoCita();

        if (estadoAnterior == EstadoCita.ATENDIDA || estadoAnterior == EstadoCita.CANCELADA) {
            throw new RuntimeException("La cita está " + estadoAnterior + " y su estado ya no puede modificarse");
        }
        if (nuevoEstado == estadoAnterior) {
            throw new RuntimeException("La cita ya se encuentra en estado " + nuevoEstado);
        }
        if (nuevoEstado != EstadoCita.ATENDIDA) {
            throw new RuntimeException("Transición de estado no permitida: " + estadoAnterior + " -> " + nuevoEstado);
        }

        cita.setEstadoCita(nuevoEstado);
        cita = citaRepository.save(cita);

        historialCitaService.registrar(cita, actor, "CAMBIO_ESTADO",
                estadoAnterior, nuevoEstado,
                null, null, null, null,
                observacion);

        return toResponse(cita);
    }


    @Transactional(readOnly = true)
    public List<CitaResponse> listarCitas(LocalDate fecha, Long medicoId, Long pacienteId,
                                          Long sedeId, Long especialidadId, String estado) {

        final EstadoCita estadoFiltro = (estado == null || estado.isBlank()) ? null : parsearEstado(estado);

        return citaRepository.findAll().stream()
                .filter(c -> fecha == null || c.getFechaCita().equals(fecha))
                .filter(c -> medicoId == null || c.getMedico().getId().equals(medicoId))
                .filter(c -> pacienteId == null || c.getPaciente().getId().equals(pacienteId))
                .filter(c -> sedeId == null || c.getSede().getIdSede().equals(sedeId))
                .filter(c -> especialidadId == null || c.getEspecialidad().getIdEspecialidad().equals(especialidadId))
                .filter(c -> estadoFiltro == null || c.getEstadoCita() == estadoFiltro)
                .sorted(Comparator.comparing(Cita::getFechaCita).thenComparing(Cita::getHoraCita))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteIdOrderByFechaCitaDescHoraCitaDesc(pacienteId).stream()
                .map(this::toResponse)
                .toList();
    }


    private void validarCita(Paciente paciente, Medico medico, Especialidad especialidad, Sede sede,
                             LocalDate fecha, LocalTime hora, Long excluirCitaId) {

        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede agendar una cita en una fecha y hora pasadas");
        }
        if (sede.getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("La sede seleccionada está inactiva");
        }
        if (especialidad.getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("La especialidad seleccionada está inactiva");
        }
        if (medico.getUsuario().getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("El médico seleccionado está inactivo");
        }
        if (medico.getEspecialidad() != null
                && !medico.getEspecialidad().getIdEspecialidad().equals(especialidad.getIdEspecialidad())) {
            throw new RuntimeException("El médico seleccionado no atiende la especialidad indicada");
        }
        if (medico.getSede() != null
                && !medico.getSede().getIdSede().equals(sede.getIdSede())) {
            throw new RuntimeException("El médico seleccionado no atiende en la sede indicada");
        }

        validarDentroDeHorario(medico, especialidad, fecha, hora);

        if (citaRepository.contarCitasDelMedico(medico.getId(), fecha, hora,
                EstadoCita.CANCELADA, excluirCitaId) > 0) {
            throw new RuntimeException("El médico ya tiene una cita registrada en esa fecha y hora");
        }

        if (citaRepository.contarCitasDelPaciente(paciente.getId(), fecha, hora,
                EstadoCita.CANCELADA, excluirCitaId) > 0) {
            throw new RuntimeException("El paciente ya tiene una cita registrada en esa fecha y hora");
        }
    }

    private void validarDentroDeHorario(Medico medico, Especialidad especialidad,
                                        LocalDate fecha, LocalTime hora) {

        int diaSemana = fecha.getDayOfWeek().getValue(); // 1 = Lunes ... 7 = Domingo
        List<HorarioMedico> bloques = horarioMedicoRepository
                .findByMedicoIdAndDiaSemana(medico.getId(), diaSemana);

                Integer duracionEsp = especialidad.getDuracionCitaMin();
        final long duracion = (duracionEsp != null && duracionEsp > 0) ? duracionEsp : 1;

        boolean dentro = bloques.stream()
                .filter(b -> b.getEstado() == EstadoGenerico.ACTIVO)
                .anyMatch(b -> !hora.isBefore(b.getHoraInicio())
                        && Duration.between(hora, b.getHoraFin()).toMinutes() >= duracion);

        if (!dentro) {
            throw new RuntimeException("La fecha y hora solicitadas no están dentro de un horario de atención "
                    + "activo del médico");
        }
    }


    private void validarPuedeGestionar(Usuario actor, Paciente paciente) {
        if (esPropietario(actor, paciente)
                || tieneRol(actor, ROL_RECEPCIONISTA)
                || tieneRol(actor, ROL_ADMINISTRADOR)) {
            return;
        }
        throw new RuntimeException("No tiene permisos para gestionar las citas de este paciente");
    }

    private boolean esPropietario(Usuario actor, Paciente paciente) {
        return paciente.getUsuario().getId().equals(actor.getId());
    }

    private boolean tieneRol(Usuario usuario, String nombreRol) {
        return usuarioRolRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(ur -> ur.getEstado() == EstadoGenerico.ACTIVO
                        && nombreRol.equalsIgnoreCase(ur.getRol().getNombre()));
    }

  
    private Usuario resolverActor(Long usuarioId, Usuario porDefecto) {
        return usuarioId == null ? porDefecto : obtenerActor(usuarioId);
    }

    private Usuario obtenerActor(Long usuarioId) {
        if (usuarioId == null) {
            throw new RuntimeException("Debe indicar el usuario que realiza la acción (usuarioId)");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        if (usuario.getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("El usuario que realiza la acción está inactivo");
        }
        return usuario;
    }

    private EstadoCita parsearEstado(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RuntimeException("Debe indicar el estado de la cita");
        }
        try {
            return EstadoCita.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de cita inválido: " + valor
                    + ". Valores permitidos: PENDIENTE, ATENDIDA, CANCELADA");
        }
    }

    private Cita obtenerCita(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    private Paciente obtenerPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
    }

    private Medico obtenerMedico(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));
    }

    private Especialidad obtenerEspecialidad(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada con ID: " + id));
    }

    private Sede obtenerSede(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + id));
    }

    private CitaResponse toResponse(Cita c) {
        Usuario up = c.getPaciente().getUsuario();
        Usuario um = c.getMedico().getUsuario();
        return new CitaResponse(
                c.getIdCita(),
                c.getPaciente().getId(),
                up.getNombres() + " " + up.getApellidos(),
                up.getDni(),
                c.getMedico().getId(),
                um.getNombres() + " " + um.getApellidos(),
                c.getEspecialidad().getIdEspecialidad(),
                c.getEspecialidad().getNombre(),
                c.getSede().getIdSede(),
                c.getSede().getNombre(),
                c.getFechaCita(),
                c.getHoraCita(),
                c.getEstadoCita(),
                c.getMotivoCita(),
                c.getFechaCreacion(),
                c.getFechaActualizacion());
    }
}