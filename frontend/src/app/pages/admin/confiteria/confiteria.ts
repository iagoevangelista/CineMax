import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { ConfiteriaService } from '../../../services/confiteria.service';
import { AuthService } from '../../../services/auth.service';
import { environment } from '../../../enviroments/environment';
import { Modal } from 'bootstrap';

@Component({
  selector: 'app-confiteria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './confiteria.html',
  styleUrls: ['./confiteria.css']
})
export class Confiteria implements OnInit {

  snacks: any[] = [];
  categories: any[] = [];
  sedes: any[] = [];
  sedeSeleccionada: any = null;
  cargando: boolean = false;
  cargandoSedes: boolean = false;
  isEditMode: boolean = false;
  currentSnackId: number | null = null;
  selectedFile: File | null = null;
  esGerenteGeneral: boolean = false;
  idVenueAsignada: number | null = null;
  mostrarModalSede: boolean = false;
  snackRecienCreado: any = null;
  mostrarModalEliminar: boolean = false;
  snackAEliminar: number | null = null;

  currentSnack: any = {
    nameSnack: '', descriptionSnack: '', price: null,
    stock: null, status: 'Activo', idSnackCategory: null
  };

  constructor(
    private confiteriaService: ConfiteriaService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
      return;
    }

    const rol = this.authService.getRole();
    this.esGerenteGeneral = rol === 'GERENTE_GENERAL';
    this.idVenueAsignada = this.authService.getIdVenue();

