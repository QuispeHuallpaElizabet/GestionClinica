(() => {
    /* ================== Configuración ================== */
    const TITULOS_VISTA = {
        inicio:          'Inicio',
        sedes:           'Sedes',
        especialidades:  'Especialidades',
        medicos:         'Médicos',
        usuarios:        'Usuarios',
        parametros:      'Duración de citas',
        perfil:          'Mi perfil'
    };

    /* ================== Navegación entre apartados ================== */
    function mostrarVista(seccion) {
        document.querySelectorAll('[data-vista]').forEach(vista => {
            vista.classList.toggle('d-none', vista.dataset.vista !== seccion);
        });
        document.querySelectorAll('.nav-link[data-seccion]').forEach(enlace => {
            enlace.classList.toggle('active', enlace.dataset.seccion === seccion);
        });
        document.getElementById('tituloVista').textContent = TITULOS_VISTA[seccion] || 'Inicio';
    }

    /* ================== Mensajes flotantes ================== */
    const toast = new bootstrap.Toast(document.getElementById('mensajeAdmin'));
    function mostrarMensaje(texto) {
        document.getElementById('mensajeAdmin').querySelector('.toast-body').textContent = texto;
        toast.show();
    }

    /* ================== Modales ================== */
    const modales = {
        sede:          new bootstrap.Modal(document.getElementById('modalSede')),
        especialidad:  new bootstrap.Modal(document.getElementById('modalEspecialidad')),
        medico:        new bootstrap.Modal(document.getElementById('modalMedico')),
        usuario:       new bootstrap.Modal(document.getElementById('modalUsuario')),
        horario:       new bootstrap.Modal(document.getElementById('modalHorario')),
        duracion:      new bootstrap.Modal(document.getElementById('modalDuracion'))
    };

    document.querySelectorAll('[data-seccion]').forEach(enlace => {
        enlace.addEventListener('click', evento => {
            evento.preventDefault();
            const seccion = enlace.dataset.seccion;
            mostrarVista(seccion);

            if (seccion === 'sedes')          cargarSedes();
            if (seccion === 'especialidades') cargarEspecialidades();
            if (seccion === 'medicos')        cargarMedicos();
            if (seccion === 'horarios')       cargarHorariosInicial();
            if (seccion === 'parametros') cargarDuraciones();
        });
    });

    /* ================== Filtros de usuarios ================== */
    ['filtroBusquedaUsuarios', 'filtroRol', 'filtroEstado'].forEach(id => {
        document.getElementById(id).addEventListener('input', () => {
            // TODO: recargar tabla de usuarios aplicando filtros vía API
        });
    });

    /* ================== Constantes compartidas ================== */
const API_BASE = "http://localhost:8080/api";

function escaparHtml(v) {
    return String(v ?? "").replace(/[&<>"']/g, c => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[c]);
}

/* ================== SEDES ================== */

async function cargarSedes() {
    const tbody = document.getElementById("tablaSedes");
    if (!tbody) return;
    try {
        const r = await fetch(`${API_BASE}/sedes`);
        const sedes = await r.json();
        if (!sedes.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="tabla-vacia">No hay sedes registradas.</td></tr>`;
            return;
        }
        tbody.innerHTML = sedes.map(s => `
            <tr>
                <td class="fw-semibold">${escaparHtml(s.nombre)}</td>
                <td>${escaparHtml(s.direccion)}</td>
                <td>${escaparHtml(s.telefono)}</td>
                <td><span class="etiqueta-estado ${s.estado ? 'estado-atendida' : 'estado-cancelada'}">${s.estado ? 'Activa' : 'Inactiva'}</span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary" data-accion="editar-sede" data-id="${s.idSede}">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm ${s.estado ? 'btn-outline-danger' : 'btn-outline-success'}"
                            data-accion="toggle-sede" data-id="${s.idSede}" data-activo="${s.estado}">
                        <i class="bi ${s.estado ? 'bi-slash-circle' : 'bi-check-circle'}"></i>
                    </button>
                </td>
            </tr>`).join("");
    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="5" class="tabla-vacia">Error al cargar sedes.</td></tr>`;
    }
}

document.getElementById("formSede")?.addEventListener("submit", async e => {
    e.preventDefault();
    const id = document.getElementById("sedeId").value;
    const payload = {
        nombre:    document.getElementById("sedeNombre").value.trim(),
        direccion: document.getElementById("sedeDireccion").value.trim(),
        telefono:  document.getElementById("sedeTelefono").value.trim(),
        estado:    document.getElementById("sedeActivo").checked
    };
    const url    = id ? `${API_BASE}/sedes/${id}` : `${API_BASE}/sedes`;
    const method = id ? "PUT" : "POST";
    const r = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!r.ok) { mostrarMensaje("No se pudo guardar la sede."); return; }
    modales.sede.hide();
    mostrarMensaje(id ? "Sede actualizada." : "Sede registrada.");
    cargarSedes();
});

document.getElementById("tablaSedes")?.addEventListener("click", async e => {
    const b = e.target.closest("[data-accion]");
    if (!b) return;
    const id = b.dataset.id;

    if (b.dataset.accion === "editar-sede") {
        const r = await fetch(`${API_BASE}/sedes/${id}`);
        const s = await r.json();
        document.getElementById("tituloModalSede").textContent = "Editar sede";
        document.getElementById("sedeId").value        = s.idSede;
        document.getElementById("sedeNombre").value    = s.nombre;
        document.getElementById("sedeDireccion").value = s.direccion;
        document.getElementById("sedeTelefono").value  = s.telefono;
        document.getElementById("sedeActivo").checked  = s.estado;
        modales.sede.show();
    }

    if (b.dataset.accion === "toggle-sede") {
        const activo = b.dataset.activo === "true";
        await fetch(`${API_BASE}/sedes/${id}/estado?activo=${!activo}`, { method: "PATCH" });
        cargarSedes();
    }
});

/* ================== ESPECIALIDADES ================== */

async function cargarEspecialidades() {
    const tbody = document.getElementById("tablaEspecialidades");
    if (!tbody) return;
    try {
        const r = await fetch(`${API_BASE}/especialidades`);
        const lista = await r.json();
        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="4" class="tabla-vacia">No hay especialidades registradas.</td></tr>`;
            return;
        }
        tbody.innerHTML = lista.map(esp => `
            <tr>
                <td class="fw-semibold">${escaparHtml(esp.nombre)}</td>
                <td>${esp.duracionCitaMin} min</td>
                <td><span class="etiqueta-estado ${esp.estado ? 'estado-atendida' : 'estado-cancelada'}">${esp.estado ? 'Activa' : 'Inactiva'}</span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary" data-accion="editar-especialidad" data-id="${esp.idEspecialidad}">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm ${esp.estado ? 'btn-outline-danger' : 'btn-outline-success'}"
                            data-accion="toggle-especialidad" data-id="${esp.idEspecialidad}" data-activo="${esp.estado}">
                        <i class="bi ${esp.estado ? 'bi-slash-circle' : 'bi-check-circle'}"></i>
                    </button>
                </td>
            </tr>`).join("");
    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="4" class="tabla-vacia">Error al cargar especialidades.</td></tr>`;
    }
}

document.getElementById("formEspecialidad")?.addEventListener("submit", async e => {
    e.preventDefault();
    const id = document.getElementById("especialidadId").value;
    const payload = {
        nombre:          document.getElementById("especialidadNombre").value.trim(),
        duracionCitaMin: Number(document.getElementById("especialidadDuracion").value),
        estado:          document.getElementById("especialidadActivo").checked
    };
    const url    = id ? `${API_BASE}/especialidades/${id}` : `${API_BASE}/especialidades`;
    const method = id ? "PUT" : "POST";
    const r = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!r.ok) { mostrarMensaje("No se pudo guardar la especialidad."); return; }
    modales.especialidad.hide();
    mostrarMensaje(id ? "Especialidad actualizada." : "Especialidad registrada.");
    cargarEspecialidades();
});

document.getElementById("tablaEspecialidades")?.addEventListener("click", async e => {
    const b = e.target.closest("[data-accion]");
    if (!b) return;
    const id = b.dataset.id;

    if (b.dataset.accion === "editar-especialidad") {
        const r = await fetch(`${API_BASE}/especialidades/${id}`);
        const esp = await r.json();
        document.getElementById("tituloModalEspecialidad").textContent = "Editar especialidad";
        document.getElementById("especialidadId").value       = esp.idEspecialidad;
        document.getElementById("especialidadNombre").value   = esp.nombre;
        document.getElementById("especialidadDuracion").value = esp.duracionCitaMin;
        document.getElementById("especialidadActivo").checked = esp.estado;
        modales.especialidad.show();
    }

    if (b.dataset.accion === "toggle-especialidad") {
        const activo = b.dataset.activo === "true";
        await fetch(`${API_BASE}/especialidades/${id}/estado?activo=${!activo}`, { method: "PATCH" });
        cargarEspecialidades();
    }
});

/* ================== MÉDICOS ================== */

async function llenarSelectsMedico() {
    const selEsp  = document.getElementById("medicoEspecialidad");
    const selSede = document.getElementById("medicoSede");
    if (!selEsp || !selSede) return;

    const [rEsp, rSede] = await Promise.all([
        fetch(`${API_BASE}/especialidades`),
        fetch(`${API_BASE}/sedes`)
    ]);
    const esp  = await rEsp.json();
    const sedes = await rSede.json();

    selEsp.innerHTML  = esp.map(e => `<option value="${e.idEspecialidad}">${escaparHtml(e.nombre)}</option>`).join("");
    selSede.innerHTML = sedes.map(s => `<option value="${s.idSede}">${escaparHtml(s.nombre)}</option>`).join("");
}

async function cargarMedicos() {
    const tbody = document.getElementById("tablaMedicos");
    if (!tbody) return;
    try {
        const r = await fetch(`${API_BASE}/medicos`);
        const lista = await r.json();
        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="6" class="tabla-vacia">No hay médicos registrados.</td></tr>`;
            return;
        }
        tbody.innerHTML = lista.map(m => `
            <tr>
                <td>
                    <div class="paciente-nombre">${escaparHtml(m.nombres)} ${escaparHtml(m.apellidos)}</div>
                    <div class="paciente-documento">${escaparHtml(m.numeroColegiatura)}</div>
                </td>
                <td>${escaparHtml(m.idEspecialidad)}</td>
                <td>${escaparHtml(m.idSede)}</td>
                <td>
                    ${escaparHtml(m.correo)}<br>
                    <small class="text-secondary">${escaparHtml(m.telefono)}</small>
                </td>
                <td><span class="etiqueta-estado ${m.activo ? 'estado-atendida' : 'estado-cancelada'}">${m.activo ? 'Activo' : 'Inactivo'}</span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary" data-accion="editar-medico" data-id="${m.idUsuario}">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm ${m.activo ? 'btn-outline-danger' : 'btn-outline-success'}"
                            data-accion="toggle-medico" data-id="${m.idUsuario}" data-activo="${m.activo}">
                        <i class="bi ${m.activo ? 'bi-slash-circle' : 'bi-check-circle'}"></i>
                    </button>
                </td>
            </tr>`).join("");
    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="6" class="tabla-vacia">Error al cargar médicos.</td></tr>`;
    }
}

document.querySelectorAll(".btn-nuevo-medico").forEach(b =>
    b.addEventListener("click", async () => {
        document.getElementById("formMedico").reset();
        document.getElementById("medicoId").value = "";
        document.getElementById("tituloModalMedico").textContent = "Nuevo médico";
        document.getElementById("medicoActivo").checked = true;
        await llenarSelectsMedico();
    })
);

document.getElementById("formMedico")?.addEventListener("submit", async e => {
    e.preventDefault();
    const id = document.getElementById("medicoId").value;
    const payload = {
        nombres:         document.getElementById("medicoNombres").value.trim(),
        apellidos:       document.getElementById("medicoApellidos").value.trim(),
        correo:          document.getElementById("medicoEmail").value.trim(),
        telefono:        document.getElementById("medicoTelefono").value.trim(),
        idEspecialidad:  Number(document.getElementById("medicoEspecialidad").value),
        idSede:          Number(document.getElementById("medicoSede").value),
        activo:          document.getElementById("medicoActivo").checked,
        contrasena:      id ? null : "123456"
    };
    const url    = id ? `${API_BASE}/medicos/${id}` : `${API_BASE}/medicos`;
    const method = id ? "PUT" : "POST";
    const r = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!r.ok) { mostrarMensaje("No se pudo guardar el médico."); return; }
    modales.medico.hide();
    mostrarMensaje(id ? "Médico actualizado." : "Médico registrado.");
    cargarMedicos();
});

document.getElementById("tablaMedicos")?.addEventListener("click", async e => {
    const b = e.target.closest("[data-accion]");
    if (!b) return;
    const id = b.dataset.id;

    if (b.dataset.accion === "editar-medico") {
        await llenarSelectsMedico();
        const r = await fetch(`${API_BASE}/medicos/${id}`);
        const m = await r.json();
        document.getElementById("tituloModalMedico").textContent = "Editar médico";
        document.getElementById("medicoId").value                 = m.idUsuario;
        document.getElementById("medicoNombres").value            = m.nombres;
        document.getElementById("medicoApellidos").value          = m.apellidos;
        document.getElementById("medicoEmail").value              = m.correo;
        document.getElementById("medicoTelefono").value           = m.telefono;
        document.getElementById("medicoEspecialidad").value       = m.idEspecialidad;
        document.getElementById("medicoSede").value               = m.idSede;
        document.getElementById("medicoActivo").checked           = m.activo;
        modales.medico.show();
    }

    if (b.dataset.accion === "toggle-medico") {
        const activo = b.dataset.activo === "true";
        await fetch(`${API_BASE}/medicos/${id}/estado?activo=${!activo}`, { method: "PATCH" });
        cargarMedicos();
    }
});

    /* ================== HORARIOS ================== */
    const DIAS_SEMANA = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

    async function cargarSelectMedicosHorario() {
        const selFiltro = document.getElementById('horarioMedicoFiltro');
        const selModal  = document.getElementById('horarioMedico');
        if (!selFiltro && !selModal) return;

        const r = await fetch(`${API_BASE}/medicos`);
        const medicos = await r.json();
        const opciones = medicos.map(m =>
            `<option value="${m.idUsuario}">${escaparHtml(m.nombres)} ${escaparHtml(m.apellidos)}</option>`
        ).join('');

        if (selFiltro) selFiltro.innerHTML = opciones;
        if (selModal)  selModal.innerHTML  = opciones;
    }

    async function cargarHorariosInicial() {
        await cargarSelectMedicosHorario();
        await cargarHorarios();
    }

    async function cargarHorarios() {
        const tbody = document.getElementById('tablaHorarios');
        if (!tbody) return;

        const idMedico = document.getElementById('horarioMedicoFiltro')?.value;
        if (!idMedico) {
            tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">Selecciona un médico.</td></tr>';
            return;
        }

        try {
            const r = await fetch(`${API_BASE}/horarios/medico/${idMedico}`);
            const horarios = await r.json();

            if (!horarios.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">Este médico no tiene horarios registrados.</td></tr>';
                return;
            }

            tbody.innerHTML = horarios
                .slice()
                .sort((a, b) => a.diaSemana - b.diaSemana || a.horaInicio.localeCompare(b.horaInicio))
                .map(h => `
                    <tr>
                        <td class="fw-semibold">${DIAS_SEMANA[h.diaSemana] || h.diaSemana}</td>
                        <td class="font-monospace">${escaparHtml(h.horaInicio)}</td>
                        <td class="font-monospace">${escaparHtml(h.horaFin)}</td>
                        <td><span class="etiqueta-estado ${h.estado ? 'estado-atendida' : 'estado-cancelada'}">${h.estado ? 'Activo' : 'Inactivo'}</span></td>
                        <td class="text-end">
                            <button class="btn btn-sm btn-outline-primary" data-accion="editar-horario" data-id="${h.idHorario}">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm ${h.estado ? 'btn-outline-danger' : 'btn-outline-success'}"
                                    data-accion="toggle-horario" data-id="${h.idHorario}" data-activo="${h.estado}">
                                <i class="bi ${h.estado ? 'bi-slash-circle' : 'bi-check-circle'}"></i>
                            </button>
                        </td>
                    </tr>`).join('');
        } catch (e) {
            console.error(e);
            tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">Error al cargar horarios.</td></tr>';
        }
    }

    document.getElementById('horarioMedicoFiltro')?.addEventListener('change', cargarHorarios);

    document.querySelectorAll('.btn-nuevo-horario').forEach(boton =>
        boton.addEventListener('click', async () => {
            document.getElementById('formHorario').reset();
            document.getElementById('horarioId').value = '';
            document.getElementById('tituloModalHorario').textContent = 'Nuevo horario';
            await cargarSelectMedicosHorario();
        })
    );

    document.getElementById('formHorario')?.addEventListener('submit', async evento => {
        evento.preventDefault();
        const id = document.getElementById('horarioId').value;
        const payload = {
            idMedico:   Number(document.getElementById('horarioMedico').value),
            diaSemana:  Number(document.getElementById('horarioDia').value),
            horaInicio: document.getElementById('horarioInicio').value,
            horaFin:    document.getElementById('horarioFin').value,
            estado:     true
        };
        const url    = id ? `${API_BASE}/horarios/${id}` : `${API_BASE}/horarios`;
        const method = id ? 'PUT' : 'POST';

        const r = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!r.ok) { mostrarMensaje('No se pudo guardar el horario.'); return; }

        modales.horario.hide();
        mostrarMensaje(id ? 'Horario actualizado.' : 'Horario registrado.');
        cargarHorarios();
    });

    document.getElementById('tablaHorarios')?.addEventListener('click', async evento => {
        const boton = evento.target.closest('[data-accion]');
        if (!boton) return;
        const id = boton.dataset.id;

        if (boton.dataset.accion === 'editar-horario') {
            const r = await fetch(`${API_BASE}/horarios/${id}`);
            const h = await r.json();
            await cargarSelectMedicosHorario();

            document.getElementById('tituloModalHorario').textContent = 'Editar horario';
            document.getElementById('horarioId').value      = h.idHorario;
            document.getElementById('horarioMedico').value  = h.idMedico;
            document.getElementById('horarioDia').value     = h.diaSemana;
            document.getElementById('horarioInicio').value  = h.horaInicio;
            document.getElementById('horarioFin').value     = h.horaFin;
            modales.horario.show();
        }

        if (boton.dataset.accion === 'toggle-horario') {
            const activo = boton.dataset.activo === 'true';
            await fetch(`${API_BASE}/horarios/${id}/estado?activo=${!activo}`, { method: 'PATCH' });
            cargarHorarios();
        }
    });

    /* ================== DURACIÓN DE CITAS ================== */

async function cargarDuraciones() {
    const tbody = document.getElementById("tablaDuraciones");
    if (!tbody) return;

    try {
        const r = await fetch(`${API_BASE}/especialidades`);
        const lista = await r.json();

        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="4" class="tabla-vacia">No hay especialidades registradas.</td></tr>`;
            return;
        }

        tbody.innerHTML = lista.map(esp => `
            <tr>
                <td class="fw-semibold">${escaparHtml(esp.nombre)}</td>
                <td class="font-monospace">${esp.duracionCitaMin} min</td>
                <td>
                    <span class="etiqueta-estado ${esp.estado ? 'estado-atendida' : 'estado-cancelada'}">
                        ${esp.estado ? 'Activa' : 'Inactiva'}
                    </span>
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary"
                            data-accion="editar-duracion"
                            data-id="${esp.idEspecialidad}">
                        <i class="bi bi-pencil"></i>
                    </button>
                </td>
            </tr>`).join("");
    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="4" class="tabla-vacia">Error al cargar duraciones.</td></tr>`;
    }
}

document.getElementById("tablaDuraciones")?.addEventListener("click", async evento => {
    const boton = evento.target.closest('[data-accion="editar-duracion"]');
    if (!boton) return;

    const id = boton.dataset.id;
    const esp = await fetch(`${API_BASE}/especialidades/${id}`).then(r => r.json());

    document.getElementById("tituloModalDuracion").textContent   = `Duración · ${esp.nombre}`;
    document.getElementById("duracionIdEspecialidad").value      = esp.idEspecialidad;
    document.getElementById("duracionEspecialidad").value        = esp.nombre;
    document.getElementById("duracionMinutos").value             = esp.duracionCitaMin;

    modales.duracion.show();
});

document.getElementById("formDuracion")?.addEventListener("submit", async evento => {
    evento.preventDefault();

    const id = document.getElementById("duracionIdEspecialidad").value;
    const duracionCitaMin = Number(document.getElementById("duracionMinutos").value);

    if (!duracionCitaMin || duracionCitaMin <= 0) {
        mostrarMensaje("La duración debe ser mayor a 0.");
        return;
    }

    try {
        const r = await fetch(`${API_BASE}/especialidades/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ duracionCitaMin })
        });

        if (!r.ok) {
            mostrarMensaje("No se pudo guardar la duración.");
            return;
        }

        modales.duracion.hide();
        mostrarMensaje("Duración actualizada.");
        cargarDuraciones();

        // También refrescamos la caché local de especialidades si la usas en otros modales
        // (no es crítico, cada fetch trae lo último)
    } catch (e) {
        console.error(e);
        mostrarMensaje("No se pudo conectar con el servidor.");
    }
});
    /* ================== Arranque ================== */
    mostrarVista('inicio');
})();