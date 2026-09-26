(() => {
    const API_BASE = "http://localhost:8080/api";

    const TITULOS_VISTA = {
        inicio:    "Inicio",
        citas:     "Citas",
        pacientes: "Pacientes",
        perfil:    "Mi perfil"
    };

    const sesion = JSON.parse(localStorage.getItem("novasalud_usuario") || "null");
    if (!sesion || sesion.rolActivo !== "RECEPCIONISTA") return;

    /* ================== Caché de referencias ================== */
    const cache = { pacientes: [], medicos: [], especialidades: [], sedes: [] };

    async function cargarReferencias() {
        const [rPac, rMed, rEsp, rSede] = await Promise.all([
            fetch(`${API_BASE}/pacientes`).then(r => r.json()),
            fetch(`${API_BASE}/medicos`).then(r => r.json()),
            fetch(`${API_BASE}/especialidades`).then(r => r.json()),
            fetch(`${API_BASE}/sedes`).then(r => r.json())
        ]);
        cache.pacientes      = rPac;
        cache.medicos        = rMed;
        cache.especialidades = rEsp;
        cache.sedes          = rSede;
    }

    /* ================== Helpers ================== */
    function escapar(valor) {
        return String(valor ?? "").replace(/[&<>"']/g, c => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        })[c]);
    }
    function hoyISO() {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
    }
    function claseEstado(estado) {
        return ({
            "Pendiente":  "estado-pendiente",
            "Confirmada": "estado-confirmada",
            "Atendida":   "estado-atendida",
            "Cancelada":  "estado-cancelada",
            "No asistió": "estado-cancelada"
        })[estado] || "estado-pendiente";
    }
    function nombrePaciente(id) {
        const p = cache.pacientes.find(x => x.idUsuario === id);
        return p ? `${p.nombres} ${p.apellidos}` : `Paciente #${id}`;
    }
    function nombreMedico(id) {
        const m = cache.medicos.find(x => x.idUsuario === id);
        return m ? `${m.nombres} ${m.apellidos}` : `Médico #${id}`;
    }
    function nombreEspecialidad(id) {
        return cache.especialidades.find(e => e.idEspecialidad === id)?.nombre || `Especialidad #${id}`;
    }
    function nombreSede(id) {
        return cache.sedes.find(s => s.idSede === id)?.nombre || `Sede #${id}`;
    }

    /* ================== Navegación ================== */
    function mostrarVista(seccion) {
        document.querySelectorAll("[data-vista]").forEach(v =>
            v.classList.toggle("d-none", v.dataset.vista !== seccion));
        document.querySelectorAll("[data-seccion]").forEach(a =>
            a.classList.toggle("active", a.dataset.seccion === seccion));

        const titulo = document.getElementById("tituloVista");
        if (titulo) titulo.textContent = TITULOS_VISTA[seccion] || "Inicio";

        if (seccion === "inicio")    { cargarMetricasRecepcion(); cargarProximasCitas(); }
        if (seccion === "citas")     cargarTablaCitas();
        if (seccion === "pacientes") cargarPacientes();
        if (seccion === "perfil")    cargarPerfil();
    }

    document.querySelectorAll("[data-seccion]").forEach(enlace => {
        enlace.addEventListener("click", evento => {
            evento.preventDefault();
            mostrarVista(enlace.dataset.seccion);
        });
    });

    /* ================== Toast ================== */
    const toast = new bootstrap.Toast(document.getElementById("mensajeRecepcion"));
    function mostrarMensaje(texto) {
        document.getElementById("mensajeRecepcion").querySelector(".toast-body").textContent = texto;
        toast.show();
    }

    /* ================== Modales ================== */
    const modalPaciente = new bootstrap.Modal(document.getElementById("modalPaciente"));
    const modalCita     = new bootstrap.Modal(document.getElementById("modalCita"));

    /* ================== INICIO ================== */
    async function cargarMetricasRecepcion() {
        const cont = document.getElementById("metricasRecepcion");
        if (!cont) return;
        try {
            const hoy = hoyISO();
            const citas = await fetch(`${API_BASE}/citas/buscar?fecha=${hoy}`).then(r => r.json());
            const confirmadas = citas.filter(c => c.estado === "Confirmada").length;
            const pendientes  = citas.filter(c => c.estado === "Pendiente").length;

            cont.innerHTML = `
                <div class="col-12 col-sm-6 col-lg-3">
                    <div class="tarjeta-metrica tarjeta-azul">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-calendar-event fs-5 text-primary"></i>
                            <span class="metric-label">Citas de hoy</span>
                        </div>
                        <span class="metric-number">${citas.length}</span>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-lg-3">
                    <div class="tarjeta-metrica tarjeta-verde">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-person-check fs-5 text-success"></i>
                            <span class="metric-label">Confirmadas</span>
                        </div>
                        <span class="metric-number">${confirmadas}</span>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-lg-3">
                    <div class="tarjeta-metrica tarjeta-amarilla">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-clock fs-5 text-warning"></i>
                            <span class="metric-label">Pendientes</span>
                        </div>
                        <span class="metric-number">${pendientes}</span>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-lg-3">
                    <div class="tarjeta-metrica tarjeta-morada">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-people fs-5" style="color:#6f42c1;"></i>
                            <span class="metric-label">Pacientes</span>
                        </div>
                        <span class="metric-number">${cache.pacientes.length}</span>
                    </div>
                </div>`;
        } catch (e) { console.error(e); }
    }

    async function cargarProximasCitas() {
        const tbody = document.getElementById("tablaProximasCitas");
        if (!tbody) return;
        try {
            const hoy = hoyISO();
            const citas = await fetch(`${API_BASE}/citas/buscar?fecha=${hoy}`).then(r => r.json());
            citas.sort((a, b) => (a.horaCita || "").localeCompare(b.horaCita || ""));

            if (!citas.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">No hay citas registradas para hoy.</td></tr>';
                return;
            }
            tbody.innerHTML = citas.map(c => `
                <tr>
                    <td class="font-monospace">${escapar(c.horaCita)}</td>
                    <td class="paciente-nombre">${escapar(nombrePaciente(c.idPaciente))}</td>
                    <td>${escapar(nombreEspecialidad(c.idEspecialidad))}</td>
                    <td>${escapar(nombreMedico(c.idMedico))}</td>
                    <td><span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></td>
                </tr>`).join("");
        } catch (e) { console.error(e); }
    }

    /* ================== CITAS ================== */
    function llenarFiltrosCitas() {
        const selSede = document.getElementById("filtroSede");
        const selEsp  = document.getElementById("filtroEspecialidad");
        const selMed  = document.getElementById("filtroMedico");
        if (!selSede) return;

        selSede.innerHTML = '<option value="">Todas</option>' +
            cache.sedes.map(s => `<option value="${s.idSede}">${escapar(s.nombre)}</option>`).join("");
        selEsp.innerHTML  = '<option value="">Todas</option>' +
            cache.especialidades.map(e => `<option value="${e.idEspecialidad}">${escapar(e.nombre)}</option>`).join("");
        selMed.innerHTML  = '<option value="">Todos</option>' +
            cache.medicos.map(m => `<option value="${m.idUsuario}">${escapar(m.nombres)} ${escapar(m.apellidos)}</option>`).join("");
    }

    async function cargarTablaCitas() {
        const tbody = document.getElementById("tablaCitas");
        if (!tbody) return;

        const qs = new URLSearchParams();
        const fecha    = document.getElementById("filtroFecha").value;
        const idSede   = document.getElementById("filtroSede").value;
        const idEsp    = document.getElementById("filtroEspecialidad").value;
        const idMedico = document.getElementById("filtroMedico").value;
        const estado   = document.getElementById("filtroEstado").value;

        if (fecha)    qs.set("fecha", fecha);
        if (idSede)   qs.set("idSede", idSede);
        if (idEsp)    qs.set("idEspecialidad", idEsp);
        if (idMedico) qs.set("idMedico", idMedico);
        if (estado)   qs.set("estado", estado);

        try {
            const citas = await fetch(`${API_BASE}/citas/buscar?${qs.toString()}`).then(r => r.json());
            citas.sort((a, b) => (b.fechaCita + b.horaCita).localeCompare(a.fechaCita + a.horaCita));

            if (!citas.length) {
                tbody.innerHTML = '<tr><td colspan="8" class="tabla-vacia">No hay citas con esos filtros.</td></tr>';
                return;
            }

            tbody.innerHTML = citas.map(c => {
                const activa = c.estado === "Pendiente" || c.estado === "Confirmada";
                return `
                    <tr>
                        <td>${escapar(c.fechaCita)}</td>
                        <td class="font-monospace">${escapar(c.horaCita)}</td>
                        <td class="paciente-nombre">${escapar(nombrePaciente(c.idPaciente))}</td>
                        <td>${escapar(nombreEspecialidad(c.idEspecialidad))}</td>
                        <td>${escapar(nombreMedico(c.idMedico))}</td>
                        <td>${escapar(nombreSede(c.idSede))}</td>
                        <td><span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></td>
                        <td class="text-end">
                            <div class="acciones-fila">
                                <button class="btn btn-sm btn-outline-primary" data-accion="editar-cita" data-id="${c.idCita}">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-secondary" data-accion="detalle-cita" data-id="${c.idCita}">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" data-accion="cancelar-cita" data-id="${c.idCita}" ${activa ? "" : "disabled"}>
                                    <i class="bi bi-x-circle"></i>
                                </button>
                            </div>
                        </td>
                    </tr>`;
            }).join("");
        } catch (e) {
            console.error(e);
            tbody.innerHTML = '<tr><td colspan="8" class="tabla-vacia">Error al cargar citas.</td></tr>';
        }
    }

    ['filtroFecha','filtroSede','filtroEspecialidad','filtroMedico','filtroEstado'].forEach(id => {
        document.getElementById(id)?.addEventListener("change", cargarTablaCitas);
    });

    document.getElementById("btnLimpiarFiltros")?.addEventListener("click", () => {
        ['filtroFecha','filtroSede','filtroEspecialidad','filtroMedico','filtroEstado'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = "";
        });
        cargarTablaCitas();
    });

    /* ---------- Modal de cita ---------- */
    function llenarSelectsModalCita() {
        const selPac  = document.getElementById("citaPaciente");
        const selEsp  = document.getElementById("citaEspecialidad");
        const selSede = document.getElementById("citaSede");

        selPac.innerHTML = cache.pacientes.map(p =>
            `<option value="${p.idUsuario}">${escapar(p.nombres)} ${escapar(p.apellidos)}</option>`).join("");
        selEsp.innerHTML = cache.especialidades.map(e =>
            `<option value="${e.idEspecialidad}">${escapar(e.nombre)}</option>`).join("");
        selSede.innerHTML = cache.sedes.map(s =>
            `<option value="${s.idSede}">${escapar(s.nombre)}</option>`).join("");

        actualizarMedicosModal();
    }

    function actualizarMedicosModal() {
        const selEsp  = document.getElementById("citaEspecialidad");
        const selSede = document.getElementById("citaSede");
        const selMed  = document.getElementById("citaMedico");
        if (!selEsp || !selSede || !selMed) return;

        const idEsp  = Number(selEsp.value);
        const idSede = Number(selSede.value);
        const medicos = cache.medicos.filter(m =>
            m.activo && m.idEspecialidad === idEsp && m.idSede === idSede);

        selMed.innerHTML = medicos.length
            ? medicos.map(m => `<option value="${m.idUsuario}">${escapar(m.nombres)} ${escapar(m.apellidos)}</option>`).join("")
            : '<option value="">Sin médicos disponibles</option>';

        cargarHorariosModalCita();
    }

    async function cargarHorariosModalCita() {
        const idMedico = document.getElementById("citaMedico").value;
        const fecha    = document.getElementById("citaFecha").value;
        const selHor   = document.getElementById("citaHorario");
        if (!selHor) return;

        if (!idMedico || !fecha) {
            selHor.innerHTML = '<option value="">Selecciona médico y fecha primero</option>';
            return;
        }

        selHor.innerHTML = '<option value="">Cargando...</option>';
        try {
            const slots = await fetch(
                `${API_BASE}/citas/disponibles?idMedico=${idMedico}&fecha=${fecha}`
            ).then(r => r.json());

            selHor.innerHTML = slots.length
                ? slots.map(h => `<option value="${h}">${h}</option>`).join("")
                : '<option value="">No hay horarios disponibles ese día</option>';
        } catch (e) {
            console.error(e);
            selHor.innerHTML = '<option value="">Error al consultar horarios</option>';
        }
    }

    document.getElementById("citaEspecialidad")?.addEventListener("change", actualizarMedicosModal);
    document.getElementById("citaSede")?.addEventListener("change", actualizarMedicosModal);
    document.getElementById("citaMedico")?.addEventListener("change", cargarHorariosModalCita);
    document.getElementById("citaFecha")?.addEventListener("change", cargarHorariosModalCita);

    document.querySelectorAll(".btn-nueva-cita").forEach(boton =>
        boton.addEventListener("click", () => {
            document.getElementById("formCita").reset();
            document.getElementById("citaId").value = "";
            document.getElementById("tituloModalCita").textContent = "Nueva cita";
            document.getElementById("citaFecha").value = hoyISO();
            document.getElementById("citaFecha").min   = hoyISO();
            llenarSelectsModalCita();
        })
    );

    document.getElementById("formCita").addEventListener("submit", async evento => {
        evento.preventDefault();
        const id = document.getElementById("citaId").value;

        const payload = {
            idPaciente:     Number(document.getElementById("citaPaciente").value),
            idEspecialidad: Number(document.getElementById("citaEspecialidad").value),
            idSede:         Number(document.getElementById("citaSede").value),
            idMedico:       Number(document.getElementById("citaMedico").value),
            fechaCita:      document.getElementById("citaFecha").value,
            horaCita:       document.getElementById("citaHorario").value,
            motivoCita:     document.getElementById("citaMotivo").value.trim()
        };

        if (!payload.idMedico || !payload.horaCita) {
            mostrarMensaje("Selecciona un médico y un horario.");
            return;
        }

        const url    = id ? `${API_BASE}/citas/${id}` : `${API_BASE}/citas`;
        const method = id ? "PUT" : "POST";

        try {
            const r = await fetch(url, {
                method,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await r.json().catch(() => ({}));
            if (!r.ok) {
                mostrarMensaje(data.mensaje || "No se pudo guardar la cita.");
                return;
            }
            modalCita.hide();
            mostrarMensaje(id ? "Cita actualizada." : "Cita registrada.");
            cargarTablaCitas();
        } catch (e) {
            console.error(e);
            mostrarMensaje("No se pudo conectar con el servidor.");
        }
    });

    document.getElementById("tablaCitas").addEventListener("click", async evento => {
        const boton = evento.target.closest("[data-accion]");
        if (!boton) return;
        const id = boton.dataset.id;

        if (boton.dataset.accion === "editar-cita") {
            const c = await fetch(`${API_BASE}/citas/${id}`).then(r => r.json());
            document.getElementById("tituloModalCita").textContent = "Editar cita";
            document.getElementById("citaId").value = c.idCita;

            llenarSelectsModalCita();
            document.getElementById("citaPaciente").value     = c.idPaciente;
            document.getElementById("citaEspecialidad").value = c.idEspecialidad;
            document.getElementById("citaSede").value         = c.idSede;
            actualizarMedicosModal();
            document.getElementById("citaMedico").value = c.idMedico;
            document.getElementById("citaFecha").value  = c.fechaCita;

            await cargarHorariosModalCita();

            const selHor = document.getElementById("citaHorario");
            if (![...selHor.options].some(o => o.value === c.horaCita)) {
                selHor.insertAdjacentHTML("afterbegin",
                    `<option value="${c.horaCita}">${c.horaCita} (actual)</option>`);
            }
            selHor.value = c.horaCita;

            document.getElementById("citaMotivo").value = c.motivoCita || "";
            modalCita.show();
        }

        if (boton.dataset.accion === "detalle-cita") {
            const c = await fetch(`${API_BASE}/citas/${id}`).then(r => r.json());
            const detalle = document.getElementById("detalleCitaBody");
            if (!detalle) return;
            detalle.innerHTML = `
                <p><strong>Paciente:</strong> ${escapar(nombrePaciente(c.idPaciente))}</p>
                <p><strong>Especialidad:</strong> ${escapar(nombreEspecialidad(c.idEspecialidad))}</p>
                <p><strong>Médico:</strong> ${escapar(nombreMedico(c.idMedico))}</p>
                <p><strong>Sede:</strong> ${escapar(nombreSede(c.idSede))}</p>
                <p><strong>Fecha:</strong> ${escapar(c.fechaCita)} - ${escapar(c.horaCita)}</p>
                <p><strong>Motivo:</strong> ${escapar(c.motivoCita || "-")}</p>
                <p><strong>Estado:</strong> <span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></p>
            `;
            new bootstrap.Modal(document.getElementById("modalDetalleCita")).show();
        }

        if (boton.dataset.accion === "cancelar-cita") {
            if (!confirm("¿Cancelar esta cita?")) return;
            try {
                await fetch(`${API_BASE}/citas/${id}/estado?valor=Cancelada`, { method: "PATCH" });
                mostrarMensaje("Cita cancelada.");
                cargarTablaCitas();
            } catch (e) { console.error(e); }
        }
    });

    /* ================== PACIENTES ================== */
    const tablaPacientes = document.getElementById("tablaUsuarios");

    async function cargarPacientes() {
        if (!tablaPacientes) return;
        try {
            const r = await fetch(`${API_BASE}/pacientes`);
            if (!r.ok) throw new Error("Error al listar pacientes");
            const pacientes = await r.json();
            pintarPacientes(pacientes);
        } catch (error) {
            console.error(error);
            tablaPacientes.innerHTML = `
                <tr><td colspan="7" class="tabla-vacia">
                    No se pudieron cargar los pacientes.
                </td></tr>`;
        }
    }

    async function buscarPacientes(texto) {
        if (!tablaPacientes) return;
        const t = texto.trim();
        if (!t) return cargarPacientes();

        try {
            const r = await fetch(`${API_BASE}/pacientes/buscar?texto=${encodeURIComponent(t)}`);
            if (!r.ok) throw new Error("Error en la búsqueda");
            const pacientes = await r.json();
            pintarPacientes(pacientes);
        } catch (error) {
            console.error(error);
            mostrarMensaje("No se pudo realizar la búsqueda.");
        }
    }

    function pintarPacientes(pacientes) {
        if (!tablaPacientes) return;

        if (!pacientes.length) {
            tablaPacientes.innerHTML = `
                <tr><td colspan="7" class="tabla-vacia">
                    No se encontraron pacientes.
                </td></tr>`;
            return;
        }

        tablaPacientes.innerHTML = pacientes.map(p => `
            <tr>
                <td class="font-monospace">${escapar(p.idUsuario)}</td>
                <td>
                    <div class="paciente-nombre">${escapar(p.nombres)} ${escapar(p.apellidos)}</div>
                    <div class="paciente-documento">DNI ${escapar(p.dni)}</div>
                </td>
                <td>${escapar(p.dni)}</td>
                <td>${escapar(p.telefono)}</td>
                <td>${escapar(p.correo)}</td>
                <td>
                    <span class="etiqueta-estado ${p.activo ? "estado-atendida" : "estado-cancelada"}">
                        ${p.activo ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary"
                            data-accion="editar-paciente"
                            data-id="${escapar(p.idUsuario)}">
                        <i class="bi bi-pencil"></i>
                    </button>
                </td>
            </tr>
        `).join("");
    }

    const inputBusquedaFiltro = document.getElementById("filtroBusquedaPaciente");
    const inputBusqueda       = document.getElementById("buscarUsuario");
    const botonBuscar         = document.getElementById("btnBuscar");

    if (inputBusquedaFiltro) {
        inputBusquedaFiltro.addEventListener("input", () => buscarPacientes(inputBusquedaFiltro.value));
    }
    if (botonBuscar) {
        botonBuscar.addEventListener("click", () => buscarPacientes(inputBusqueda.value));
    }
    if (inputBusqueda) {
        inputBusqueda.addEventListener("keyup", evento => {
            if (evento.key === "Enter") buscarPacientes(inputBusqueda.value);
        });
    }

    if (tablaPacientes) {
        tablaPacientes.addEventListener("click", evento => {
            const boton = evento.target.closest("[data-accion]");
            if (!boton) return;
            if (boton.dataset.accion === "editar-paciente") {
                abrirEditarPaciente(boton.dataset.id);
            }
        });
    }

    async function abrirEditarPaciente(id) {
        try {
            const r = await fetch(`${API_BASE}/pacientes/${id}`);
            if (!r.ok) throw new Error("Paciente no encontrado");
            const p = await r.json();

            document.getElementById("tituloModalPaciente").textContent = "Editar paciente";
            document.getElementById("pacienteId").value               = p.idUsuario;
            document.getElementById("pacienteDni").value              = p.dni ?? "";
            document.getElementById("pacienteNombres").value          = p.nombres ?? "";
            document.getElementById("pacienteApellidos").value        = p.apellidos ?? "";
            document.getElementById("pacienteTelefono").value         = p.telefono ?? "";
            document.getElementById("pacienteEmail").value            = p.correo ?? "";
            document.getElementById("pacienteFechaNacimiento").value  = p.fechaNac ?? "";

            modalPaciente.show();
        } catch (error) {
            console.error(error);
            mostrarMensaje("No se pudo cargar el paciente.");
        }
    }

    document.querySelectorAll(".btn-nuevo-paciente").forEach(boton =>
        boton.addEventListener("click", () => {
            document.getElementById("formPaciente").reset();
            document.getElementById("pacienteId").value = "";
            document.getElementById("tituloModalPaciente").textContent = "Nuevo paciente";
        })
    );

    document.getElementById("formPaciente").addEventListener("submit", async evento => {
        evento.preventDefault();

        const id = document.getElementById("pacienteId").value;

        const payload = {
            nombres:   document.getElementById("pacienteNombres").value.trim(),
            apellidos: document.getElementById("pacienteApellidos").value.trim(),
            dni:       document.getElementById("pacienteDni").value.trim(),
            telefono:  document.getElementById("pacienteTelefono").value.trim(),
            correo:    document.getElementById("pacienteEmail").value.trim(),
            fechaNac:  document.getElementById("pacienteFechaNacimiento").value || null,
            activo:    true
        };

        const url    = id ? `${API_BASE}/pacientes/${id}` : `${API_BASE}/pacientes`;
        const method = id ? "PUT" : "POST";

        try {
            const r = await fetch(url, {
                method,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!r.ok) throw new Error("Error al guardar paciente");

            modalPaciente.hide();
            mostrarMensaje(id ? "Paciente actualizado." : "Paciente registrado.");
            await cargarReferencias();
            cargarPacientes();

        } catch (error) {
            console.error(error);
            mostrarMensaje("No se pudo guardar el paciente.");
        }
    });

    /* ================== PERFIL ================== */
    async function cargarPerfil() {
        try {
            const u = await fetch(`${API_BASE}/usuarios/${sesion.idUsuario}`).then(r => r.json());
            document.getElementById("perfilNombres").value   = u.nombres || "";
            document.getElementById("perfilApellidos").value = u.apellidos || "";
            document.getElementById("perfilCorreo").value    = u.correo || "";
            document.getElementById("perfilTelefono").value  = u.telefono || "";
        } catch (e) { console.error(e); }
    }

    document.getElementById("formPerfil").addEventListener("submit", async evento => {
        evento.preventDefault();
        const payload = {
            nombres:   document.getElementById("perfilNombres").value.trim(),
            apellidos: document.getElementById("perfilApellidos").value.trim(),
            correo:    document.getElementById("perfilCorreo").value.trim(),
            telefono:  document.getElementById("perfilTelefono").value.trim()
        };
        try {
            const r = await fetch(`${API_BASE}/usuarios/${sesion.idUsuario}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!r.ok) { mostrarMensaje("No se pudo guardar."); return; }
            document.getElementById("perfilMensaje").textContent = "Cambios guardados.";
        } catch (e) { console.error(e); }
    });

    /* ================== Arranque ================== */
    (async () => {
        await cargarReferencias();
        llenarFiltrosCitas();
        mostrarVista("inicio");
    })();
})();