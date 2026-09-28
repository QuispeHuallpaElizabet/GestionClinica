(() => {
    /* ================== Configuración ================== */
    const API_URL      = "http://localhost:8080/api/auth/cambiar-contrasena";
    const CLAVE_TEMP   = "novasalud_cambio_temp";
    const CLAVE_SESION = "novasalud_usuario";

    const RUTAS_PANEL = {
        "ADMINISTRADOR": "admin.html",
        "RECEPCIONISTA": "recepcion.html",
        "MEDICO":        "medico.html",
        "PACIENTE":      "paciente.html"
    };

    /* ================== Elementos ================== */
    const form           = document.getElementById("formCambiarClave");
    const nuevaClave     = document.getElementById("nuevaClave");
    const confirmarClave = document.getElementById("confirmarClave");
    const mensaje        = document.getElementById("mensajeClave");
    const boton          = document.getElementById("btnCambiar");
    const aviso          = document.getElementById("avisoUsuario");

    /* ================== Recuperar sesión temporal ================== */
    let temp = null;
    try {
        temp = JSON.parse(localStorage.getItem(CLAVE_TEMP) || "null");
    } catch (_) {
        temp = null;
    }

    if (!temp || !temp.correo) {
        window.location.href = "../index.html";
        return;
    }

    aviso.classList.remove("d-none");
    aviso.textContent = `Hola ${temp.nombres || ""}, debes cambiar tu contraseña antes de continuar.`;

    /* ================== Envío ================== */
    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        mensaje.textContent = "";

        if (nuevaClave.value.length < 6) {
            mensaje.textContent = "La contraseña debe tener al menos 6 caracteres.";
            return;
        }
        if (nuevaClave.value !== confirmarClave.value) {
            mensaje.textContent = "Las contraseñas no coinciden.";
            return;
        }

        boton.disabled = true;
        boton.textContent = "Guardando...";

        try {
            const r = await fetch(API_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    correo:     temp.correo,
                    contrasena: nuevaClave.value
                })
            });
            const data = await r.json();

            if (!r.ok) {
                mensaje.textContent = data.mensaje || "No se pudo actualizar la contraseña.";
                return;
            }

            const rolActivo = data.roles?.[0]?.nombre;
            const destino   = RUTAS_PANEL[rolActivo];

            if (!destino) {
                mensaje.textContent = "Tu cuenta no tiene un rol válido.";
                return;
            }

            const sesion = {
                idUsuario: data.idUsuario,
                nombres:   data.nombres,
                apellidos: data.apellidos,
                correo:    data.correo,
                roles:     data.roles,
                rolActivo: rolActivo
            };

            localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
            localStorage.removeItem(CLAVE_TEMP);

            window.location.href = destino;

        } catch (error) {
            console.error(error);
            mensaje.textContent = "No se pudo conectar con el servidor.";
        } finally {
            boton.disabled = false;
            boton.textContent = "Cambiar contraseña";
        }
    });
})();