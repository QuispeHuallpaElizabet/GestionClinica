(() => {
    /* ================== Configuración ================== */
    const API_BASE = "http://localhost:8080/api";
    const TITULOS_VISTA = {
        agenda:     "Mi agenda",
        atenciones: "Atenciones",
        perfil:     "Mi perfil"
    };

    const sesion = JSON.parse(localStorage.getItem("novasalud_usuario") || "null");
    if (!sesion || sesion.rolActivo !== "MEDICO") return;
    const idMedico = sesion.idUsuario;

    /* ================== Caché ================== */
    const cache = { especialidades: [], sedes: [], pacientes: [] };

    async function cargarReferencias() {
        const [rEsp, rSede, rPac] = await Promise.all([
            fetch(`${API_BASE}/especialidades`).then(r => r.json()),
            fetch(`${API_BASE}/sedes`).then(r => r.json()),
            fetch(`${API_BASE}/pacientes`).then(r => r.json())
        ]);
        cache.especialidades = rEsp;
        cache.sedes          = rSede;
        cache.pacientes      = rPac;
    }

    function nombrePaciente(id) {
        const p = cache.pacientes.find(x => x.idUsuario === id);
        return p ? `${p.nombres} ${p.apellidos}` : `Paciente #${id}`;
    }
    function nombreEspecialidad(id) {
        return cache.especialidades.find(e => e.idEspecialidad === id)?.nombre || `Especialidad #${id}`;
    }
    function escapar(v) {
        return String(v ?? "").replace(/[&<>"']/g, c => ({
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

    /* ================== Navegación ================== */
    function mostrarVista(seccion) {
        document.querySelectorAll("[data-vista]").forEach(v =>
            v.classList.toggle("d-none", v.dataset.vista !== seccion));
        document.querySelectorAll("[data-seccion]").forEach(a =>
            a.classList.toggle("active", a.dataset.seccion === seccion));
        document.getElementById("tituloVista").textContent = TITULOS_VISTA[seccion] || "Mi agenda";

        if (seccion === "agenda")     cargarAgenda();
        if (seccion === "atenciones") cargarAtenciones();
        if (seccion === "perfil")     cargarPerfil();
    }
    document.querySelectorAll("[data-seccion]").forEach(enlace => {
        enlace.addEventListener("click", e => {
            e.preventDefault();
            mostrarVista(enlace.dataset.seccion);
        });
    });

    /* ================== Toast ================== */
    const toast = new bootstrap.Toast(document.getElementById("mensajeMedico"));
    function mostrarMensaje(t) {
        document.getElementById("mensajeMedico").querySelector(".toast-body").textContent = t;
        toast.show();
    }

    /* ================== Modal atención ================== */
    const modalAtencion = new bootstrap.Modal(document.getElementById("modalAtencion"));

    function abrirAtencion(cita) {
        document.getElementById("atencionCitaId").value  = cita.idCita;
        document.getElementById("resumenPaciente").textContent =
            `${nombrePaciente(cita.idPaciente)} · ${cita.fechaCita} · ${cita.horaCita}`;
        document.getElementById("notaAtencion").value = cita.notaAtencion || "";
        modalAtencion.show();
    }

    /* ================== AGENDA ================== */
    let filtroEstado = "Todas";

    document.querySelectorAll(".filtro-estado").forEach(boton => {
        boton.addEventListener("click", () => {
            filtroEstado = boton.dataset.estado;
            document.querySelectorAll(".filtro-estado").forEach(b => {
                const activo = b === boton;
                b.classList.toggle("btn-primary", activo);
                b.classList.toggle("btn-outline-primary", !activo);
                b.setAttribute("aria-pressed", String(activo));
            });
            cargarAgenda();
        });
    });

    document.getElementById("filtroFecha").addEventListener("change", cargarAgenda);

    async function cargarAgenda() {
        const tbody    = document.getElementById("tablaAgenda");
        const fecha    = document.getElementById("filtroFecha").value;
        const metricas = document.getElementById("metricasMedico");
        if (!tbody) return;

        try {
            const url = `${API_BASE}/citas/medico/${idMedico}?fecha=${fecha || ""}`;
            const citas = await fetch(url).then(r => r.json());

            const pendientes = citas.filter(c => c.estado === "Pendiente" || c.estado === "Confirmada").length;
            const atendidas  = citas.filter(c => c.estado === "Atendida").length;

            metricas.innerHTML = `
                <div class="col-12 col-sm-6 col-lg-4">
                    <div class="tarjeta-metrica tarjeta-azul">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-calendar-event fs-5 text-primary"></i>
                            <span class="metric-label">Citas del día</span>
                        </div>
                        <span class="metric-number">${citas.length}</span>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-lg-4">
                    <div class="tarjeta-metrica tarjeta-amarilla">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-hourglass-split fs-5 text-warning"></i>
                            <span class="metric-label">Pendientes</span>
                        </div>
                        <span class="metric-number">${pendientes}</span>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-lg-4">
                    <div class="tarjeta-metrica tarjeta-verde">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <i class="bi bi-check-circle fs-5 text-success"></i>
                            <span class="metric-label">Atendidas</span>
                        </div>
                        <span class="metric-number">${atendidas}</span>
                    </div>
                </div>`;

            let visibles = citas;
            if (filtroEstado === "Pendiente") {
                visibles = citas.filter(c => c.estado === "Pendiente" || c.estado === "Confirmada");
            } else if (filtroEstado !== "Todas") {
                visibles = citas.filter(c => c.estado === filtroEstado);
            }
            visibles.sort((a, b) => (a.horaCita || "").localeCompare(b.horaCita || ""));

            if (!visibles.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">No hay citas para esta fecha y filtro.</td></tr>';
                return;
            }

            tbody.innerHTML = visibles.map(c => {
                const nota = c.notaAtencion
                    ? '<span class="badge text-bg-light border ms-2">Nota</span>'
                    : "";
                const activa = c.estado === "Pendiente" || c.estado === "Confirmada";
                const acciones = activa
                    ? `<button class="btn btn-sm btn-primary" data-accion="atender" data-id="${c.idCita}">
                           <i class="bi bi-clipboard2-check me-1"></i>Registrar
                       </button>
                       <button class="btn btn-sm btn-outline-secondary" data-accion="no-asistio" data-id="${c.idCita}">
                           No asistió
                       </button>`
                    : `<button class="btn btn-sm btn-outline-primary" data-accion="atender" data-id="${c.idCita}">
                           <i class="bi bi-eye me-1"></i>Ver / editar nota
                       </button>`;

                return `
                    <tr>
                        <td class="font-monospace">${escapar(c.horaCita)}</td>
                        <td>
                            <div class="paciente-nombre">${escapar(nombrePaciente(c.idPaciente))}</div>
                            <div class="paciente-documento">${escapar(nombreEspecialidad(c.idEspecialidad))}</div>
                        </td>
                        <td>${escapar(c.motivoCita || "-")}${nota}</td>
                        <td><span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></td>
                        <td class="text-end"><div class="acciones-fila">${acciones}</div></td>
                    </tr>`;
            }).join("");

        } catch (e) {
            console.error(e);
            tbody.innerHTML = '<tr><td colspan="5" class="tabla-vacia">Error al cargar la agenda.</td></tr>';
        }
    }

    document.getElementById("tablaAgenda").addEventListener("click", async evento => {
        const boton = evento.target.closest("[data-accion]");
        if (!boton) return;
        const id = boton.dataset.id;

        if (boton.dataset.accion === "atender") {
            const c = await fetch(`${API_BASE}/citas/${id}`).then(r => r.json());
            abrirAtencion(c);
        }

        if (boton.dataset.accion === "no-asistio") {
            const c = await fetch(`${API_BASE}/citas/${id}`).then(r => r.json());
            if (!confirm(`¿Registrar que ${nombrePaciente(c.idPaciente)} no asistió?`)) return;
            try {
                await fetch(
                    `${API_BASE}/citas/${id}/estado?valor=${encodeURIComponent("No asistió")}`,
                    { method: "PATCH" }
                );
                mostrarMensaje("Estado actualizado.");
                cargarAgenda();
            } catch (e) { console.error(e); }
        }
    });

    document.getElementById("formAtencion").addEventListener("submit", async evento => {
        evento.preventDefault();
        const nota = document.getElementById("notaAtencion").value.trim();
        if (!nota) return;

        const id = document.getElementById("atencionCitaId").value;
        try {
            const r = await fetch(
                `${API_BASE}/citas/${id}/estado?valor=Atendida&nota=${encodeURIComponent(nota)}`,
                { method: "PATCH" }
            );
            if (!r.ok) { mostrarMensaje("No se pudo guardar."); return; }
            modalAtencion.hide();
            mostrarMensaje("Atención guardada.");
            cargarAgenda();
        } catch (e) {
            console.error(e);
            mostrarMensaje("No se pudo conectar con el servidor.");
        }
    });

    /* ================== ATENCIONES ================== */
    document.getElementById("filtroHistorial").addEventListener("input", cargarAtenciones);

    async function cargarAtenciones() {
        const tbody = document.getElementById("tablaAtenciones");
        if (!tbody) return;

        const texto = document.getElementById("filtroHistorial").value.trim().toLowerCase();

        try {
            const citas = await fetch(`${API_BASE}/citas/medico/${idMedico}`).then(r => r.json());
            const historial = citas
                .filter(c => c.estado === "Atendida" || c.estado === "No asistió")
                .filter(c => {
                    if (!texto) return true;
                    const n = nombrePaciente(c.idPaciente).toLowerCase();
                    return n.includes(texto)
                        || (c.estado || "").toLowerCase().includes(texto)
                        || (c.motivoCita || "").toLowerCase().includes(texto);
                })
                .sort((a, b) => (b.fechaCita + b.horaCita).localeCompare(a.fechaCita + a.horaCita));

            if (!historial.length) {
                tbody.innerHTML = '<tr><td colspan="6" class="tabla-vacia">No hay atenciones que coincidan.</td></tr>';
                return;
            }

            tbody.innerHTML = historial.map(c => {
                const detalle = c.estado === "Atendida"
                    ? `<button class="btn btn-sm btn-outline-primary" data-accion="ver-nota" data-id="${c.idCita}">
                           ${c.notaAtencion ? "Ver / editar nota" : "Agregar nota"}
                       </button>`
                    : '<span class="text-secondary small">Sin nota clínica</span>';

                return `
                    <tr>
                        <td>${escapar(c.fechaCita)}</td>
                        <td class="font-monospace">${escapar(c.horaCita)}</td>
                        <td class="paciente-nombre">${escapar(nombrePaciente(c.idPaciente))}</td>
                        <td>${escapar(c.motivoCita || "-")}</td>
                        <td><span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></td>
                        <td class="text-end">${detalle}</td>
                    </tr>`;
            }).join("");
        } catch (e) {
            console.error(e);
            tbody.innerHTML = '<tr><td colspan="6" class="tabla-vacia">Error al cargar atenciones.</td></tr>';
        }
    }

    document.getElementById("tablaAtenciones").addEventListener("click", async evento => {
        const boton = evento.target.closest('[data-accion="ver-nota"]');
        if (!boton) return;
        const c = await fetch(`${API_BASE}/citas/${boton.dataset.id}`).then(r => r.json());
        abrirAtencion(c);
    });

    /* ================== PERFIL ================== */
    async function cargarPerfil() {
        try {
            const u = await fetch(`${API_BASE}/usuarios/${idMedico}`).then(r => r.json());
            const m = await fetch(`${API_BASE}/medicos/${idMedico}`).then(r => r.json());

            document.getElementById("perfilNombre").value       = `${u.nombres} ${u.apellidos}`;
            document.getElementById("perfilCorreo").value       = u.correo || "";
            document.getElementById("perfilEspecialidad").value = nombreEspecialidad(m.idEspecialidad);
            document.getElementById("perfilSede").value         =
                cache.sedes.find(s => s.idSede === m.idSede)?.nombre || "";
            document.getElementById("perfilTelefono").value     = u.telefono || "";
        } catch (e) { console.error(e); }
    }

    document.getElementById("formPerfil").addEventListener("submit", async evento => {
        evento.preventDefault();
        const telefono = document.getElementById("perfilTelefono").value.trim();
        try {
            const r = await fetch(`${API_BASE}/usuarios/${idMedico}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ telefono })
            });
            if (!r.ok) { mostrarMensaje("No se pudo guardar."); return; }
            document.getElementById("perfilMensaje").textContent = "Cambios guardados.";
        } catch (e) { console.error(e); }
    });

    /* ================== Arranque ================== */
    (async () => {
        document.getElementById("filtroFecha").value = hoyISO();
        await cargarReferencias();
        mostrarVista("agenda");
    })();
})();