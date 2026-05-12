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
  listaSedes: any[] = [];
  cargando: boolean = false;

  // <-- Variables para el modal de Cambiar Rol -->
  usuarioSeleccionado: any = null;
  nuevoIdRole: number = 0;

  // <-- NUEVO: Objeto para crear un Nuevo Colaborador -->
  nuevoUsuario = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    idRole: 2, // Por defecto GERENTE GENERAL
    idVenue: 0 // Por defecto sin sede seleccionada
  };

  // <-- ACTUALIZADO: Inyectamos el VenueService en el constructor -->
  constructor(
    private userService: UserService, 
    private venueService: VenueService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
    this.cargarSedes(); // <-- NUEVO: Traemos la lista de sedes al iniciar
  }

  // Método para cargar la tabla de usuarios
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

  // <-- NUEVO: Método para cargar el combo de sedes
  cargarSedes() {
    this.venueService.getVenues().subscribe((datos) => {
      this.listaSedes = datos;
      this.cdr.detectChanges();
    });
  }

  // Se ejecuta al hacer clic en el botón amarillo de la tabla
  abrirModalRol(usuario: any) {
    this.usuarioSeleccionado = usuario;
    this.nuevoIdRole = usuario.idRole; // Pre-seleccionamos su rol actual
  }

  // Se ejecuta al darle "Guardar Cambios" en el Modal de Rol
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

  // <-- NUEVO: Se ejecuta al darle "Crear Colaborador" en el modal verde
  guardarNuevoUsuario() {
    // 1. Validación básica para que no envíen datos vacíos
    if(!this.nuevoUsuario.firstName || !this.nuevoUsuario.email || !this.nuevoUsuario.password) {
      alert("Por favor llena los datos principales.");
      return;
    }

    // 2. Validación de regla de negocio: Si no es Admin, debe tener sede
    if(this.nuevoUsuario.idRole !== 1 && this.nuevoUsuario.idVenue === 0) {
      alert("Debes asignarle una sede a este gerente.");
      return;
    }

    // 3. Enviamos los datos al backend
    this.userService.createUser(this.nuevoUsuario).subscribe({
      next: (res) => {
        alert("¡Colaborador creado exitosamente!");
        
        // Cerramos el modal usando su ID
        document.getElementById('cerrarModalNuevoUser')?.click();
        
        // Refrescamos la tabla para ver al nuevo trabajador
        this.cargarUsuarios(); 
        
        // Limpiamos el formulario para la próxima vez
        this.nuevoUsuario = { firstName: '', lastName: '', email: '', password: '', idRole: 2, idVenue: 0 };
      },
      error: (err) => {
        console.error("Error al crear usuario:", err);
        alert("Error al crear usuario. Revisa la consola.");
      }
    });
  }
}