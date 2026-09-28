/* auth.js — sesión, protección de ruta y pintado del sidebar */

const CLAVE_SESION = 'novasalud_usuario';

const ROL_A_SECCION = {
    'ADMINISTRADOR': 'admin',
    'RECEPCIONISTA': 'recepcion',
    'MEDICO':        'medico',
    'PACIENTE':      'paciente'
};

function obtenerSesion() {
    try {
        return JSON.parse(localStorage.getItem(CLAVE_SESION) || 'null');
    } catch {
        return null;
    }
}

function guardarSesion(usuario) {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario));
}

function cerrarSesion() {
    localStorage.removeItem(CLAVE_SESION);
    window.location.href = '../index.html';
}

/* Devuelve la sesión si el rol coincide con <body data-required-role="…"> */
function protegerRuta(seccionRequerida) {
    const sesion = obtenerSesion();
    if (!sesion) {
        window.location.href = '../index.html';
        return null;
    }
    const rol = sesion.rolActivo;
    const seccionUsuario = ROL_A_SECCION[rol];
    if (!seccionUsuario || seccionUsuario !== seccionRequerida) {
        window.location.href = '../index.html';
        return null;
    }
    return sesion;
}

/* Rellena los data-user-* del sidebar y del header */
function pintarSesion(sesion) {
    if (!sesion) return;
    const iniciales = (sesion.nombres?.[0] || '') + (sesion.apellidos?.[0] || '');
    document.querySelectorAll('[data-user-name]').forEach(el => el.textContent = `${sesion.nombres} ${sesion.apellidos}`);
    document.querySelectorAll('[data-user-email]').forEach(el => el.textContent = sesion.correo);
    document.querySelectorAll('[data-user-initials]').forEach(el => el.textContent = iniciales.toUpperCase() || 'US');
    document.querySelectorAll('[data-user-role]').forEach(el => el.textContent = sesion.rolActivo);
}

/* Pinta y protege automáticamente al cargar */
document.addEventListener('DOMContentLoaded', () => {
    const requerido = document.body.dataset.requiredRole;
    if (!requerido) return;
    const sesion = protegerRuta(requerido);
    if (sesion) pintarSesion(sesion);
});

window.cerrarSesion = cerrarSesion;