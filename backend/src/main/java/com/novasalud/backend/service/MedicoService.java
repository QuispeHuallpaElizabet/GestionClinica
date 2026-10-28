package com.novasalud.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.AgendaCitaResponse;
import com.novasalud.backend.dto.DisponibilidadResponse;
import com.novasalud.backend.dto.MedicoActualizacionRequest;
import com.novasalud.backend.dto.MedicoRegistroRequest;
import com.novasalud.backend.dto.MedicoResponse;
import com.novasalud.backend.entity.Cita;
import com.novasalud.backend.entity.Especialidad;
import com.novasalud.backend.entity.HorarioMedico;
import com.novasalud.backend.entity.Medico;
import com.novasalud.backend.entity.Rol;
import com.novasalud.backend.entity.Sede;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.entity.UsuarioRol;
import com.novasalud.backend.enums.EstadoCita;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.CitaRepository;
import com.novasalud.backend.repository.EspecialidadRepository;
import com.novasalud.backend.repository.HorarioMedicoRepository;
import com.novasalud.backend.repository.MedicoRepository;
import com.novasalud.backend.repository.RolRepository;
import com.novasalud.backend.repository.SedeRepository;
import com.novasalud.backend.repository.UsuarioRepository;
import com.novasalud.backend.repository.UsuarioRolRepository;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;
    private final SedeRepository sedeRepository;
    private final EspecialidadRepository especialidadRepository;
    private final HorarioMedicoRepository horarioRepository;
    private final CitaRepository citaRepository;

    public MedicoService(MedicoRepository medicoRepository, UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository, RolRepository rolRepository,
            SedeRepository sedeRepository, EspecialidadRepository especialidadRepository,
            HorarioMedicoRepository horarioRepository, CitaRepository citaRepository) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
        this.sedeRepository = sedeRepository;
        this.especialidadRepository = especialidadRepository;
        this.horarioRepository = horarioRepository;
        this.citaRepository = citaRepository;
    }

    @Transactional
    public MedicoResponse crear(MedicoRegistroRequest request) {
        var datos = request.getDatosPersonales();
        if (usuarioRepository.existsByCorreoIgnoreCase(datos.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByDni(datos.getDni())) {
            throw new RuntimeException("Ya existe un usuario con ese DNI");
        }
        if (medicoRepository.existsByNumeroColegiatura(request.getNumeroColegiatura())) {
            throw new RuntimeException("Ya existe un médico con ese número de colegiatura");
        }

        Sede sede = obtenerSedeActiva(request.getSedeId());
        Especialidad especialidad = obtenerEspecialidadActiva(request.getEspecialidadId());
        Rol rolMedico = rolRepository.findByNombre("MEDICO")
                .orElseThrow(() -> new RuntimeException("No existe el rol MEDICO"));

        String clave = datos.getApellidos().replaceAll("\\s+", "") + "2026";
        Usuario usuario = new Usuario(datos.getNombres(), datos.getApellidos(), datos.getDni(),
                datos.getTelefono(), datos.getCorreo(), EstadoGenerico.ACTIVO, clave,
                datos.getFechaNacimiento());
        usuario = usuarioRepository.save(usuario);

        Medico medico = new Medico(usuario, especialidad, sede, request.getNumeroColegiatura().trim());
        medico.setEstado(EstadoGenerico.ACTIVO);
        medico = medicoRepository.save(medico);
        usuarioRolRepository.save(new UsuarioRol(usuario, rolMedico, EstadoGenerico.ACTIVO));
        return respuesta(medico);
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> listar() {
        return medicoRepository.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public MedicoResponse obtener(Long id) {
        return respuesta(medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado")));
    }

    @Transactional
    public MedicoResponse actualizar(Long id, MedicoActualizacionRequest request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        String colegiatura = request.getNumeroColegiatura().trim();
        if (!medico.getNumeroColegiatura().equalsIgnoreCase(colegiatura)
                && medicoRepository.existsByNumeroColegiatura(colegiatura)) {
            throw new RuntimeException("Ya existe otro médico con esa colegiatura");
        }
        medico.setEspecialidad(obtenerEspecialidadActiva(request.getEspecialidadId()));
        medico.setSede(obtenerSedeActiva(request.getSedeId()));
        medico.setNumeroColegiatura(colegiatura);
        return respuesta(medicoRepository.save(medico));
    }

    @Transactional
    public MedicoResponse cambiarEstado(Long id, boolean activo) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        EstadoGenerico estado = activo ? EstadoGenerico.ACTIVO : EstadoGenerico.INACTIVO;
        medico.setEstado(estado);
        medico.getUsuario().setEstado(estado);
        usuarioRepository.save(medico.getUsuario());
        return respuesta(medicoRepository.save(medico));
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> disponibilidad(LocalDate fecha, Long sedeId, Long especialidadId) {
        if (fecha == null || sedeId == null || especialidadId == null) {
            throw new RuntimeException("Fecha, sedeId y especialidadId son obligatorios");
        }
        Sede sede = obtenerSedeActiva(sedeId);
        Especialidad especialidad = obtenerEspecialidadActiva(especialidadId);
        int dia = fecha.getDayOfWeek().getValue();
        List<DisponibilidadResponse> libres = new ArrayList<>();

        for (Medico medico : medicoRepository.findByEstado(EstadoGenerico.ACTIVO)) {
            if (medico.getSede() == null || medico.getEspecialidad() == null
                    || !medico.getSede().getIdSede().equals(sedeId)
                    || !medico.getEspecialidad().getIdEspecialidad().equals(especialidadId)
                    || medico.getUsuario().getEstado() != EstadoGenerico.ACTIVO) {
                continue;
            }
            List<Cita> citas = citaRepository.findByMedicoIdAndFechaCita(medico.getId(), fecha).stream()
                    .filter(c -> c.getEstadoCita() != EstadoCita.CANCELADA).toList();
            for (HorarioMedico horario : horarioRepository.findByMedicoIdAndDiaSemanaAndEstado(
                    medico.getId(), dia, EstadoGenerico.ACTIVO)) {
                LocalTime inicio = horario.getHoraInicio();
                while (!inicio.plusMinutes(especialidad.getDuracionCitaMin()).isAfter(horario.getHoraFin())) {
                    LocalTime fin = inicio.plusMinutes(especialidad.getDuracionCitaMin());
                    LocalTime slotInicio = inicio;
                    boolean ocupado = citas.stream().anyMatch(c -> {
                        int duracion = c.getEspecialidad() == null
                                ? especialidad.getDuracionCitaMin() : c.getEspecialidad().getDuracionCitaMin();
                        LocalTime citaFin = c.getHoraCita().plusMinutes(duracion);
                        return slotInicio.isBefore(citaFin) && fin.isAfter(c.getHoraCita());
                    });
                    if (!ocupado) {
                        Usuario u = medico.getUsuario();
                        libres.add(new DisponibilidadResponse(medico.getId(),
                                u.getNombres() + " " + u.getApellidos(), especialidad.getIdEspecialidad(),
                                especialidad.getNombre(), sede.getIdSede(), sede.getNombre(), fecha, inicio, fin));
                    }
                    inicio = fin;
                }
            }
        }
        libres.sort(Comparator.comparing(DisponibilidadResponse::horaInicio));
        return libres;
    }

    @Transactional(readOnly = true)
    public List<AgendaCitaResponse> agenda(Long medicoId, LocalDate fecha, String vista) {
        if (fecha == null) throw new RuntimeException("La fecha es obligatoria");
        LocalDate desde = fecha;
        LocalDate hasta = fecha;
        if ("semanal".equalsIgnoreCase(vista)) {
            desde = fecha.with(DayOfWeek.MONDAY);
            hasta = fecha.with(DayOfWeek.SUNDAY);
        } else if (vista != null && !"diaria".equalsIgnoreCase(vista)) {
            throw new RuntimeException("La vista debe ser diaria o semanal");
        }
        if (!medicoRepository.existsById(medicoId)) throw new RuntimeException("Médico no encontrado");
        return citaRepository.findByMedicoIdAndFechaCitaBetween(medicoId, desde, hasta).stream()
                .sorted(Comparator.comparing(Cita::getFechaCita).thenComparing(Cita::getHoraCita))
                .map(c -> new AgendaCitaResponse(c.getIdCita(), c.getFechaCita(), c.getHoraCita(),
                        c.getPaciente().getUsuario().getNombres() + " " + c.getPaciente().getUsuario().getApellidos(),
                        c.getPaciente().getUsuario().getDni(), c.getEspecialidad().getNombre(),
                        c.getSede().getNombre(), c.getEstadoCita(), c.getMotivoCita()))
                .toList();
    }

    private Sede obtenerSedeActiva(Long id) {
        Sede sede = sedeRepository.findById(id).orElseThrow(() -> new RuntimeException("Sede no encontrada"));
        if (sede.getEstado() != EstadoGenerico.ACTIVO) throw new RuntimeException("La sede está inactiva");
        return sede;
    }

    private Especialidad obtenerEspecialidadActiva(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
        if (especialidad.getEstado() != EstadoGenerico.ACTIVO) throw new RuntimeException("La especialidad está inactiva");
        return especialidad;
    }

    private MedicoResponse respuesta(Medico m) {
        Usuario u = m.getUsuario();
        return new MedicoResponse(m.getId(), u.getId(), u.getNombres(), u.getApellidos(), u.getDni(),
                u.getCorreo(), u.getTelefono(), m.getNumeroColegiatura(),
                m.getEspecialidad() == null ? null : m.getEspecialidad().getIdEspecialidad(),
                m.getEspecialidad() == null ? null : m.getEspecialidad().getNombre(),
                m.getSede() == null ? null : m.getSede().getIdSede(),
                m.getSede() == null ? null : m.getSede().getNombre(), m.getEstado());
    }
}
