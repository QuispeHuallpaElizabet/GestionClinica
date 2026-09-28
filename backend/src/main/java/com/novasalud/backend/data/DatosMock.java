package com.novasalud.backend.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.novasalud.backend.models.Cita;
import com.novasalud.backend.models.Especialidad;
import com.novasalud.backend.models.HorarioMedico;
import com.novasalud.backend.models.Medico;
import com.novasalud.backend.models.Paciente;
import com.novasalud.backend.models.Rol;
import com.novasalud.backend.models.Sede;
import com.novasalud.backend.models.Usuario;

@Component
public class DatosMock {

    /* ================== Listas en memoria ================== */
    public final List<Rol>          roles          = new ArrayList<>();
    public final List<Usuario>      usuarios       = new ArrayList<>();
    public final List<Sede>         sedes          = new ArrayList<>();
    public final List<Especialidad> especialidades = new ArrayList<>();
    public final List<Medico> medicos              = new ArrayList<>();
    public final List<Paciente>       pacientes = new ArrayList<>();
    public final List<HorarioMedico>  horarios  = new ArrayList<>();
    public final List<Cita>           citas     = new ArrayList<>();
    public final List<Map<String,Object>> historialCitas = new ArrayList<>();

    /* ================== Secuencias (simulan IDENTITY) ================== */
    public final AtomicInteger seqUsuario       = new AtomicInteger(0);
    public final AtomicInteger seqSede          = new AtomicInteger(0);
    public final AtomicInteger seqEspecialidad  = new AtomicInteger(0);
    public final AtomicInteger seqMedico        = new AtomicInteger(0);
    public final AtomicInteger seqPaciente = new AtomicInteger(0);
    public final AtomicInteger seqHorario  = new AtomicInteger(0);
    public final AtomicInteger seqCita     = new AtomicInteger(0);
    public final AtomicInteger seqHistorial = new AtomicInteger(0);

    public DatosMock() {
        cargarRoles();
        cargarUsuarios();
        cargarSedes();
        cargarEspecialidades();
        cargarMedicos(); 
        cargarPacientes();
        cargarHorarios();
        cargarCitasDemo();
    }

    /* ================== Semillas ================== */

    private void cargarRoles() {
        // Coincide con el INSERT del script_bd.txt
        roles.add(new Rol(1, "ADMINISTRADOR"));
        roles.add(new Rol(2, "RECEPCIONISTA"));
        roles.add(new Rol(3, "MEDICO"));
        roles.add(new Rol(4, "PACIENTE"));
    }

    private void cargarUsuarios() {
        // Un usuario por cada rol, todos ACTIVO y sin cambio de contraseña forzado.
        usuarios.add(new Usuario(
                seqUsuario.incrementAndGet(),
                "Admin", "NovaSalud", "00000000", "900000000",
                "admin@novasalud.com", "admin123",
                "ACTIVO", false,
                List.of(roles.get(0))
        ));

        usuarios.add(new Usuario(
                seqUsuario.incrementAndGet(),
                "Carlos", "Ramírez", "87654321", "912345678",
                "carlos@novasalud.com", "123456",
                "ACTIVO", false,
                List.of(roles.get(1))
        ));

        usuarios.add(new Usuario(
                seqUsuario.incrementAndGet(),
                "María", "Gómez", "45678912", "999888777",
                "maria@novasalud.com", "123456",
                "ACTIVO", false,
                List.of(roles.get(2))
        ));

        usuarios.add(new Usuario(
                seqUsuario.incrementAndGet(),
                "Ana", "Torres", "12345678", "987654321",
                "ana@novasalud.com", "123456",
                "ACTIVO", false,
                List.of(roles.get(3))
        ));

        usuarios.add(new Usuario(
                seqUsuario.incrementAndGet(),
                "Lina", "Torres", "12345778", "987654321",
                "lina@novasalud.com", "123456",
                "ACTIVO", true,
                List.of(roles.get(3))
        ));
    }

    private void cargarSedes() {
        sedes.add(new Sede(
                seqSede.incrementAndGet(),
                "Sede Central",
                "Av. Principal 123, Lima",
                "014567890",
                true
        ));

        sedes.add(new Sede(
                seqSede.incrementAndGet(),
                "Sede Norte",
                "Av. Los Álamos 456, Lima",
                "014567891",
                true
        ));
    }

    private void cargarEspecialidades() {
        // duracionCitaMin > 0 (respeta el CHECK del script)
        especialidades.add(new Especialidad(
                seqEspecialidad.incrementAndGet(),
                "Cardiología",
                30,
                true
        ));

        especialidades.add(new Especialidad(
                seqEspecialidad.incrementAndGet(),
                "Medicina General",
                20,
                true
        ));

        especialidades.add(new Especialidad(
                seqEspecialidad.incrementAndGet(),
                "Pediatría",
                25,
                true
        ));
    }

    private void cargarMedicos() {
        /* María Gómez ya existe en usuarios con rol MEDICO.
        La registramos también como médica, asociada a Cardiología y Sede Central. */
        Usuario maria = usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase("maria@novasalud.com"))
                .findFirst()
                .orElse(null);

        if (maria != null) {
            medicos.add(new Medico(
                    maria.getIdUsuario(),
                    maria.getNombres(), maria.getApellidos(),
                    maria.getDni(), maria.getTelefono(), maria.getCorreo(),
                    maria.getContrasena(),
                    true,
                    1,   // idEspecialidad → Cardiología
                    1,   // idSede         → Sede Central
                    "CMP-10001"
            ));
            seqMedico.incrementAndGet();
        }
    }

    private void cargarPacientes() {
        // Ana ya está en usuarios con rol PACIENTE
        Usuario ana = usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase("ana@novasalud.com"))
                .findFirst().orElse(null);

        if (ana != null) {
            pacientes.add(new Paciente(
                    ana.getIdUsuario(),
                    ana.getNombres(), ana.getApellidos(),
                    ana.getDni(), ana.getTelefono(), ana.getCorreo(),
                    ana.getContrasena(), true,
                    "1995-04-12",
                    "Av. Los Pinos 789"
            ));
            seqPaciente.incrementAndGet();
        }
    }

    private void cargarHorarios() {
        // María Gómez (idUsuario=3) atiende L–V de 08:00 a 13:00
        for (int dia = 1; dia <= 5; dia++) {
            horarios.add(new HorarioMedico(
                    seqHorario.incrementAndGet(),
                    3, dia,
                    "08:00", "13:00",
                    true
            ));
        }
}

private void cargarCitasDemo() {
        String hoy = java.time.LocalDate.now().toString();

        citas.add(new Cita(
                seqCita.incrementAndGet(),
                4, 3, 1, 1,
                hoy, "09:00", "Confirmada",
                "Chequeo anual", hoy + "T00:00:00", hoy + "T00:00:00",
                null
        ));

        citas.add(new Cita(
                seqCita.incrementAndGet(),
                4, 3, 1, 1,
                hoy, "10:00", "Pendiente",
                "Control de presión", hoy + "T00:00:00", hoy + "T00:00:00",
                null
        ));
    }
}