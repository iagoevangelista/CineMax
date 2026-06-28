import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { finalize } from 'rxjs/operators';

// Mismo patrón: mínimo 8 caracteres, al menos una letra y un número.
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;

function confirmPasswordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPassword = group.get('newPassword')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  if (!newPassword || !confirmPassword) return null;
  return newPassword === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {

  pestanaActiva: string = 'datos';
  cargando: boolean = false;
  guardando: boolean = false;
  esAdmin: boolean = false;

  mensaje: string = '';
  esError: boolean = false;

  // Formularios
  datosForm: FormGroup = new FormGroup({});
  seguridadForm: FormGroup = new FormGroup({});
  formDatosEnviado = false;
  formSeguridadEnviado = false;

  perfilSoloLectura = { email: '', idDocumentType: 1, documentNumber: '', imageUrl: '' };

  misCompras: any[] = [
    { idSale: 1042, movieTitle: 'Super Mario Bros 2', venueName: 'CineMax San Miguel', date: '2026-05-18', time: '19:30', amount: 45.00, ticketsCount: 2, roomName: 'Sala 3 (2D)', seats: 'G12, G13' },
    { idSale: 987, movieTitle: 'La Momia', venueName: 'CineMax Villa María', date: '2026-04-12', time: '21:00', amount: 22.50, ticketsCount: 1, roomName: 'Sala 1 (3D)', seats: 'F5' }
  ];

  imagenSeleccionada: File | null = null;
  imagenPrevia: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
  if (!this.authService.isLoggedIn()) {
    this.router.navigate(['/']);
    return;
  }

  this.construirFormularios();
  this.cargarPerfil();
  
  const rol = this.authService.getRole();
  const rolesAdmin = ['GERENTE_GENERAL', 'GERENTE_DE_MARKETING', 'GERENTE_DE_OPERACIONES', 'ADMIN'];
  this.esAdmin = rolesAdmin.includes(rol) || document.referrer.includes('/admin');
  console.log('ROL detectado:', rol, '| esAdmin:', this.esAdmin);
  this.cdr.detectChanges();  
  this.route.queryParams.subscribe(params => {
    if (params['tab']) {
      this.pestanaActiva = params['tab'];
      this.mensaje = '';
      this.cdr.detectChanges();
    }
  });
}

  // Construcción de formularios

  private construirFormularios(): void {
    this.datosForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      // Opcional: si se completa, debe parecer un número de teléfono (7 a 9 dígitos).
      phone: ['', [Validators.pattern(/^[0-9]{7,9}$/)]],
      datebirth: ['', [this.fechaNacimientoValidator]],
    });

    this.seguridadForm = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', [Validators.required]],
    }, { validators: confirmPasswordMatchValidator });
  }

  // Evita fechas de nacimiento futuras 
  private fechaNacimientoValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const fecha = new Date(control.value);
    const hoy = new Date();
    return fecha > hoy ? { fechaFutura: true } : null;
  }

  campoDatosInvalido(nombreControl: string): boolean {
    const control = this.datosForm.get(nombreControl);
    if (!control) return false;
    return control.invalid && (control.touched || this.formDatosEnviado);
  }

  campoSeguridadInvalido(nombreControl: string): boolean {
    const control = this.seguridadForm.get(nombreControl);
    if (!control) return false;
    return control.invalid && (control.touched || this.formSeguridadEnviado);
  }

  get confirmPasswordNoCoincide(): boolean {
    const tocado = this.seguridadForm.get('confirmPassword')?.touched || this.formSeguridadEnviado;
    return !!tocado && this.seguridadForm.hasError('passwordMismatch');
  }

  cambiarPestana(pestana: string) {
    this.mensaje = '';
    this.router.navigate([], { relativeTo: this.route, queryParams: { tab: pestana } });
  }

  cargarPerfil(esSilencioso: boolean = false) {
    if (!esSilencioso) this.cargando = true;

    this.userService.getProfile().subscribe({
      next: (res) => {
        this.actualizarVariablesLocales(res);
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar perfil:', err);
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Método auxiliar para no repetir código
  private actualizarVariablesLocales(res: any) {
    this.datosForm.patchValue({
      firstName: res.firstName || '',
      lastName: res.lastName || '',
      phone: res.phone || '',
      datebirth: res.datebirth || '',
    });
    this.perfilSoloLectura = {
      email: res.email || '',
      idDocumentType: res.idDocumentType || 1,
      documentNumber: res.documentNumber || '',
      imageUrl: res.imageUrl || '',
    };
  }

  // Helpers para las iniciales del avatar
  get inicialNombre(): string {
    return (this.datosForm?.get('firstName')?.value || '').charAt(0).toUpperCase();
  }
  get inicialApellido(): string {
    return (this.datosForm?.get('lastName')?.value || '').charAt(0).toUpperCase();
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.type.match(/image\/*/) == null) {
        alert("Solo se permiten imágenes.");
        return;
      }
      this.imagenSeleccionada = file;
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        this.imagenPrevia = reader.result as string;
        this.cdr.detectChanges();
      };
    }
  }

  guardarDatosPersonales() {
    this.formDatosEnviado = true;
    this.mensaje = '';

    if (this.datosForm.invalid) {
      this.datosForm.markAllAsTouched();
      return;
    }

    this.guardando = true;

    const payload = {
      ...this.datosForm.value,
      idDocumentType: this.perfilSoloLectura.idDocumentType,
      documentNumber: this.perfilSoloLectura.documentNumber,
    };

    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    if (this.imagenSeleccionada) {
      formData.append('image', this.imagenSeleccionada);
    }

    this.userService.updateProfile(formData)
      .pipe(
        finalize(() => {
          this.guardando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res: any) => {
          this.esError = false;
          this.mensaje = '¡Datos actualizados correctamente!';
          this.imagenSeleccionada = null;
          this.imagenPrevia = null; 
          this.formDatosEnviado = false;

          this.actualizarVariablesLocales(res);
          this.userService.updateLocalUser(res);

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error:', err);
          this.esError = true;
          this.mensaje = err.error?.message || 'Error al actualizar.';
        }
      });
  }

  actualizarContrasena() {
    this.formSeguridadEnviado = true;
    this.mensaje = '';

    if (this.seguridadForm.invalid) {
      this.seguridadForm.markAllAsTouched();
      return;
    }

    this.guardando = true;
    const { oldPassword, newPassword } = this.seguridadForm.value;

    const payload = {
      ...this.datosForm.value,
      oldPassword,
      newPassword,
    };

    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    this.userService.updateProfile(formData)
      .pipe(finalize(() => { this.guardando = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: () => {
          this.esError = false;
          this.mensaje = '¡Contraseña cambiada con éxito!';
          this.formSeguridadEnviado = false;
          this.seguridadForm.reset({ oldPassword: '', newPassword: '', confirmPassword: '' });
        },
        error: (err) => {
          this.esError = true;
          this.mensaje = err.error?.message || 'Contraseña actual incorrecta.';
        }
      });
  }

  eliminarCuenta() {
    const confirmar = confirm('¿Estás completamente seguro de eliminar tu cuenta de CineMax? Esta acción es irreversible y perderás tu historial.');
    if (!confirmar) return;

    this.guardando = true;
    this.mensaje = '';

    this.userService.deleteMyAccount()
      .pipe(finalize(() => { this.guardando = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: () => {
          alert('Tu cuenta ha sido dada de baja correctamente. Cerrando sesión.');
          this.authService.logout();
          this.router.navigate(['/']);
          setTimeout(() => window.location.reload(), 500);
        },
        error: (err) => {
          this.esError = true;
          this.mensaje = err.error?.message || 'No se pudo eliminar la cuenta. Intenta nuevamente.';
        }
      });
  }

  volver(): void {
  this.router.navigate(['/admin/dashboard']);
}

}