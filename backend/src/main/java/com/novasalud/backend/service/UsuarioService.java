package com.novasalud.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novasalud.backend.dto.DatosPersonalesRequest;
import com.novasalud.backend.dto.EmpleadoRegistroRequest;
import com.novasalud.backend.dto.UsuarioResponse;
import com.novasalud.backend.dto.UsuarioRolRequest;
import com.novasalud.backend.dto.UsuarioRolResponse;
import com.novasalud.backend.entity.Rol;
import com.novasalud.backend.entity.Usuario;
import com.novasalud.backend.entity.UsuarioRol;
import com.novasalud.backend.enums.EstadoGenerico;
import com.novasalud.backend.repository.RolRepository;
import com.novasalud.backend.repository.UsuarioRepository;
import com.novasalud.backend.repository.UsuarioRolRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Transactional
    public UsuarioResponse crearEmpleado(EmpleadoRegistroRequest request) {
        DatosPersonalesRequest datos = request.getDatosPersonales();
        if (usuarioRepository.existsByCorreoIgnoreCase(datos.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByDni(datos.getDni())) {
            throw new RuntimeException("Ya existe un usuario con ese DNI");
        }
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        if ("MEDICO".equals(rol.getNombre()) || "PACIENTE".equals(rol.getNombre())) {
            throw new RuntimeException("El rol " + rol.getNombre() + " no corresponde a un empleado, use su registro específico");
        }

        String clave = datos.getApellidos().replaceAll("\\s+", "") + "2026";
        Usuario usuario = new Usuario(datos.getNombres(), datos.getApellidos(), datos.getDni(),
                datos.getTelefono(), datos.getCorreo(), EstadoGenerico.ACTIVO, clave,
                datos.getFechaNacimiento());
        usuario = usuarioRepository.save(usuario);
        UsuarioRol usuarioRol = usuarioRolRepository.save(new UsuarioRol(usuario, rol, EstadoGenerico.ACTIVO));
        usuario.getUsuarioRoles().add(usuarioRol);
        return respuesta(usuario);
    }

    @Transactional
    public UsuarioRolResponse asignarRol(UsuarioRolRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        UsuarioRol usuarioRol = usuarioRolRepository.findByUsuarioIdAndRolId(usuario.getId(), rol.getId())
                .orElse(null);
        if (usuarioRol != null && usuarioRol.getEstado() == EstadoGenerico.ACTIVO) {
            throw new RuntimeException("El usuario ya tiene asignado el rol " + rol.getNombre());
        }
        if (usuarioRol == null) {
            usuarioRol = new UsuarioRol(usuario, rol, EstadoGenerico.ACTIVO);
        } else {
            usuarioRol.setEstado(EstadoGenerico.ACTIVO);
        }
        usuarioRol = usuarioRolRepository.save(usuarioRol);
        return new UsuarioRolResponse(usuarioRol.getId(), usuario.getId(), rol.getId(), rol.getNombre(),
                usuarioRol.getEstado());
    }

    UsuarioResponse respuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombres(), u.getApellidos(), u.getDni(), u.getTelefono(),
                u.getCorreo(), u.getFechaNacimiento(), u.getEstado(),
                u.getUsuarioRoles().stream()
                        .filter(ur -> ur.getEstado() == EstadoGenerico.ACTIVO)
                        .map(ur -> ur.getRol().getNombre())
                        .toList());
    }
}
