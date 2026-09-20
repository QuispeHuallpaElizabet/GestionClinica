const pacientes = [
    { id: 1, dni: '12345678', nombres: 'Ana', apellidos: 'García' },
    { id: 2, dni: '87654321', nombres: 'Carlos', apellidos: 'Ruiz' },
    { id: 3, dni: '45678912', nombres: 'María', apellidos: 'Paz' },
    { id: 4, dni: '11223344', nombres: 'Luis', apellidos: 'Torres' },
    { id: 5, dni: '55667788', nombres: 'Elena', apellidos: 'Vega' }
    ];

    const STORAGE_KEY = 'novasalud_citas';

    // ---- Estado de citas ----
    let citas = [];

    // ---- Cargar citas desde localStorage ----
    function cargarCitas() {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
        try {
        citas = JSON.parse(stored);
        } catch (e) {
        console.error('Error al parsear citas', e);
        citas = [];
        }
    } else {
        // Datos de ejemplo para mostrar el dashboard
        const hoy = new Date().toISOString().slice(0, 10);
        citas = [
        { id: 1, pacienteId: 1, paciente: 'Ana García', especialidad: 'Cardiología', sede: 'Sede Central', medico: 'Dr. López', fecha: hoy, horario: '09:00', motivo: 'Chequeo anual', estado: 'Pendiente' },
        { id: 2, pacienteId: 2, paciente: 'Carlos Ruiz', especialidad: 'Dermatología', sede: 'Sede Norte', medico: 'Dra. Silva', fecha: hoy, horario: '10:30', motivo: 'Consulta por acné', estado: 'Atendida' },
        { id: 3, pacienteId: 1, paciente: 'Ana García', especialidad: 'Pediatría', sede: 'Sede Central', medico: 'Dr. Torres', fecha: hoy, horario: '11:00', motivo: 'Control niño', estado: 'Cancelada' }
        ];
        guardarCitas();
    }
    }

    // ---- Guardar citas en localStorage ----
    function guardarCitas() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(citas));
    }


    const metricsContainer = document.getElementById('metricsContainer');
    const tableBody = document.getElementById('tableBody');
    const listaCitas = document.getElementById('listaCitas');
    const citasCount = document.getElementById('citasCount');
    const selectPaciente = document.getElementById('citaPaciente');

    // ---- Llenar select de pacientes ----
    function llenarSelectPacientes() {
    selectPaciente.innerHTML = '<option value="">Seleccione un paciente</option>';
    pacientes.forEach(p => {
        const option = document.createElement('option');
        option.value = p.id;
        option.textContent = `${p.dni} - ${p.nombres} ${p.apellidos}`;
        selectPaciente.appendChild(option);
    });
    }

    // ---- Actualizar métricas ----
    function actualizarMetricas() {
    const hoy = new Date().toISOString().slice(0, 10);
    const citasHoy = citas.filter(c => c.fecha === hoy);
    const total = citasHoy.length;
    const pendientes = citasHoy.filter(c => c.estado === 'Pendiente').length;
    const atendidas = citasHoy.filter(c => c.estado === 'Atendida').length;
    const canceladas = citasHoy.filter(c => c.estado === 'Cancelada').length;

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

    // ---- Actualizar tabla de citas de hoy ----
    function actualizarTabla() {
    const hoy = new Date().toISOString().slice(0, 10);
    const citasHoy = citas.filter(c => c.fecha === hoy);
    citasHoy.sort((a, b) => a.horario.localeCompare(b.horario));

    if (citasHoy.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No hay citas para hoy.</td></tr>`;
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
            <!-- Botón de acciones (solo visual, sin funcionalidad por ahora) -->
            <button class="btn btn-sm btn-outline-secondary" disabled><i class="bi bi-three-dots-vertical"></i></button>
        </td>
        </tr>
    `).join('');
    }

    // ---- Actualizar lista de citas recientes ----
    function actualizarListados() {
    const citasRecientes = citas.slice(-5).reverse();
    listaCitas.innerHTML = citasRecientes.length
        ? citasRecientes.map(c => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
            ${c.paciente} · ${c.especialidad} · ${c.fecha} ${c.horario}
            <span class="badge bg-primary">${c.medico}</span>
            </li>
        `).join('')
        : '<li class="list-group-item text-muted">No hay citas registradas</li>';

    citasCount.textContent = citas.length;
    }

    // ---- Actualizar todo el dashboard ----
    function actualizarDashboard() {
    guardarCitas();       
    actualizarMetricas();
    actualizarTabla();
    actualizarListados();
    }

    // ---- Evento: Guardar nueva cita ----
    document.getElementById('btnGuardarCita').addEventListener('click', function() {
    // Obtener valores
    const pacienteId = parseInt(document.getElementById('citaPaciente').value);
    const especialidad = document.getElementById('citaEspecialidad').value;
    const sede = document.getElementById('citaSede').value;
    const medico = document.getElementById('citaMedico').value;
    const fecha = document.getElementById('citaFecha').value;
    const horario = document.getElementById('citaHorario').value;
    const motivo = document.getElementById('citaMotivo').value.trim();

    // Validar campos obligatorios
    if (!pacienteId || !especialidad || !sede || !medico || !fecha || !horario) {
        alert('Por favor completa todos los campos obligatorios.');
        return;
    }

    // Buscar paciente seleccionado
    const pacienteObj = pacientes.find(p => p.id === pacienteId);
    if (!pacienteObj) {
        alert('Paciente no válido.');
        return;
    }

    // Crear nueva cita
    const nuevaCita = {
        id: Date.now(),
        pacienteId,
        paciente: `${pacienteObj.nombres} ${pacienteObj.apellidos}`,
        especialidad,
        sede,
        medico,
        fecha,
        horario,
        motivo,
        estado: 'Pendiente'  
    };

    // Agregar al array y guardar
    citas.push(nuevaCita);
    actualizarDashboard();

    // Cerrar modal y resetear formulario
    const modal = bootstrap.Modal.getInstance(document.getElementById('modalCita'));
    modal.hide();
    document.getElementById('formCita').reset();

    // Mostrar mensaje de éxito
    alert('Cita registrada exitosamente.');
    });

    // ---- Al cerrar el modal, resetear formulario ----
    document.getElementById('modalCita').addEventListener('hidden.bs.modal', function() {
    document.getElementById('formCita').reset();
    });

    // ---- Inicialización ----
    document.addEventListener('DOMContentLoaded', function() {
    cargarCitas();                 
    llenarSelectPacientes();       
    actualizarDashboard();         
    });