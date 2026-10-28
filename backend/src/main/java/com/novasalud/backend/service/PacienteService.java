package com.novasalud.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.CitaResponse;
import com.novasalud.backend.dto.DatosPersonalesRequest;
import com.novasalud.backend.dto.PacienteRegistroRequest;
import com.novasalud.backend.dto.PacienteRequest;
import com.novasalud.backend.dto.PacienteResponse;
import com.novasalud.backend.entity.Paciente;
import com.novasalud.backend.entity.Rol;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.entity.UsuarioRol;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.PacienteRepository;
import com.novasalud.backend.repository.RolRepository;
import com.novasalud.backend.repository.UsuarioRepository;
import com.novasalud.backend.repository.UsuarioRolRepository;

@Service
public class PacienteService {

    private static final String ROL_PACIENTE = "PACIENTE";

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final CitaService citaService;

    public PacienteService(PacienteRepository pacienteRepository,
                           UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           UsuarioRolRepository usuarioRolRepository,
                           CitaService citaService) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.citaService = citaService;
    }

   
    @Transactional
    public PacienteResponse registrarPaciente(PacienteRegistroRequest request) {
        DatosPersonalesRequest datos = request.getDatosPersonales();

        if (usuarioRepository.existsByDni(datos.getDni())) {
            throw new RuntimeException("Ya existe un usuario registrado con el DNI " + datos.getDni());
        }
        if (usuarioRepository.findByCorreoIgnoreCase(datos.getCorreo().trim()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario registrado con el correo " + datos.getCorreo());
        }

        Usuario usuario = new Usuario(
                datos.getNombres().trim(),
                datos.getApellidos().trim(),
                datos.getDni(),
                datos.getTelefono(),
                datos.getCorreo().trim(),
                EstadoGenerico.ACTIVO,
                request.getContrasena(),
                datos.getFechaNacimiento());
        usuario = usuarioRepository.save(usuario);

        asignarRolPaciente(usuario);

        Paciente paciente = new Paciente(usuario, generarNumeroHistorial(usuario.getDni()), null, null);
        return toResponse(pacienteRepository.save(paciente));
    }


    @Transactional
    public PacienteResponse crearPerfilClinico(PacienteRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUsuarioId()));

        if (usuario.getEstado() != EstadoGenerico.ACTIVO) {
            throw new RuntimeException("El usuario está inactivo, no se puede crear su perfil de paciente");
        }
        if (pacienteRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new RuntimeException("El usuario ya tiene un perfil de paciente");
        }

        String historial = request.getNumeroHistorialClinica().trim();
        if (pacienteRepository.findByNumeroHistorialClinica(historial).isPresent()) {
            throw new RuntimeException("Ya existe un paciente con el número de historial clínico " + historial);
        }

        Paciente paciente = new Paciente(usuario, historial,
                request.getGrupoSanguineo(), request.getContactoEmergencia());
        paciente = pacienteRepository.save(paciente);

        asignarRolPaciente(usuario);

        return toResponse(paciente);
    }

   
    @Transactional(readOnly = true)
      public List<PacienteResponse> buscar(String dni, String nombre) {
        String dniLimpio = (dni == null) ? "" : dni.trim();
        String nombreLimpio = (nombre == null) ? "" : nombre.trim();

        boolean hayDni = !dniLimpio.isEmpty();
        boolean hayNombre = !nombreLimpio.isEmpty();

        if (!hayDni && !hayNombre) {
            throw new RuntimeException("Debe indicar el DNI o el nombre del paciente a buscar");
        }

        List<Paciente> resultado;

        if (hayDni) {
            if (!dniLimpio.matches("\\d{8}")) {
                throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
            }
            resultado = pacienteRepository.buscarPorDni(dniLimpio)
                    .map(p -> List.of(p))
                    .orElse(List.of());

            if (hayNombre) {
                String filtro = nombreLimpio.toLowerCase();
                resultado = resultado.stream()
                        .filter(p -> nombreCompleto(p.getUsuario()).toLowerCase().contains(filtro))
                        .toList();
            }
        } else {
            resultado = pacienteRepository.buscarPorNombre(nombreLimpio);
        }

        return resultado.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerHistorial(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RuntimeException("Paciente no encontrado con ID: " + pacienteId);
        }
        return citaService.listarPorPaciente(pacienteId);
    }


    private void asignarRolPaciente(Usuario usuario) {
        Rol rol = rolRepository.findByNombre(ROL_PACIENTE)
                .orElseThrow(() -> new RuntimeException("El rol " + ROL_PACIENTE + " no existe en el sistema"));

        UsuarioRol existente = usuarioRolRepository.findByUsuarioIdAndRolId(usuario.getId(), rol.getId())
                .orElse(null);

        if (existente == null) {
            usuarioRolRepository.save(new UsuarioRol(usuario, rol, EstadoGenerico.ACTIVO));
        } else if (existente.getEstado() != EstadoGenerico.ACTIVO) {
            existente.setEstado(EstadoGenerico.ACTIVO);
            usuarioRolRepository.save(existente);
        }
    }

    private String generarNumeroHistorial(String dni) {
        String base = "HC-" + dni;
        if (pacienteRepository.findByNumeroHistorialClinica(base).isEmpty()) {
            return base;
        }
        return base + "-" + (System.currentTimeMillis() % 1000);
    }

    private String nombreCompleto(Usuario u) {
        return u.getNombres() + " " + u.getApellidos();
    }

    private PacienteResponse toResponse(Paciente p) {
        Usuario u = p.getUsuario();
        return new PacienteResponse(
                p.getId(),
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getDni(),
                u.getTelefono(),
                u.getCorreo(),
                u.getFechaNacimiento(),
                p.getNumeroHistorialClinica(),
                p.getGrupoSanguineo(),
                p.getContactoEmergencia());
    }
}
