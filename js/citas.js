
// ---- Clave para localStorage ----
const STORAGE_KEY = 'novasalud_data';

const DEFAULT_DATA = {
    pacientes: [
        { id: 1, dni: '12345678', nombres: 'Ana', apellidos: 'García', fechaNac: '1990-05-12', telefono: '987654321', correo: 'ana@mail.com', direccion: 'Av. Lima 123' },
        { id: 2, dni: '87654321', nombres: 'Carlos', apellidos: 'Ruiz', fechaNac: '1985-08-22', telefono: '912345678', correo: 'carlos@mail.com', direccion: 'Calle 2 #456' }
    ],
    citas: []
    };

    // ---- Cargar datos desde localStorage o inicializar ----
    function cargarDatos() {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
        try {
        return JSON.parse(stored);
        } catch (e) {
        console.error('Error al parsear datos, usando default', e);
        return JSON.parse(JSON.stringify(DEFAULT_DATA));
        }
    }
    // Si no hay datos, guardar los default y devolverlos
    guardarDatos(DEFAULT_DATA);
    return JSON.parse(JSON.stringify(DEFAULT_DATA));
    }

    // ---- Guardar datos en localStorage ----
    function guardarDatos(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    }

    // ---- Estado global ----
    let data = cargarDatos();
    let pacientes = data.pacientes;
    let citas = data.citas;

    // ---- Referencias DOM ----
    const metricsContainer = document.getElementById('metricsContainer');
    const tableBody = document.getElementById('tableBody');
    const listaCitas = document.getElementById('listaCitas');
    const citasCount = document.getElementById('citasCount');

    // ---- Funciones de actualización de UI ----

    // 1. Métricas
    function actualizarMetricas() {
    const hoy = new Date().toISOString().slice(0, 10);
    const citasHoy = citas.filter(c => c.fecha === hoy);
    const pendientes = citasHoy.filter(c => c.estado === 'Pendiente').length;
    const atendidas = citasHoy.filter(c => c.estado === 'Atendida').length;
    const canceladas = citasHoy.filter(c => c.estado === 'Cancelada').length;
    const total = citasHoy.length;

    const metricas = [
        { icon: 'bi-calendar-check', label: 'Citas de hoy', value: total },
        { icon: 'bi-clock-history',  label: 'Pendientes',   value: pendientes },
        { icon: 'bi-check2-circle',  label: 'Atendidas',    value: atendidas },
        { icon: 'bi-x-circle',       label: 'Canceladas',   value: canceladas }
    ];

    metricsContainer.innerHTML = metricas.map(m => `
        <div class="col-md-3 col-sm-6">
        <div class="card-metric">
            <div class="d-flex align-items-center gap-2 mb-2">
            <i class="bi ${m.icon} fs-5 text-secondary"></i>
            <span class="metric-label">${m.label}</span>
            </div>
            <span class="metric-number">${m.value}</span>
        </div>
        </div>
    `).join('');
    }

    // 2. Tabla de citas de hoy
    function actualizarTabla() {
    const hoy = new Date().toISOString().slice(0, 10);
    const citasHoy = citas.filter(c => c.fecha === hoy);
    citasHoy.sort((a, b) => a.horario.localeCompare(b.horario));

    if (citasHoy.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No hay citas programadas para hoy.</td></tr>`;
        return;
    }

    const estadoClase = {
        'Pendiente': 'pendiente',
        'Atendida': 'atendida',
        'Cancelada': 'cancelada'
    };

    tableBody.innerHTML = citasHoy.map(c => `
        <tr>
        <td class="font-monospace">${c.horario}</td>
        <td>${c.paciente}</td>
        <td>${c.especialidad}</td>
        <td>${c.medico}</td>
        <td><span class="estado-texto ${estadoClase[c.estado] || 'pendiente'}">${c.estado}</span></td>
        <td class="text-end">
            <div class="dropdown">
            <a href="#" class="action-icon" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-three-dots-vertical"></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-end">
                <li><a class="dropdown-item" href="#" data-accion="ver" data-id="${c.id}"><i class="bi bi-eye"></i> Ver detalle</a></li>
                <li><a class="dropdown-item" href="#" data-accion="reprogramar" data-id="${c.id}"><i class="bi bi-pencil"></i> Reprogramar</a></li>
                <li><a class="dropdown-item" href="#" data-accion="cancelar" data-id="${c.id}"><i class="bi bi-x-circle"></i> Cancelar</a></li>
            </ul>
            </div>
        </td>
        </tr>
    `).join('');

    // Asignar eventos a las acciones
    document.querySelectorAll('[data-accion]').forEach(el => {
        el.addEventListener('click', function(e) {
        e.preventDefault();
        const id = parseInt(this.dataset.id);
        const accion = this.dataset.accion;
        if (accion === 'ver') abrirDetalle(id);
        else if (accion === 'reprogramar') abrirReprogramar(id);
        else if (accion === 'cancelar') abrirCancelar(id);
        });
    });
    }

    // 3. Lista de citas recientes
    function actualizarListados() {
    const citasRecientes = citas.slice(-5).reverse();
    listaCitas.innerHTML = citasRecientes.map(c =>
        `<li class="list-group-item d-flex justify-content-between align-items-center">
        ${c.paciente} · ${c.especialidad} · ${c.fecha} ${c.horario}
        <span class="badge bg-primary">${c.medico}</span>
        </li>`
    ).join('') || '<li class="list-group-item text-muted">No hay citas registradas</li>';

    citasCount.textContent = citas.length;
    }

    // 4. Actualizar todo
    function actualizarDashboard() {
    // Guardar siempre los datos actualizados en localStorage
    guardarDatos({ pacientes, citas });

    actualizarMetricas();
    actualizarTabla();
    actualizarListados();
    llenarSelectPacientes();
    }

    // ---- Llenar select de pacientes ----
    function llenarSelectPacientes() {
    const select = document.getElementById('citaPaciente');
    if (!select) return;
    select.innerHTML = '<option value="">Buscar por DNI o Nombre...</option>';
    pacientes.forEach(p => {
        const option = document.createElement('option');
        option.value = p.id;
        option.textContent = `${p.dni} - ${p.nombres} ${p.apellidos}`;
        select.appendChild(option);
    });
    }

    // ---- ACCIONES DE LA TABLA ----
    let citaEnEdicion = null;

    // Ver detalle
    function abrirDetalle(id) {
    const cita = citas.find(c => c.id === id);
    if (!cita) return;
    const paciente = pacientes.find(p => p.id === cita.pacienteId);
    const body = document.getElementById('detalleCitaBody');
    body.innerHTML = `
        <div class="detalle-item"><span class="label">Paciente:</span><span class="valor">${cita.paciente}</span></div>
        <div class="detalle-item"><span class="label">DNI:</span><span class="valor">${paciente ? paciente.dni : 'N/A'}</span></div>
        <div class="detalle-item"><span class="label">Especialidad:</span><span class="valor">${cita.especialidad}</span></div>
        <div class="detalle-item"><span class="label">Médico:</span><span class="valor">${cita.medico}</span></div>
        <div class="detalle-item"><span class="label">Sede:</span><span class="valor">${cita.sede}</span></div>
        <div class="detalle-item"><span class="label">Fecha:</span><span class="valor">${cita.fecha}</span></div>
        <div class="detalle-item"><span class="label">Hora:</span><span class="valor">${cita.horario}</span></div>
        <div class="detalle-item"><span class="label">Estado:</span><span class="valor">${cita.estado}</span></div>
        <div class="detalle-item" style="border-bottom:none;"><span class="label">Motivo:</span><span class="valor">${cita.motivo || 'No especificado'}</span></div>
    `;
    const modal = new bootstrap.Modal(document.getElementById('modalDetalleCita'));
    modal.show();
    }

    // Reprogramar
    function abrirReprogramar(id) {
    const cita = citas.find(c => c.id === id);
    if (!cita) return;
    citaEnEdicion = id;

    document.getElementById('reprogramarPaciente').value = cita.paciente;
    document.getElementById('reprogramarSede').value = cita.sede;
    document.getElementById('reprogramarEspecialidad').value = cita.especialidad;
    document.getElementById('reprogramarMedico').value = cita.medico;
    document.getElementById('reprogramarFecha').value = cita.fecha;
    document.getElementById('reprogramarMotivo').value = cita.motivo || '';

    generarHorarios(cita.horario);
    
    const modal = new bootstrap.Modal(document.getElementById('modalReprogramar'));
    modal.show();
    }

    function generarHorarios(horarioSeleccionado) {
    const container = document.getElementById('horariosDisponibles');
    const horarios = ['08:00','08:30','09:00','09:30','10:00','10:30','11:00','11:30','12:00'];
    container.innerHTML = horarios.map(h => `
        <button type="button" class="btn-horario ${h === horarioSeleccionado ? 'seleccionado' : ''}" data-horario="${h}">${h}</button>
    `).join('');

    document.getElementById('reprogramarHorario').value = horarioSeleccionado;

    container.querySelectorAll('.btn-horario').forEach(btn => {
        btn.addEventListener('click', function() {
        container.querySelectorAll('.btn-horario').forEach(b => b.classList.remove('seleccionado'));
        this.classList.add('seleccionado');
        document.getElementById('reprogramarHorario').value = this.dataset.horario;
        });
    });
    }

    // Guardar reprogramación
    document.getElementById('btnGuardarReprogramacion').addEventListener('click', function() {
    const id = citaEnEdicion;
    const cita = citas.find(c => c.id === id);
    if (!cita) return;

    cita.sede = document.getElementById('reprogramarSede').value;
    cita.especialidad = document.getElementById('reprogramarEspecialidad').value;
    cita.medico = document.getElementById('reprogramarMedico').value;
    cita.fecha = document.getElementById('reprogramarFecha').value;
    cita.horario = document.getElementById('reprogramarHorario').value;
    cita.motivo = document.getElementById('reprogramarMotivo').value;

    actualizarDashboard();
    const modal = bootstrap.Modal.getInstance(document.getElementById('modalReprogramar'));
    modal.hide();
    mostrarExito('¡Cita reprogramada exitosamente!');
    });

    // Cancelar cita
    function abrirCancelar(id) {
    const cita = citas.find(c => c.id === id);
    if (!cita) return;
    const body = document.getElementById('cancelarCitaBody');
    body.innerHTML = `
        <p class="text-danger fw-bold">¿Cancelar esta cita?</p>
        <p>La cita de <strong>${cita.paciente}</strong> con el <strong>${cita.medico}</strong> el <strong>${cita.fecha}</strong> a las <strong>${cita.horario}</strong> será marcada como cancelada.</p>
    `;
    document.getElementById('btnConfirmarCancelacion').dataset.id = id;
    const modal = new bootstrap.Modal(document.getElementById('modalCancelarCita'));
    modal.show();
    }

    document.getElementById('btnConfirmarCancelacion').addEventListener('click', function() {
    const id = parseInt(this.dataset.id);
    const cita = citas.find(c => c.id === id);
    if (cita) {
        cita.estado = 'Cancelada';
        actualizarDashboard();
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalCancelarCita'));
        modal.hide();
        mostrarExito('Cita cancelada correctamente.');
    }
    });

    // ---- MODAL DE ÉXITO ----
    function mostrarExito(mensaje) {
    document.getElementById('exitoMensaje').textContent = mensaje;
    const modal = new bootstrap.Modal(document.getElementById('modalExito'));
    modal.show();
    }

    // ---- GUARDAR PACIENTE ----
    document.getElementById('btnGuardarPaciente').addEventListener('click', function() {
    const dni = document.getElementById('pacienteDni').value.trim();
    const nombres = document.getElementById('pacienteNombres').value.trim();
    const apellidos = document.getElementById('pacienteApellidos').value.trim();
    const fechaNac = document.getElementById('pacienteFechaNac').value;
    const telefono = document.getElementById('pacienteTelefono').value.trim();
    const correo = document.getElementById('pacienteCorreo').value.trim();
    const direccion = document.getElementById('pacienteDireccion').value.trim();

    if (!dni || !nombres || !apellidos || !fechaNac) {
        alert('Por favor completa los campos obligatorios (DNI, Nombres, Apellidos, Fecha de Nacimiento).');
        return;
    }

    const nuevoPaciente = {
        id: Date.now(),
        dni,
        nombres,
        apellidos,
        fechaNac,
        telefono,
        correo,
        direccion
    };

    pacientes.push(nuevoPaciente);
    actualizarDashboard();

    const modal = bootstrap.Modal.getInstance(document.getElementById('modalPaciente'));
    modal.hide();
    document.getElementById('formPaciente').reset();
    mostrarExito('Paciente registrado exitosamente.');
    });

    // ---- GUARDAR CITA ----
    document.getElementById('btnGuardarCita').addEventListener('click', function() {
    const pacienteId = document.getElementById('citaPaciente').value;
    const especialidad = document.getElementById('citaEspecialidad').value;
    const sede = document.getElementById('citaSede').value;
    const medico = document.getElementById('citaMedico').value;
    const fecha = document.getElementById('citaFecha').value;
    const horario = document.getElementById('citaHorario').value;
    const motivo = document.getElementById('citaMotivo').value.trim();

    if (!pacienteId || !especialidad || !sede || !medico || !fecha || !horario) {
        alert('Por favor completa todos los campos obligatorios.');
        return;
    }

    const pacienteObj = pacientes.find(p => p.id == pacienteId);
    const pacienteNombre = pacienteObj ? `${pacienteObj.nombres} ${pacienteObj.apellidos}` : 'Paciente';

    const nuevaCita = {
        id: Date.now(),
        pacienteId,
        paciente: pacienteNombre,
        especialidad,
        sede,
        medico,
        fecha,
        horario,
        motivo,
        estado: 'Pendiente'
    };

    citas.push(nuevaCita);
    actualizarDashboard();

    const modal = bootstrap.Modal.getInstance(document.getElementById('modalCita'));
    modal.hide();
    document.getElementById('formCita').reset();
    mostrarExito('Cita registrada exitosamente.');
    });

    // ---- INICIALIZACIÓN ----
    document.addEventListener('DOMContentLoaded', function() {
    // Si no hay citas, agregar algunas de ejemplo (solo si no hay citas)
    if (citas.length === 0) {
        const hoy = new Date().toISOString().slice(0, 10);
        citas.push(
        { id: 1, pacienteId: 1, paciente: 'Ana García', especialidad: 'Cardiología', sede: 'Sede Central', medico: 'Dr. López', fecha: hoy, horario: '09:00', motivo: 'Chequeo anual', estado: 'Pendiente' },
        { id: 2, pacienteId: 2, paciente: 'Carlos Ruiz', especialidad: 'Dermatología', sede: 'Sede Norte', medico: 'Dra. Silva', fecha: hoy, horario: '10:30', motivo: 'Consulta por acné', estado: 'Atendida' },
        { id: 3, pacienteId: 1, paciente: 'Ana García', especialidad: 'Pediatría', sede: 'Sede Central', medico: 'Dr. Torres', fecha: hoy, horario: '11:00', motivo: 'Control niño', estado: 'Cancelada' }
        );
        guardarDatos({ pacientes, citas });
    }

    actualizarDashboard();

    // Limpiar formularios al cerrar modales
    document.getElementById('modalPaciente').addEventListener('hidden.bs.modal', function() {
        document.getElementById('formPaciente').reset();
    });
    document.getElementById('modalCita').addEventListener('hidden.bs.modal', function() {
        document.getElementById('formCita').reset();
    });
});