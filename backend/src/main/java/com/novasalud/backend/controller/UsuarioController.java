package com.novasalud.backend.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novasalud.backend.models.Usuario;

@RestController // Esta anotación indica que esta clase es un controlador REST, lo que significa que manejará las solicitudes HTTP y devolverá respuestas en formato JSON o XML
@CrossOrigin(origins = "http://127.0.0.1:5500")

public class UsuarioController {

    @RequestMapping("/usuario/{id}") // Esta anotación indica que este método manejará las solicitudes HTTP GET a la ruta "/usuario/{id}", donde {id} es un parámetro de ruta que representa el ID del usuario
    public Usuario getUsuario(@PathVariable Long id) { // El parámetro @PathVariable Long id indica que el valor del parámetro de ruta {id} se asignará a la variable id del método 
        Usuario usuario = new Usuario();
        usuario.setId(id); // Se establece el ID del usuario con el valor proporcionado en la solicitud HTTP
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juanperez@gmail.com");
        usuario.setTelefono("123456789");
        usuario.setContraseña("123456");
        return usuario; // Devuelve un objeto Usuario como respuesta a la solicitud HTTP
    }

    @RequestMapping("/usuarios") 
    public List<Usuario> getUsuarios() { 

        List<Usuario> usuarios = new ArrayList<>();

        Usuario usuario = new Usuario();
        usuario.setId(8956L); 
        usuario.setNombre("Maria");
        usuario.setApellido("Pérez");
        usuario.setEmail("juanperez@gmail.com");
        usuario.setTelefono("123456789");
        usuario.setContraseña("123456");

        Usuario usuario2 = new Usuario();
        usuario2.setId(16413L); 
        usuario2.setNombre("Jose");
        usuario2.setApellido("Pérez");
        usuario2.setEmail("juanperez@gmail.com");
        usuario2.setTelefono("123456789");
        usuario2.setContraseña("123456");


        Usuario usuario3 = new Usuario();
        usuario3.setId(4545L); 
        usuario3.setNombre("Juan");
        usuario3.setApellido("Pérez");
        usuario3.setEmail("juanperez@gmail.com");
        usuario3.setTelefono("123456789");
        usuario3.setContraseña("123456");

        usuarios.add(usuario);
        usuarios.add(usuario2);
        usuarios.add(usuario3);

        return usuarios; // Devuelve un objeto Usuario como respuesta a la solicitud HTTP
    }

}   
