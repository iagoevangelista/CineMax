import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.html'
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Extraemos el token que viene de la URL del correo: ?token=XXXX
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    
    if (!this.token) {
      this.errorMessage = 'El enlace de recuperación no es válido. Por favor, solicita uno nuevo.';
    }
  }

  cambiarContrasena() {
    this.errorMessage = '';
    this.successMessage = '';

    // 1. Validación de campos vacíos
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Por favor, rellena ambos campos de contraseña.';
      return;
    }

    // 2. Validación de longitud mínima de seguridad
    if (this.newPassword.length < 6) {
      this.errorMessage = 'Por seguridad, la contraseña debe tener al menos 6 caracteres.';
      return;
    }

    // 3. Validación de coincidencia idéntica (Doble escritura)
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas ingresadas no coinciden. Por favor, verifica y vuelve a intentarlo.';
      return;
    }

    if (!this.token) {
      this.errorMessage = 'Falta el token de autorización.';
      return;
    }

    this.loading = true;

    // Enviar datos validados al backend
    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.successMessage = '¡Contraseña restablecida con éxito! Redirigiéndote al inicio...';
        
        // Espera 3 segundos para que el usuario lea el éxito y redirige al Home
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
      },
      error: (err: any) => {
        this.loading = false;
        console.error(err);
        this.errorMessage = err.error?.error || 'El token ha expirado o ya fue utilizado.';
      }
    });
  }
}