    this.cargandoSedes = true;
    this.cargarSedes();
    this.cargarCategorias();
  }

  cargarSedes() {
    this.http.get<any[]>(`${environment.apiUrl}/venues/public`).subscribe({
      next: (res) => {
        if (!this.esGerenteGeneral && this.idVenueAsignada) {
          this.sedes = res.filter(s => s.idVenue === this.idVenueAsignada);
        } else {
          this.sedes = res;
        }
        this.cargandoSedes = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoSedes = false;
        this.cdr.detectChanges();
      }
    });
  }

 seleccionarSede(sede: any) {
  this.sedeSeleccionada = sede;
  this.cdr.detectChanges();
  this.cargarSnacksPorSede(sede.idVenue);
}

  cargarSnacks() {
    this.cargando = true;
    this.confiteriaService.cargarSnacks().subscribe({
      next: (res: any) => { this.snacks = res; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  cargarSnacksPorSede(idVenue: number) {
  this.cargando = true;
  this.cdr.detectChanges();
  const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
  this.http.get<any[]>(`${environment.apiUrl}/snacks/venue/${idVenue}/admin`, { headers }).subscribe({
    next: (res) => { 
      this.snacks = res; 
      this.cargando = false; 
      this.cdr.detectChanges(); 
    },
    error: () => { 
      this.cargando = false; 
      this.cdr.detectChanges(); 
    }
  });
}

  cargarCategorias() {
    this.confiteriaService.cargarCategorias().subscribe({
      next: (res: any) => { this.categories = res; this.cdr.detectChanges(); },
      error: (err: any) => console.error('Error categorías:', err)
    });
  }

  abrirModalNuevo() {
    this.isEditMode = false;
    this.currentSnackId = null;
    this.selectedFile = null;
    this.currentSnack = { nameSnack: '', descriptionSnack: '', price: null, stock: null, status: 'Activo', idSnackCategory: null };
  }

  abrirModalEditar(snack: any) {
    this.isEditMode = true;
    this.currentSnackId = snack.idSnack;
    this.selectedFile = null;
    this.currentSnack = {
      nameSnack: snack.nameSnack, descriptionSnack: snack.descriptionSnack,
      price: snack.price, stock: snack.stock, status: snack.status,
      idSnackCategory: snack.idSnackCategory
    };
  }

  onFileSelected(event: any) {
    if (event.target.files.length > 0) this.selectedFile = event.target.files[0];
  }

  private cerrarModalSnack() {
  const modalEl = document.getElementById('modalSnack');
  if (modalEl) {
    const modal = Modal.getInstance(modalEl) || new Modal(modalEl);
    modal.hide();
  }
  setTimeout(() => {
    document.body.classList.remove('modal-open');
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('padding-right');
    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(b => b.remove());
  }, 300);
}

  guardarSnack() {
    if (!this.currentSnack.nameSnack || this.currentSnack.nameSnack.trim() === '') {
      alert('El nombre del producto es obligatorio');
      return;
    }
    if (!this.currentSnack.price || Number(this.currentSnack.price) <= 0) {
      alert('El precio debe ser mayor a S/. 0.00');
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
    const formData = new FormData();
    const snackData = JSON.stringify({
      nameSnack: this.currentSnack.nameSnack,
      descriptionSnack: this.currentSnack.descriptionSnack,
      price: Number(this.currentSnack.price),
      stock: Number(this.currentSnack.stock),
      status: this.currentSnack.status || 'Activo',
      idSnackCategory: Number(this.currentSnack.idSnackCategory)
    });
    formData.append('snack', snackData);
    if (this.selectedFile) formData.append('file', this.selectedFile);

    if (this.isEditMode && this.currentSnackId) {
      this.confiteriaService.actualizarSnack(this.currentSnackId, formData).subscribe({
        next: () => {
          if (this.sedeSeleccionada) {
            this.http.patch(
              `${environment.apiUrl}/snacks/${this.currentSnackId}/venue/${this.sedeSeleccionada.idVenue}/stock?stock=${this.currentSnack.stock}`,
              {},
              { headers }
            ).subscribe({
              next: () => { this.cerrarModalSnack(); this.recargarSnacks(); alert('Producto actualizado'); },
              error: (err: any) => alert('Error al actualizar stock: ' + err.message)
            });
          } else {
            this.cerrarModalSnack();
            this.recargarSnacks();
            alert('Producto actualizado');
          }
        },
        error: (err: any) => alert('Error: ' + err.message)
      });
    } else {
      this.confiteriaService.crearSnack(formData).subscribe({
        next: (res: any) => {
          this.cerrarModalSnack();
          this.snackRecienCreado = res;
          this.mostrarModalSede = true;
          this.cdr.detectChanges();
        },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

  agregarASede(solo: boolean) {
    if (!this.snackRecienCreado) return;
    const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
    const stock = this.snackRecienCreado.stock ?? 0;

    if (solo && this.sedeSeleccionada) {
      this.http.post(
        `${environment.apiUrl}/snacks/${this.snackRecienCreado.idSnack}/venue/${this.sedeSeleccionada.idVenue}/agregar?stock=${stock}`,
        {},
        { headers }
      ).subscribe({
        next: () => { this.mostrarModalSede = false; this.snackRecienCreado = null; this.recargarSnacks(); this.cdr.detectChanges(); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    } else {
      this.http.post(
        `${environment.apiUrl}/snacks/${this.snackRecienCreado.idSnack}/venue/todas/agregar?stock=${stock}`,
        {},
        { headers }
      ).subscribe({
        next: () => { this.mostrarModalSede = false; this.snackRecienCreado = null; this.recargarSnacks(); this.cdr.detectChanges(); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

  recargarSnacks() {
    if (this.sedeSeleccionada) {
      this.cargarSnacksPorSede(this.sedeSeleccionada.idVenue);
    } else {
      this.cargarSnacks();
    }
  }

  inhabilitarSnack(idSnack: number) {
    if (!this.sedeSeleccionada) return;
    if (confirm('¿Inhabilitar este producto en esta sede?')) {
      const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
      this.http.patch(`${environment.apiUrl}/snacks/${idSnack}/venue/${this.sedeSeleccionada.idVenue}/inhabilitar`, {}, { headers }).subscribe({
        next: () => { this.recargarSnacks(); alert('Producto inhabilitado en esta sede'); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

  habilitarSnack(idSnack: number) {
    if (!this.sedeSeleccionada) return;
    if (confirm('¿Habilitar este producto en esta sede?')) {
      const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
      this.http.patch(`${environment.apiUrl}/snacks/${idSnack}/venue/${this.sedeSeleccionada.idVenue}/habilitar`, {}, { headers }).subscribe({
        next: () => { this.recargarSnacks(); alert('Producto habilitado en esta sede'); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

eliminarSnack(idSnack: number) {
  if (!this.sedeSeleccionada) return;

  const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });

  this.http.get<number>(`${environment.apiUrl}/snacks/${idSnack}/sedes-count`, { headers }).subscribe({
    next: (count) => {
      if (count <= 1) {
        if (confirm('¿Eliminar este producto definitivamente?')) {
          this.http.delete(`${environment.apiUrl}/snacks/${idSnack}/venue/todas`, { headers }).subscribe({
            next: () => { 
              this.recargarSnacks(); 
              alert('Producto eliminado'); 
              this.cdr.detectChanges();
            },
            error: (err: any) => alert('Error: ' + err.message)
          });
        }
      } else {
        this.snackAEliminar = idSnack;
        this.mostrarModalEliminar = true;
        this.cdr.detectChanges();
      }
    },
    error: (err: any) => alert('Error al verificar sedes: ' + err.message)
  });
}

confirmarEliminar(todas: boolean) {
  if (!this.snackAEliminar) return;
  const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });

  const url = todas
    ? `${environment.apiUrl}/snacks/${this.snackAEliminar}/venue/todas`
    : `${environment.apiUrl}/snacks/${this.snackAEliminar}/venue/${this.sedeSeleccionada.idVenue}`;

  this.http.delete(url, { headers }).subscribe({
    next: () => {
      this.mostrarModalEliminar = false;
      this.snackAEliminar = null;
      this.recargarSnacks();
      alert('Producto eliminado');
      this.cdr.detectChanges();
    },
    error: (err: any) => alert('Error: ' + err.message)
  });
}
}