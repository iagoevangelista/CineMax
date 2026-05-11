import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router'; 
import { AuthService } from '../../services/auth.service'; // Asegúrate de que esta ruta sea correcta según tu carpeta

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  
  // Aquí guardamos lo que el usuario escribe
  loginData = {
    email: '',
    password: ''
  };

  // Un solo constructor con el Router y el AuthService inyectados
  constructor(private authService: AuthService, private router: Router) {}

  // Un solo método para iniciar sesión
  iniciarSesion() {
    // 1. Validamos que no intenten entrar con los campos vacíos
    if (!this.loginData.email || !this.loginData.password) {
      alert("Por favor ingresa tu correo y contraseña");
      return; // Detiene la ejecución para no enviarlo al backend
    }

    console.log("Enviando credenciales al backend...", this.loginData);

    // 2. Llamada real al backend en Spring Boot
    this.authService.login(this.loginData).subscribe({
      next: (res) => {
        // Si el backend responde OK (200), cerramos el panel
        document.getElementById('cerrarLogin')?.click();
        
        // Y lo mandamos al panel dashboard de admin
        this.router.navigate(['/admin/dashboard']);;
      },
      error: (err) => {
        // Si el backend rechaza la clave (403/401)
        console.error("Error al iniciar sesión:", err);
        alert("Correo o contraseña incorrectos");
      }
    });
  }
}