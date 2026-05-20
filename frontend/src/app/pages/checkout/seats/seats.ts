import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BookingService } from '../../../services/booking';
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
  resumenActual: any = {};

  constructor(
    private router: Router,
    private bookingService: BookingService,
    private authService: AuthService,
    private seatService: SeatService,
    private showtimeService: ShowtimeService,
    private cdr: ChangeDetectorRef // Para forzar el refresco visual
  ) {}

  ngOnInit() {
    const ID_PRUEBA = 1; 
    this.bookingService.iniciarReserva(ID_PRUEBA);
    this.resumenActual = this.bookingService.obtenerResumen();
  
    // 1. CARGA DE PELÍCULA (Usando tus nombres de ShowtimeSummaryDTO)
    this.showtimeService.getShowtimeSummary(ID_PRUEBA).subscribe({
      next: (data) => {
        console.log("🔍 Datos de Película:", data);
        this.resumenActual.pelicula = { 
          nombre: data.titleMovie,   // 👈 Exacto como tu DTO
          poster: data.posterUrl,    // 👈 Exacto como tu DTO
          formato: data.languageFormat 
        };
        this.resumenActual.cine = data.nameVenue;
        this.resumenActual.fechaHora = `${data.showDate} - ${data.startTime}`;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("❌ Error 404 o conexión en Showtime:", err)
    });
  
    // 2. CARGA DE ASIENTOS (Usando tus nombres de SeatStatusDTO)
    this.seatService.getSeatsStatusByShowtime(ID_PRUEBA).subscribe({
      next: (asientos) => {
        console.log("🔍 Datos de Asientos:", asientos);
        this.asientosOcupados = [];
  
        asientos.forEach(a => {
          // Unimos 'rowLetter' (A) y 'columnNumber' (4) -> "A4"
          const codigo = `${a.rowLetter}${a.columnNumber}`;
          
          // En tu DTO es un Boolean, no un String
          if (a.isOccupied === true) { 
            this.asientosOcupados.push(codigo);
          }
          
        });
        console.log("✅ Asientos ocupados en mapa:", this.asientosOcupados);
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
    // Opcional: Actualizar el resumen en tiempo real para que el HTML lateral se refresque
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

    // Guardamos definitivamente los asientos en la bandeja
    this.bookingService.guardarAsientos(this.asientosSeleccionados);
    
    const isUserLoggedIn = this.authService.isLoggedIn();
    
    if (isUserLoggedIn) {
      this.router.navigate(['/tickets']);
    } else {
      console.log("No hay sesión. Abriendo panel de login...");
      // Intentamos abrir tu panel lateral offcanvas
      const loginTrigger = document.querySelector('.user-icon') as HTMLElement;
      if (loginTrigger) {
        loginTrigger.click(); 
      } else {
        const alternativeTrigger = document.querySelector('[data-bs-target="#authOffcanvas"]') as HTMLElement;
        alternativeTrigger?.click();
      }
    }
  }
}