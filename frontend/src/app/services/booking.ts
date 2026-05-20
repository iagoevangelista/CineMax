import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  
  private reserva = {
    horarioId: null as number | null,
    pelicula: { nombre: '', formato: '', imagen: '' }, 
    cine: 'CINEMAX - PLAZA ANGAMOS',
    fechaHora: '19 MAYO - 3:45pm',
    asientos: [] as string[],
    boletos: { adulto: 0, nino: 0 },
    snacks: [] as any[],
    totalPagar: 0
  };

  private seleccionSnacks: any[] = [];

  constructor() { }

  iniciarReserva(idShowtime: number) {
    this.reserva = {
      horarioId: idShowtime,
      pelicula: { nombre: '', formato: '', imagen: '' }, 
      cine: '', 
      fechaHora: '',
      asientos: [],
      boletos: { adulto: 0, nino: 0 },
      snacks: [],
      totalPagar: 0
    };
    console.log("Nueva reserva iniciada en el Service para el ID:", idShowtime);
  }

  // Permite que las pantallas vean qué hay en la reserva
  obtenerResumen() {
    return this.reserva;
  }

  // Guarda los datos de la película (puedes llamarlo desde el MovieDetail o desde el Seats)
  guardarPelicula(movie: any) {
    this.reserva.pelicula = {
      nombre: movie.nombre || movie.title || movie.titleMovie, // Adaptado a tu DTO de Java
      formato: movie.formato || '2D - Doblada',
      imagen: movie.imagen || movie.poster_path || movie.posterUrl
    };
  }

  // Guarda los asientos que elijas en el Paso 1
  guardarAsientos(asientosSeleccionados: string[]) {
    this.reserva.asientos = asientosSeleccionados;
  }

  // === LÓGICA DE SNACKS ===
  guardarSnacks(snacks: any[]) {
    this.reserva.snacks = snacks;
    this.seleccionSnacks = snacks;
    localStorage.setItem('snacksSeleccionados', JSON.stringify(snacks));
  }

  obtenerSnacks() {
    const snacks = localStorage.getItem('snacksSeleccionados');
    return snacks ? JSON.parse(snacks) : [];
  }

  calcularTotalSnacks() {
    return this.seleccionSnacks.reduce((total, s) => total + (s.precio * s.cantidad), 0);
  }
}