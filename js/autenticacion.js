
const correo='recepcionista@gmail.com';
const contraseña='hola123';

function loguear(){
    let email =document.getElementById("email").value;
    let pass = document.getElementById("password").value;

    if(email===correo && pass===contraseña){
        window.location="recepcionista/inicio.html";
    }else{
        alert("Credenciales incorrectas")
    }
}


