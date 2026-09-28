/* ============================================================
    login.js — Autenticación contra /api/auth/login
    Guarda la sesión normalizada en localStorage.novasalud_usuario
    para que auth.js pueda proteger las rutas de cada panel.
   ============================================================ */

const API_URL = "http://localhost:8080/api/auth/login";

const formLogin    = document.getElementById("formLogin");
const correo       = document.getElementById("correo");
const clave        = document.getElementById("clave");
const mensajeLogin = document.getElementById("mensajeLogin");
const btnIngresar  = document.getElementById("btnIngresar");

/* Mapeo rol (backend) → archivo del panel */
const RUTAS_PANEL = {
    "ADMINISTRADOR": "html/admin.html",
    "RECEPCIONISTA": "html/recepcion.html",
    "MEDICO":        "html/medico.html",
    "PACIENTE":      "html/paciente.html"
};

formLogin.addEventListener("submit", async function (event) {

    event.preventDefault();
    mensajeLogin.textContent = "";
    btnIngresar.disabled = true;
    btnIngresar.textContent = "Ingresando...";

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                correo:    correo.value.trim(),
                contrasena: clave.value
            })
        });

        const resultado = await response.json();

        if (!response.ok) {
            mensajeLogin.textContent = resultado.mensaje || "Error al iniciar sesión.";
            return;
        }

        /* Cambio de contraseña obligatorio: no dejamos entrar al panel */
        if (resultado.requiereCambioContrasena) {
            localStorage.setItem("novasalud_cambio_temp", JSON.stringify(resultado));
            window.location.href = "html/cambiar-contrasena.html";
            return;
        }

        /* Determinamos el rol activo (primer rol del usuario) */
        const rolActivo = resultado.roles?.[0]?.nombre;

        if (!rolActivo || !RUTAS_PANEL[rolActivo]) {
            mensajeLogin.textContent = "El usuario no tiene un rol válido.";
            return;
        }

        /* Sesión normalizada que auth.js espera */
        const sesion = {
            idUsuario:  resultado.idUsuario,
            nombres:    resultado.nombres,
            apellidos:  resultado.apellidos,
            correo:     resultado.correo,
            roles:      resultado.roles,
            rolActivo:  rolActivo
        };

        localStorage.setItem("novasalud_usuario", JSON.stringify(sesion));

        window.location.href = RUTAS_PANEL[rolActivo];

    } catch (error) {
        console.error("Error en login:", error);
        mensajeLogin.textContent = "No se pudo conectar con el servidor.";
    } finally {
        btnIngresar.disabled = false;
        btnIngresar.textContent = "Iniciar Sesión";
    }
});
