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
    documentNumber: '', // <-- NUEVO CAMPO
    idDocumentType: 1,  // <-- NUEVO CAMPO (1 = DNI en la BD)
    idRole: 2, 
    idVenue: 0
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
    // 1. Validación básica: que no envíen datos vacíos, incluyendo el DOCUMENTO (DNI)
    if (!this.nuevoUsuario.firstName || !this.nuevoUsuario.email || !this.nuevoUsuario.password || !this.nuevoUsuario.documentNumber) {
      alert("Por favor llena los datos principales, incluyendo el documento de identidad.");
      return;
    }

    // 2. Limpieza de datos: Si es ADMIN (1) o GERENTE_GRAL (2), NO llevan sede
    // (Forzamos a 0 por si el administrador seleccionó una sede por error y luego cambió de rol en el combo)
    if (this.nuevoUsuario.idRole == 1 || this.nuevoUsuario.idRole == 2) {
      this.nuevoUsuario.idVenue = 0; 
    }

    // 3. Validación de regla de negocio: Si es MKT (3) u OPERACIONES (5), DEBEN tener sede
    if ((this.nuevoUsuario.idRole == 3 || this.nuevoUsuario.idRole == 5) && this.nuevoUsuario.idVenue == 0) {
      alert("¡Atención! Un Gerente de Marketing u Operaciones DEBE tener una sede asignada obligatoriamente.");
      return;
    }

    // 4. Enviamos los datos al backend
    this.userService.createUser(this.nuevoUsuario).subscribe({
      next: (res) => {
        alert("¡Colaborador creado exitosamente!");
        
        // Cerramos el modal usando su ID
        document.getElementById('cerrarModalNuevoUser')?.click();
        
        // Refrescamos la tabla para ver al nuevo trabajador
        this.cargarUsuarios(); 
        
        // Limpiamos el formulario para la próxima vez
        this.nuevoUsuario = { 
          firstName: '', 
          lastName: '', 
          email: '', 
          password: '', 
          documentNumber: '', 
          idDocumentType: 1, 
          idRole: 2, 
          idVenue: 0 
        };
      },
      error: (err) => {
        console.error("Error al crear usuario:", err);
        
        // TRUCO PRO: Si el backend lanza nuestra RuntimeException (Ej: "Error: Ya existe un Gerente General..."), lo mostramos.
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