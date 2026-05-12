import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './client-layout.html',
  styleUrl: './client-layout.css',
})
export class ClientLayout {

  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', email: '', password: '' };

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}


  iniciarSesion() {
    if (!this.loginData.email || !this.loginData.password) {
      alert('Por favor ingresa tu correo y contrasena');
      return;
    }
    this.authService.login(this.loginData).subscribe({
      next: () => {
        document.getElementById('cerrarLogin')?.click();
        this.router.navigate(['/admin/dashboard']);
      },
      error: (err: any) => {
        console.error('Error al iniciar sesion:', err);
        alert('Correo o contrasena incorrectos');
      }
    });
  }

  registrarse() {
    if (!this.registerData.firstName || !this.registerData.email || !this.registerData.password) {
      alert('Por favor completa todos los campos');
      return;
    }
    this.authService.register(this.registerData).subscribe({
      next: () => {
        alert('Registro exitoso. Ya puedes iniciar sesion.');
        document.getElementById('cerrarRegister')?.click();
      },
      error: (err: any) => {
        console.error('Error al registrarse:', err);
        alert('Error al registrarse. Intenta de nuevo.');
      }
    });
  }

}
