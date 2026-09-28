(() => {
    /* ================== Configuración ================== */
    const API_BASE = "http://localhost:8080/api";
    const TITULOS_VISTA = {
        inicio:   "Inicio",
        misCitas: "Mis citas",
        perfil:   "Mi perfil"
    };

    const sesion = JSON.parse(localStorage.getItem("novasalud_usuario") || "null");
    if (!sesion || sesion.rolActivo !== "PACIENTE") return;
    const idPaciente = sesion.idUsuario;

    /* ================== Caché de referencias ================== */
    const cache = { especialidades: [], sedes: [], medicos: [], misCitas: [] };

    async function cargarReferencias() {
        const [rEsp, rSede, rMed] = await Promise.all([
            fetch(`${API_BASE}/especialidades`).then(r => r.json()),
            fetch(`${API_BASE}/sedes`).then(r => r.json()),
            fetch(`${API_BASE}/medicos`).then(r => r.json())
        ]);
        cache.especialidades = rEsp;
        cache.sedes          = rSede;
        cache.medicos        = rMed;
    }

    function nombreEspecialidad(id) {
        return cache.especialidades.find(e => e.idEspecialidad === id)?.nombre || `Especialidad #${id}`;
    }
    function nombreSede(id) {
        return cache.sedes.find(s => s.idSede === id)?.nombre || `Sede #${id}`;
    }
    function nombreMedico(id) {
        const m = cache.medicos.find(x => x.idUsuario === id);
        return m ? `${m.nombres} ${m.apellidos}` : `Médico #${id}`;
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
        document.querySelectorAll(".nav-link[data-seccion]").forEach(a =>
            a.classList.toggle("active", a.dataset.seccion === seccion));
        document.getElementById("tituloVista").textContent = TITULOS_VISTA[seccion] || "Inicio";

        if (seccion === "inicio")   cargarInicio();
        if (seccion === "misCitas") cargarMisCitas();
        if (seccion === "perfil")   cargarPerfil();
    }
    document.querySelectorAll("[data-seccion]").forEach(enlace => {
        enlace.addEventListener("click", e => {
            e.preventDefault();
            mostrarVista(enlace.dataset.seccion);
        });
    });

    /* ================== Toast ================== */
    const toast = new bootstrap.Toast(document.getElementById("mensajePaciente"));
    function mostrarMensaje(texto) {
        document.getElementById("mensajePaciente").querySelector(".toast-body").textContent = texto;
        toast.show();
    }

    /* ================== Modales ================== */
    const modalDetalle     = new bootstrap.Modal(document.getElementById("modalDetalleCita"));
    const modalReprogramar = new bootstrap.Modal(document.getElementById("modalReprogramar"));

    /* ================== INICIO ================== */
    async function cargarInicio() {
        try {
            const citas = await fetch(`${API_BASE}/citas/paciente/${idPaciente}`).then(r => r.json());
            const hoy = hoyISO();

            const proximas   = citas.filter(c => c.fechaCita >= hoy && c.estado !== "Cancelada");
            const pendientes = citas.filter(c => c.estado === "Pendiente" || c.estado === "Confirmada");
            const atendidas  = citas.filter(c => c.estado === "Atendida");

            document.getElementById("metricaProximas").textContent   = proximas.length;
            document.getElementById("metricaPendientes").textContent = pendientes.length;
            document.getElementById("metricaAtendidas").textContent  = atendidas.length;

            const prox = proximas
                .slice()
                .sort((a, b) => (a.fechaCita + a.horaCita).localeCompare(b.fechaCita + b.horaCita))[0];

            const cont = document.getElementById("proximaCitaDetalle");
            if (!prox) {
                cont.innerHTML = '<i class="bi bi-calendar-x me-2"></i>No tienes próximas citas.';
                return;
            }
            cont.innerHTML = `
                <p class="mb-1"><strong>${escapar(nombreEspecialidad(prox.idEspecialidad))}</strong> con ${escapar(nombreMedico(prox.idMedico))}</p>
                <p class="mb-1"><i class="bi bi-geo-alt me-1"></i>${escapar(nombreSede(prox.idSede))}</p>
                <p class="mb-1"><i class="bi bi-calendar3 me-1"></i>${escapar(prox.fechaCita)} a las ${escapar(prox.horaCita)}</p>
                <span class="etiqueta-estado ${claseEstado(prox.estado)}">${escapar(prox.estado)}</span>
            `;
        } catch (e) {
            console.error(e);
            mostrarMensaje("No se pudo cargar el inicio.");
        }
    }

    /* ================== MIS CITAS ================== */
    async function cargarMisCitas() {
        const tbody = document.getElementById("tablaMisCitas");
        if (!tbody) return;

        try {
            const citas = await fetch(`${API_BASE}/citas/paciente/${idPaciente}`).then(r => r.json());
            cache.misCitas = citas;

            if (!citas.length) {
                tbody.innerHTML = '<tr><td colspan="7" class="tabla-vacia">Aún no tienes citas.</td></tr>';
                return;
            }

            citas.sort((a, b) => (b.fechaCita + b.horaCita).localeCompare(a.fechaCita + a.horaCita));

            tbody.innerHTML = citas.map(c => {
                const activa = c.estado === "Pendiente" || c.estado === "Confirmada";
                return `
                    <tr>
                        <td>${escapar(c.fechaCita)}</td>
                        <td class="font-monospace">${escapar(c.horaCita)}</td>
                        <td>${escapar(nombreEspecialidad(c.idEspecialidad))}</td>
                        <td>${escapar(nombreMedico(c.idMedico))}</td>
                        <td>${escapar(nombreSede(c.idSede))}</td>
                        <td><span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></td>
                        <td class="text-end">
                            <div class="acciones-fila">
                                <button class="btn btn-sm btn-outline-primary" data-accion="detalle" data-id="${c.idCita}">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-secondary" data-accion="reprogramar" data-id="${c.idCita}" ${activa ? "" : "disabled"}>
                                    <i class="bi bi-calendar-week"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" data-accion="cancelar" data-id="${c.idCita}" ${activa ? "" : "disabled"}>
                                    <i class="bi bi-x-circle"></i>
                                </button>
                            </div>
                        </td>
                    </tr>`;
            }).join("");
        } catch (e) {
            console.error(e);
            tbody.innerHTML = '<tr><td colspan="7" class="tabla-vacia">Error al cargar tus citas.</td></tr>';
        }
    }

    document.getElementById("tablaMisCitas").addEventListener("click", async evento => {
        const boton = evento.target.closest("[data-accion]");
        if (!boton) return;
        const id = boton.dataset.id;

        if (boton.dataset.accion === "detalle") {
            const c = await fetch(`${API_BASE}/citas/${id}`).then(r => r.json());
            document.getElementById("detalleCitaCuerpo").innerHTML = `
                <p><strong>Especialidad:</strong> ${escapar(nombreEspecialidad(c.idEspecialidad))}</p>
                <p><strong>Médico:</strong> ${escapar(nombreMedico(c.idMedico))}</p>
                <p><strong>Sede:</strong> ${escapar(nombreSede(c.idSede))}</p>
                <p><strong>Fecha:</strong> ${escapar(c.fechaCita)} - ${escapar(c.horaCita)}</p>
                <p><strong>Motivo:</strong> ${escapar(c.motivoCita || "-")}</p>
                <p><strong>Estado:</strong> <span class="etiqueta-estado ${claseEstado(c.estado)}">${escapar(c.estado)}</span></p>
            `;
            modalDetalle.show();
        }

        if (boton.dataset.accion === "reprogramar") {
            const cita = cache.misCitas.find(c => String(c.idCita) === String(id));
            if (!cita) return;

            const inputFecha = document.getElementById("reprogramarFecha");
            const selHorario = document.getElementById("reprogramarHorario");

            document.getElementById("reprogramarId").value = cita.idCita;
            inputFecha.min = hoyISO();
            inputFecha.value = cita.fechaCita;

            // Al cambiar la fecha → recargar slots (manteniendo la referencia al slot original)
            inputFecha.onchange = () =>
                cargarSlotsReprogramar(cita.idMedico, inputFecha.value, cita.horaCita, cita.fechaCita);

            // Carga inicial: slots + slot actual inyectado
            await cargarSlotsReprogramar(
                cita.idMedico,
                cita.fechaCita,
                cita.horaCita,
                cita.fechaCita
            );
            selHorario.value = cita.horaCita;

            modalReprogramar.show();
        }

        if (boton.dataset.accion === "cancelar") {
            if (!confirm("¿Seguro que deseas cancelar esta cita?")) return;
            await fetch(`${API_BASE}/citas/${id}/estado?valor=Cancelada`, { method: "PATCH" });
            mostrarMensaje("Cita cancelada.");
            cargarMisCitas();
        }
    });

    /* Carga los slots disponibles del médico para una fecha dada. */
async function cargarSlotsReprogramar(idMedico, fecha, horaActual, fechaOriginal) {
        const selHorario = document.getElementById("reprogramarHorario");
        if (!selHorario) return;

        if (!idMedico || !fecha) {
            selHorario.innerHTML = '<option value="">Selecciona una fecha primero</option>';
            return;
        }

        selHorario.innerHTML = '<option value="">Cargando...</option>';
        try {
            const slots = await fetch(
                `${API_BASE}/citas/disponibles?idMedico=${idMedico}&fecha=${fecha}`
            ).then(r => r.json());

            // Solo inyectamos el slot actual si seguimos en la fecha original
            const inyectarActual = horaActual
                && fecha === fechaOriginal
                && !slots.includes(horaActual);

            const opciones = inyectarActual
                ? [{ valor: horaActual, etiqueta: `${horaActual} (actual)` },
                ...slots.map(h => ({ valor: h, etiqueta: h }))]
                : slots.map(h => ({ valor: h, etiqueta: h }));

            selHorario.innerHTML = opciones.length
                ? opciones.map(o => `<option value="${o.valor}">${o.etiqueta}</option>`).join("")
                : '<option value="">No hay horarios disponibles ese día</option>';
        } catch (e) {
            console.error(e);
            selHorario.innerHTML = '<option value="">Error al consultar horarios</option>';
        }
    }

    document.getElementById("formReprogramar").addEventListener("submit", async evento => {
        evento.preventDefault();
        const id    = document.getElementById("reprogramarId").value;
        const fecha = document.getElementById("reprogramarFecha").value;
        const hora  = document.getElementById("reprogramarHorario").value;

        if (!hora) { mostrarMensaje("Selecciona un horario disponible."); return; }

        try {
            const r = await fetch(`${API_BASE}/citas/${id}/reprogramar`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ fechaCita: fecha, horaCita: hora })
            });
            const data = await r.json().catch(() => ({}));
            if (!r.ok) {
                mostrarMensaje(data.mensaje || "No se pudo reprogramar.");
                return;
            }
            modalReprogramar.hide();
            mostrarMensaje("Cita reprogramada.");
            cargarMisCitas();
        } catch (e) {
            console.error(e);
            mostrarMensaje("No se pudo conectar con el servidor.");
        }
    });

    /* ================== PERFIL ================== */
    async function cargarPerfil() {
        try {
            const u = await fetch(`${API_BASE}/usuarios/${idPaciente}`).then(r => r.json());
            document.getElementById("perfilDni").value       = u.dni || "";
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
            const r = await fetch(`${API_BASE}/usuarios/${idPaciente}`, {
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
        mostrarVista("inicio");
    })();
})();