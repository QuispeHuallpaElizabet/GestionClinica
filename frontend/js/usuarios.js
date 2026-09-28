async function cargarUsuarios() {

    const request = await fetch('http://localhost:8080/usuarios');

    const usuarios = await request.json();

    console.log(usuarios);

    let contenido = '';

    usuarios.forEach(usuario => {
        contenido += `
            <tr>
                <td>${usuario.id}</td>
                <td>${usuario.nombre}</td>
                <td>${usuario.apellido}</td>
                <td>${usuario.email}</td>
                <td>${usuario.telefono}</td>
                <td> <button class="btn btn-sm btn-outline-primary me-3">Ver Detalles</button>
                    <button class="btn btn-danger btn-circle btn-sm"><i class="bi bi-trash"></i></button></td>
            </tr>
        `;
    });

    document.querySelector('#tableBody').innerHTML = contenido;
}

cargarUsuarios();

