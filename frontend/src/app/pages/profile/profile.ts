import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router'; 
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  
  pestanaActiva: string = 'datos'; 
  cargando: boolean = false;
  guardando: boolean = false; // <-- Controla el botón

  mensaje: string = '';
  esError: boolean = false;

  formDatos = {
    firstName: '', lastName: '', email: '', idDocumentType: 1, 
    documentNumber: '', phone: '', datebirth: '', imageUrl: ''
  };

  formSeguridad = { oldPassword: '', newPassword: '', confirmPassword: '' };

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
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
      return;
    }
    this.cargarPerfil();

    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.pestanaActiva = params['tab'];
        this.mensaje = ''; 
        this.cdr.detectChanges(); 
      }
    });
  }

  cambiarPestana(pestana: string) {
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
    this.formDatos.firstName = res.firstName || '';
    this.formDatos.lastName = res.lastName || '';
    this.formDatos.email = res.email || '';
    this.formDatos.idDocumentType = res.idDocumentType || 1;
    this.formDatos.documentNumber = res.documentNumber || '';
    this.formDatos.phone = res.phone || '';
    this.formDatos.datebirth = res.datebirth || '';
    this.formDatos.imageUrl = res.imageUrl || ''; 
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
    this.guardando = true;
    this.mensaje = '';
    
    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(this.formDatos)], { type: 'application/json' }));
    
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
          this.imagenPrevia = null; // Borramos la vista previa para que cargue la URL de Cloudinary real
          
          // 1. Actualizamos el formulario de esta pantalla al instante
          this.actualizarVariablesLocales(res);
          
          // 2. AVISAMOS A TODA LA PÁGINA (Al Navbar) QUE CAMBIAMOS DE FOTO/NOMBRE
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
    this.guardando = true;
    this.mensaje = '';
    const payload = {
      firstName: this.formDatos.firstName, lastName: this.formDatos.lastName, phone: this.formDatos.phone, datebirth: this.formDatos.datebirth,
      oldPassword: this.formSeguridad.oldPassword, newPassword: this.formSeguridad.newPassword
    };

    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    this.userService.updateProfile(formData)
      .pipe(finalize(() => { this.guardando = false; this.cdr.detectChanges(); }))
      .subscribe({
      next: () => {
        this.esError = false;
        this.mensaje = '¡Contraseña cambiada con éxito!';
        this.formSeguridad = { oldPassword: '', newPassword: '', confirmPassword: '' };
      },
      error: (err) => {
        this.esError = true;
        this.mensaje = err.error?.message || 'Contraseña actual incorrecta.';
      }
    });
  }

  eliminarCuenta() {
    const confirmar = confirm('¿Estás completamente seguro de eliminar tu cuenta de CineMax? Esta acción es irreversible y perderás tu historial.');
    if (confirmar) {
      this.guardando = true;
      alert('Tu solicitud de eliminación lógica ha sido procesada. Cerrando sesión.');
      this.authService.logout();
      this.router.navigate(['/']);
      setTimeout(() => window.location.reload(), 500);
    }
  }
}