import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoomService } from '../../../services/room.service';
import { VenueService } from '../../../services/venue.service';
import { AuthService } from '../../../services/auth.service';
import { SeatService } from '../../../services/seat.service';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rooms.html',
  styleUrl: './rooms.css'
})
export class Rooms implements OnInit {
  
  listaSalas: any[] = [];
  listaSedes: any[] = [];
  
  // Lógica de Roles
  rolUsuario: string = '';
  sedeAsignadaId: number = 0;
  esAdminGlobal: boolean = false;
  sedeFiltroId: number = 0; // Para el combobox del Admin

  nuevaSala = {
    nameRoom: '',
    capacity: 0,
    numRows: 0,
    seatsPerRow: 0,
    status: 'Activo',
    idVenue: 0
  };

  constructor(
    private roomService: RoomService,
    private venueService: VenueService,
    private authService: AuthService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private seatService: SeatService,
  ) {}

  ngOnInit() {
    this.rolUsuario = this.authService.getRole();
    
    // Si es Admin o Gerente General (Tienen control global)
    if (this.rolUsuario === 'ROLE_ADMIN' || this.rolUsuario === 'ROLE_GERENTE_GENERAL' || this.rolUsuario === 'ADMIN' || this.rolUsuario === 'GERENTE_GENERAL') {
      this.esAdminGlobal = true;
      this.cargarSedes();
    } 
    // Si es Gerente de Operaciones (Control local)
    else {
      this.esAdminGlobal = false;
      this.userService.getProfile().subscribe({
        next: (perfil) => {
          console.log("DATOS DEL PERFIL RECIBIDOS:", perfil);
          this.sedeAsignadaId = perfil.idVenue; // El ID de su cine
          this.nuevaSala.idVenue = this.sedeAsignadaId;
          this.cargarSalasPorSede(this.sedeAsignadaId);
        }
      });
    }
  }

  cargarSedes() {
    this.venueService.getVenues().subscribe(sedes => {
      this.listaSedes = sedes;
      this.cdr.detectChanges();
    });
  }

  // Se ejecuta cuando el Admin elige un cine en el desplegable
  onSedeChange() {
    if (this.sedeFiltroId > 0) {
      this.nuevaSala.idVenue = this.sedeFiltroId;
      this.cargarSalasPorSede(this.sedeFiltroId);
    } else {
      this.listaSalas = [];
    }
  }

  cargarSalasPorSede(idVenue: number) {
    this.roomService.getRoomsByVenue(idVenue).subscribe({
      next: (salas) => {
        this.listaSalas = salas;
        this.cdr.detectChanges();
      }
    });
  }

  // Este método calcula la capacidad en tiempo real mientras el usuario escribe
  calcularCapacidad() {
    if (this.nuevaSala.numRows > 0 && this.nuevaSala.seatsPerRow > 0) {
      this.nuevaSala.capacity = this.nuevaSala.numRows * this.nuevaSala.seatsPerRow;
    } else {
      this.nuevaSala.capacity = 0;
    }
  }

  prepararCreacion() {
    this.nuevaSala = {
      nameRoom: '',
      capacity: 0,
      numRows: 0,
      seatsPerRow: 0,
      status: 'Activo',
      idVenue: this.esAdminGlobal ? this.sedeFiltroId : this.sedeAsignadaId
    };
  }

  guardarSala() {
    if (this.nuevaSala.idVenue <= 0) {
      alert("Error: No se ha seleccionado a qué sede pertenece la sala.");
      return;
    }
    if (this.nuevaSala.capacity <= 0) {
      alert("La sala debe tener al menos 1 fila y 1 asiento por fila.");
      return;
    }

    if (confirm(`¿Estás seguro de registrar la ${this.nuevaSala.nameRoom} con ${this.nuevaSala.capacity} asientos? El sistema generará las butacas automáticamente.`)) {
      this.roomService.createRoom(this.nuevaSala).subscribe({
        next: () => {
          alert('¡Sala creada con éxito!');
          document.getElementById('btnCerrarModalSala')?.click();
          this.cargarSalasPorSede(this.nuevaSala.idVenue);
        },
        error: (err) => {
          alert(err.error?.message || 'Error al guardar la sala');
        }
      });
    }
  }

  salaSeleccionadaMatriz: any = null;
  matrizFilas: { fila: string, asientos: any[] }[] = [];
  
  // El "Pincel" actual del gerente (Por defecto: crear pasillos)
  pincelSeleccionado: string = 'OCULTO'; 

  abrirMatriz(sala: any) {
    this.salaSeleccionadaMatriz = sala;
    
    // Llamamos al backend para traer las butacas
    this.seatService.getSeatsByRoom(sala.idRoom).subscribe({
      next: (asientos) => {
        // Agrupamos los asientos por fila (A, B, C...) para poder dibujarlos en el HTML
        const agrupado = asientos.reduce((acc: any, asiento: any) => {
          if (!acc[asiento.rowName]) {
            acc[asiento.rowName] = [];
          }
          acc[asiento.rowName].push(asiento);
          return acc;
        }, {});

        // Convertimos el objeto en un arreglo iterable para el *ngFor del HTML
        this.matrizFilas = Object.keys(agrupado).map(key => {
          return { fila: key, asientos: agrupado[key] };
        });
        
        this.cdr.detectChanges();
      }
    });
  }

  seleccionarPincel(tipo: string) {
    this.pincelSeleccionado = tipo;
  }

  aplicarPincel(asiento: any) {
    // 1. Manejamos la lógica de cambio de estado y tipo de asiento
    switch (this.pincelSeleccionado) {
      case 'ACTIVO':
        asiento.status = 'ACTIVO';
        asiento.idSeatType = 1; // 1 = Estándar (Reseteamos de 2 a 1)
        break;

      case 'MANTENIMIENTO':
        asiento.status = 'MANTENIMIENTO';
        asiento.idSeatType = 1; // Normalmente en mantenimiento no debería ser VIP/Wheelchair
        break;

      case 'OCULTO':
        asiento.status = 'OCULTO';
        asiento.idSeatType = 1; 
        break;

      case 'WHEELCHAIR':
        asiento.status = 'ACTIVO';
        asiento.idSeatType = 2; // 2 = Discapacitados
        break;
        
      default:
        return;
    }

    // 2. Guardamos los cambios
    this.seatService.updateSeat(asiento.idSeat, asiento).subscribe({
      next: () => {
        // Opcional: refrescar localmente si es necesario, 
        // aunque el objeto ya está actualizado por referencia.
        this.cdr.detectChanges();
      },
      error: () => { 
        alert("Error al actualizar la butaca. Intente nuevamente."); 
      }
    });
  }

  obtenerClaseAsiento(asiento: any): string {
    if (asiento.status === 'OCULTO') return 'seat-oculto';
    if (asiento.status === 'MANTENIMIENTO') return 'seat-mantenimiento';
    if (asiento.idSeatType === 2) return 'seat-wheelchair'; // Cambiado a idSeatType
    return 'seat-activo'; 
  }
  
}