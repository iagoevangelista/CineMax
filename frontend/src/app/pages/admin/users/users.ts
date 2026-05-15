import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { UserService } from '../../../services/user.service';
import { VenueService } from '../../../services/venue.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  listaUsuarios: any[] = [];
  listaSedes: any[] = []; // Se llenará dinámicamente con sedes libres
  cargando: boolean = false;

  // <-- Variables para el modal de Cambiar Rol -->
  usuarioSeleccionado: any = null;
  nuevoIdRole: number = 0;

  // <-- Objeto para crear un Nuevo Colaborador -->
  nuevoUsuario = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    documentNumber: '', 
    idDocumentType: 1,  
    idRole: 2, // Por defecto empieza en Gerente General
    idVenue: 0,
  };

  // <-- Bandera que controla el HTML -->
  mostrarSedeCreate = false; // Empieza en falso porque el idRole por defecto es 2

  constructor(
    private userService: UserService, 
    private venueService: VenueService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios(); 
    // Fíjate que al iniciar ya no cargamos sedes. 
    // Las cargaremos SOLO cuando el usuario elija el rol 3 o 5.
  }

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

  // --- NUEVA FUNCIÓN ESTRELLA ---
  // Esta función se activará desde el HTML cada vez que toques el <select> de Rol
  onRoleChangeCreate() {
    // Si eligen Rol 1 o 2 (Admin o Gerente General)
    if (this.nuevoUsuario.idRole == 1 || this.nuevoUsuario.idRole == 2) {
      this.mostrarSedeCreate = false; // Bajamos la bandera (el HTML ocultará el div de sede)
      this.nuevoUsuario.idVenue = 0;  // Limpiamos los datos
      this.listaSedes = [];           // Vaciamos la lista de sedes
    } 
    // Si eligen Rol 3 o 5 (Gerentes de Sede)
    else {
      this.mostrarSedeCreate = true;  // Subimos la bandera (el HTML mostrará el div de sede)
      this.nuevoUsuario.idVenue = 0;  // Obligamos al usuario a seleccionar una de la lista
      
      // Llamamos a tu genial Endpoint en Spring Boot
      this.venueService.getAvailableVenuesForRole(this.nuevoUsuario.idRole).subscribe({
        next: (sedesDisponibles) => {
          this.listaSedes = sedesDisponibles; // Llenamos la lista solo con las que devolvió SQL Server
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Error al traer sedes disponibles:", err);
        }
      });
    }
  }

  abrirModalRol(usuario: any) {
    this.usuarioSeleccionado = usuario;
    this.nuevoIdRole = usuario.idRole; 
  }

  guardarNuevoRol() {
    if (!this.usuarioSeleccionado || this.nuevoIdRole === 0) return;

    this.userService.updateUserRole(this.usuarioSeleccionado.idUser, this.nuevoIdRole).subscribe({
      next: (res) => {
        alert('¡Rol actualizado con éxito!');
        document.getElementById('cerrarModalRol')?.click();
        this.cargarUsuarios(); 
      },
      error: (err) => {
        console.error("Error al actualizar:", err);
        alert('Hubo un error al actualizar el rol.');
      }
    });
  }

  guardarNuevoUsuario() {
    if (!this.nuevoUsuario.firstName || !this.nuevoUsuario.email || !this.nuevoUsuario.password || !this.nuevoUsuario.documentNumber) {
      alert("Por favor llena los datos principales, incluyendo el documento de identidad.");
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
        
        // Al limpiar, volvemos a bajar la bandera de sede para el siguiente usuario que se vaya a crear
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
}