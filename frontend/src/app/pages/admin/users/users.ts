import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { UserService } from '../../../services/user.service';
import { VenueService } from '../../../services/venue.service';
// 1. Importación correcta
import { ROLE_LABELS } from '../../utils/role-labels';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  listaUsuarios: any[] = [];
  listaSedes: any[] = []; 
  cargando: boolean = false;
  public ROLE_LABELS = ROLE_LABELS;
  // 2. Exponemos la función importada al HTML

  usuarioSeleccionado: any = null;
  nuevoIdRole: number = 0;

  nuevoUsuario = {
    firstName: '', lastName: '', email: '', password: '',
    documentNumber: '', idDocumentType: 1, idRole: 2, idVenue: 0,
  };

  mostrarSedeCreate = false; 
  mostrarSedeUpdate = false;
  nuevoIdVenue: number = 0;

  filtroEstado: string = 'Activo';
  filtroRol: string = 'TODOS';
  filtroSede: string = 'TODOS';

  get rolesDisponibles() {
    return [...new Set(this.listaUsuarios.map(u => u.roleName))];
  }

  get sedesDisponibles() {
    return [...new Set(this.listaUsuarios.map(u => u.venueName || 'Acceso Total'))];
  }

  get usuariosFiltrados() {
    return this.listaUsuarios.filter(user => {
      const cumpleEstado = this.filtroEstado === 'TODOS' || user.status === this.filtroEstado;
      const cumpleRol = this.filtroRol === 'TODOS' || user.roleName === this.filtroRol;
      const cumpleSede = this.filtroSede === 'TODOS' || (user.venueName || 'Acceso Total') === this.filtroSede;
      
      return cumpleEstado && cumpleRol && cumpleSede;
    });
  }

  constructor(
    private userService: UserService, 
    private venueService: VenueService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios(); 
  }

  // ELIMINAMOS EL MÉTODO LOCAL 'getRoleLabel' QUE CAUSABA CONFLICTO

  cargarUsuarios() {
    this.cargando = true;
    this.userService.getUsers().subscribe({
      next: (datos) => {
        this.listaUsuarios = datos;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Error al cargar usuarios:", err);
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  onRoleChangeUpdate() {
    if (this.nuevoIdRole == 1 || this.nuevoIdRole == 2) {
      this.mostrarSedeUpdate = false;
      this.nuevoIdVenue = 0;
    } else {
      this.mostrarSedeUpdate = true;
      this.venueService.getAvailableVenuesForRole(this.nuevoIdRole).subscribe({
        next: (sedes) => { this.listaSedes = sedes; this.cdr.detectChanges(); },
        error: (err) => console.error("Error al traer sedes:", err)
      });
    }
  }

  onRoleChangeCreate() {
    if (this.nuevoUsuario.idRole == 1 || this.nuevoUsuario.idRole == 2) {
      this.mostrarSedeCreate = false;
      this.nuevoUsuario.idVenue = 0;
    } else {
      this.mostrarSedeCreate = true;
      this.venueService.getAvailableVenuesForRole(this.nuevoUsuario.idRole).subscribe({
        next: (sedes) => { this.listaSedes = sedes; this.cdr.detectChanges(); },
        error: (err) => console.error("Error al traer sedes:", err)
      });
    }
  }

  abrirModalRol(usuario: any) {
    this.usuarioSeleccionado = usuario;
    this.nuevoIdRole = usuario.idRole; 
    this.nuevoIdVenue = usuario.idVenue || 0; 
    this.onRoleChangeUpdate(); 
  }

  guardarNuevoRol() {
    if (!this.usuarioSeleccionado || this.nuevoIdRole === 0) return;

    if ((this.nuevoIdRole == 3 || this.nuevoIdRole == 5) && this.nuevoIdVenue == 0) {
      alert("¡Atención! Un Gerente de Marketing u Operaciones DEBE tener una nueva sede asignada.");
      return;
    }

    const payloadUpdate = {
      idRole: this.nuevoIdRole,
      idVenue: this.nuevoIdVenue == 0 ? null : this.nuevoIdVenue
    };

    this.userService.updateUserRole(this.usuarioSeleccionado.idUser, payloadUpdate).subscribe({
      next: (res) => {
        alert('¡Rol y permisos actualizados con éxito!');
        document.getElementById('cerrarModalRol')?.click();
        this.cargarUsuarios(); 
      },
      error: (err) => {
        console.error("Error al actualizar:", err);
        alert('Hubo un error al actualizar los permisos.');
      }
    });
  }

  guardarNuevoUsuario() {
    if (!this.nuevoUsuario.firstName || !this.nuevoUsuario.email || !this.nuevoUsuario.password || !this.nuevoUsuario.documentNumber) {
      alert("Por favor llena los datos principales, incluyendo el documento de identidad.");
      return;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/;
    if (!emailRegex.test(this.nuevoUsuario.email)) {
      alert('Por favor ingresa un correo válido. Debe contener un "@" y terminar en ".com" (Ejemplo: colaborador@empresa.com).');
      return;
    }

    if (this.nuevoUsuario.idRole == 1 || this.nuevoUsuario.idRole == 2) {
      this.nuevoUsuario.idVenue = 0; 
    }

    if ((this.nuevoUsuario.idRole == 3 || this.nuevoUsuario.idRole == 5) && this.nuevoUsuario.idVenue == 0) {
      alert("¡Atención! Un Gerente de Marketing u Operaciones DEBE tener una sede asignada obligatoriamente.");
      return;
    }

    this.userService.createUser(this.nuevoUsuario).subscribe({
      next: (res) => {
        alert("¡Colaborador creado exitosamente!");
        document.getElementById('cerrarModalNuevoUser')?.click();
        this.cargarUsuarios(); 
        
        this.mostrarSedeCreate = false; 
        this.nuevoUsuario = { 
          firstName: '', lastName: '', email: '', password: '', 
          documentNumber: '', idDocumentType: 1, idRole: 2, idVenue: 0 
        };
      },
      error: (err) => {
        console.error("Error al crear usuario:", err);
        if (err.error && typeof err.error === 'string') {
            alert(err.error); 
        } else if (err.error && err.error.message) {
            alert(err.error.message);
        } else {
            alert("Error al crear usuario. Verifica que el correo o DNI no estén repetidos.");
        }
      }
    });
  }

  desactivarUsuario(idUser: number) {
    const confirmar = confirm('¿Estás seguro de que deseas desactivar a este colaborador?');
    if (confirmar) {
      this.userService.deleteUser(idUser).subscribe({
        next: (res) => {
          alert('Colaborador desactivado con éxito.');
          this.cargarUsuarios(); 
        },
        error: (err) => {
          console.error("Error al desactivar:", err);
          alert('Hubo un error al intentar desactivar el usuario.');
        }
      });
    }
  }

  activarUsuario(idUser: number) {
    const confirmar = confirm('¿Deseas reactivar a este colaborador para que recupere su acceso al sistema?');
    if (confirmar) {
      this.userService.activateUser(idUser).subscribe({
        next: () => {
          alert('¡Colaborador reactivado con éxito!');
          this.cargarUsuarios(); 
        },
        error: (err) => {
          console.error("Error al reactivar:", err);
          alert(err.error?.message || err.error || 'Hubo un error al intentar reactivar el usuario.');
        }
      });
    }
  }
}