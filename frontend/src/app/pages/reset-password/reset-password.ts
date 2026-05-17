import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
    private authService: AuthService,
    private cdr: ChangeDetectorRef // Nos ayuda a pintar los mensajes al instante
  ) {}

  ngOnInit() {
    // Solo extraemos el token de la URL. La interfaz siempre se mostrará al cargar la página.
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    
    if (!this.token) {
      this.errorMessage = 'El enlace de recuperación no es válido o le falta el token de autorización.';
      this.cdr.detectChanges();
    }
  }

  cambiarContrasena() {
    this.errorMessage = '';
    this.successMessage = '';

    // 1. Validación local: campos vacíos
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Por favor, rellena ambos campos de contraseña.';
      return;
    }

    // 2. Validación local: longitud
    if (this.newPassword.length < 6) {
      this.errorMessage = 'Por seguridad, la contraseña debe tener al menos 6 caracteres.';
      return;
    }

    // 3. Validación local: coincidencia
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas ingresadas no coinciden. Por favor, verifica y vuelve a intentarlo.';
      return;
    }

    if (!this.token) {
      this.errorMessage = 'No se puede procesar la solicitud porque falta el token de autorización.';
      return;
    }

    this.loading = true;
    this.cdr.detectChanges(); // Muestra la ruedita de carga en el botón inmediatamente

    // Enviamos los datos al backend para la verificación final del tiempo
    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.successMessage = '¡Contraseña restablecida con éxito! Redirigiéndote al inicio...';
        this.cdr.detectChanges();
        
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
      },
      error: (err: any) => {
        this.loading = false;
        console.error("Error capturado en el envío:", err);
        
        // CAPTURA INMEDIATA AL PICAR EL BOTÓN: Extraemos el mensaje real de Spring Boot (el del desfase de los 15 min)
        this.errorMessage = err.error?.message || err.error?.error || 'El token ha expirado o ya fue utilizado.';
        
        this.cdr.detectChanges(); // Forzamos a Angular a renderizar el aviso de expiración rojo en la pantalla
      }
    });
  }
}