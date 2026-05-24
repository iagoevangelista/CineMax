import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BookingService } from '../../../services/booking'; // Ajusta la ruta
import { AuthService } from '../../../services/auth.service';
import { ShowtimeService } from '../../../services/showtime.service';
import { SeatService } from '../../../services/seat.service';

@Component({
  selector: 'app-seats',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './seats.html',
  styleUrl: './seats.css'
})
export class Seats implements OnInit {
  // === VARIABLES DE ASIENTOS ===
  filas = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  columnas = [1, 2, 3, 4, 5, 6, 7, 8];
  
  asientosOcupados: string[] = []; 
  asientosDiscapacitados: string[] = []; 
  asientosSeleccionados: string[] = [];
  resumenActual: any = {
    pelicula: { nombre: '', poster: '', formato: '' },
    cine: '',
    fechaHora: '',
    asientos: []
  };

  constructor(
    private router: Router,
    private bookingService: BookingService,
    private authService: AuthService,
    private seatService: SeatService,
    private showtimeService: ShowtimeService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    const ID_PRUEBA = 9; 
    const estadoPrevio = this.bookingService.obtenerResumen();
    
    this.resumenActual = this.bookingService.obtenerResumen();
  
    if (!estadoPrevio || !estadoPrevio.horarioId || estadoPrevio.horarioId !== ID_PRUEBA) {
      // Si no había reserva o era de otra película, creamos una nueva "en blanco"
      console.log("Iniciando nueva reserva desde cero...");
      this.bookingService.iniciarReserva(ID_PRUEBA);
      this.resumenActual = this.bookingService.obtenerResumen();
    } else {
      // ¡MAGIA! Si ya había una reserva (el usuario retrocedió), la recuperamos
      console.log("Recuperando reserva existente...");
      this.resumenActual = estadoPrevio; // Mantenemos la referencia
      // Restauramos los asientos verdes en el mapa
      this.asientosSeleccionados = [...this.resumenActual.asientos]; 
    }

    // 1. CARGA DE PELÍCULA
    this.showtimeService.getShowtimeSummary(ID_PRUEBA).subscribe({
      next: (data) => {
        this.resumenActual.pelicula = { 
          nombre: data.titleMovie,   // 👈 Lo que viene de Java lo guardamos como 'nombre'
          poster: data.posterUrl,    // 👈 Lo guardamos como 'poster'
          formato: data.languageFormat 
        };
        this.resumenActual.cine = data.nameVenue;
        this.resumenActual.fechaHora = `${data.showDate} - ${data.startTime}`;
        
        console.log("✅ Objeto listo para el HTML:", this.resumenActual.pelicula);
        this.cdr.detectChanges(); 
      },
      error: (err) => console.error("❌ Error en Showtime:", err)
    });
  
    // 2. CARGA DE ASIENTOS
    this.seatService.getSeatsStatusByShowtime(ID_PRUEBA).subscribe({
      next: (asientos) => {
        console.log("🔍 Datos de Asientos:", asientos);
        this.asientosOcupados = [];
        this.asientosDiscapacitados = []; // Reiniciamos por si acaso
  
        asientos.forEach(a => {
          const codigo = `${a.rowLetter}${a.columnNumber}`;
          
          if (a.isOccupied === true) { 
            this.asientosOcupados.push(codigo);
          }
          
          // Si tu DTO trae el tipo, lo marcamos aquí
          if (a.nameSeatType === 'Discapacitado') {
            this.asientosDiscapacitados.push(codigo);
          }
        });
        this.cdr.detectChanges();
      }
    });
  }

  // === LÓGICA DE ASIENTOS ===
  seleccionarAsiento(asientoId: string) {
    if (this.asientosOcupados.includes(asientoId)) return; 

    const index = this.asientosSeleccionados.indexOf(asientoId);
    if (index > -1) {
      this.asientosSeleccionados.splice(index, 1);
    } else {
      if (this.asientosSeleccionados.length >= 10) {
        alert('Solo puedes seleccionar un máximo de 10 asientos por compra.');
        return;
      }
      this.asientosSeleccionados.push(asientoId);
    }
    this.resumenActual.asientos = [...this.asientosSeleccionados];
  }

  estadoAsiento(asientoId: string): string {
    if (this.asientosOcupados.includes(asientoId)) return 'ocupado';
    if (this.asientosSeleccionados.includes(asientoId)) return 'seleccionado';
    if (this.asientosDiscapacitados.includes(asientoId)) return 'discapacitado';
    return 'disponible';
  }

  irSiguientePaso() {
    if (this.asientosSeleccionados.length === 0) {
      alert('Por favor selecciona al menos un asiento.');
      return;
    }

    this.bookingService.guardarAsientos(this.asientosSeleccionados);
    
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/tickets']);
    } else {
      const loginTrigger = document.querySelector('.user-icon') as HTMLElement;
      loginTrigger?.click(); 
    }
  }
}