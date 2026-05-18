import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  datosUsuario: any = {};
  
  // Objeto enlazado al formulario de edición
  formUpdate = {
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    password: ''
  };

  mensajeFeedback: string = '';
  esError: boolean = false;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.obtenerDatosPerfil();
  }

  obtenerDatosPerfil() {
    this.userService.getProfile().subscribe({
      next: (res) => {
        this.datosUsuario = res;
        // Cargamos los valores iniciales en el formulario editable
        this.formUpdate.firstName = res.firstName;
        this.formUpdate.lastName = res.lastName;
        this.formUpdate.phone = res.phone || '';
        this.formUpdate.address = res.address || '';
      },
      error: (err) => console.error('Error cargando los datos del perfil:', err)
    });
  }

  guardarCambios() {
    this.mensajeFeedback = '';
    
    // Preparación del objeto de envío
    const payload = { ...this.formUpdate };
    // Si la contraseña se deja en blanco, la enviamos vacía para evitar que Java intente validarla
    if (!payload.password) {
      payload.password = '';
    }

    this.userService.updateProfile(payload).subscribe({
      next: (res: any) => {
        this.esError = false;
        this.mensajeFeedback = res.message || 'Cambios guardados con éxito.';
        this.formUpdate.password = ''; // Limpiamos campo por seguridad
        this.obtenerDatosPerfil(); // Refrescamos la vista con los nuevos datos de la BD
      },
      error: (err) => {
        this.esError = true;
        // Leemos el mensaje exacto enviado por nuestro GlobalExceptionHandler de Java
        this.mensajeFeedback = err.error?.message || 'Error al intentar actualizar los datos.';
      }
    });
  }
}