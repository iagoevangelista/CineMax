// reset-password.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
// ✅ CAMBIO: ReactiveFormsModule en lugar de FormsModule
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

// ✅ NUEVO: Validador personalizado para confirmar que ambas contraseñas coinciden
function passwordsCoinciden(group: AbstractControl): ValidationErrors | null {
  const nueva = group.get('newPassword')?.value;
  const confirmar = group.get('confirmPassword')?.value;
  return nueva === confirmar ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html'
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  resetForm!: FormGroup;           // ✅ NUEVO
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;
  mostrarPassword = false;          // ✅ NUEVO: toggle de visibilidad
  mostrarConfirm = false;           // ✅ NUEVO

  constructor(
    private fb: FormBuilder,        // ✅ NUEVO
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';

    // ✅ NUEVO: FormGroup con validador cruzado de coincidencia de contraseñas
    this.resetForm = this.fb.group({
      newPassword:     ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: passwordsCoinciden });

    if (!this.token) {
      this.errorMessage = 'El enlace de recuperación no es válido o le falta el token de autorización.';
      this.cdr.detectChanges();
    }
  }

  // ✅ Helpers para acceder a controles en el HTML
  get nf() { return this.resetForm.controls; }

  cambiarContrasena() {
    this.errorMessage = '';
    this.successMessage = '';
    this.resetForm.markAllAsTouched();

    if (this.resetForm.invalid) {
      if (this.resetForm.errors?.['noCoinciden']) {
        this.errorMessage = 'Las contraseñas ingresadas no coinciden. Por favor, verifica y vuelve a intentarlo.';
      } else {
        this.errorMessage = 'Por favor, rellena ambos campos correctamente.';
      }
      return;
    }

    if (!this.token) {
      this.errorMessage = 'No se puede procesar la solicitud porque falta el token de autorización.';
      return;
    }

    this.loading = true;
    this.cdr.detectChanges();

    this.authService.resetPassword(this.token, this.resetForm.value.newPassword).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.successMessage = '¡Contraseña restablecida con éxito! Redirigiéndote al inicio...';
        this.cdr.detectChanges();
        setTimeout(() => { this.router.navigate(['/']); }, 3000);
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.error?.error || 'El token ha expirado o ya fue utilizado.';
        this.cdr.detectChanges();
      }
    });
  }
}