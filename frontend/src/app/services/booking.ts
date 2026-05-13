import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  
  private reserva = {
    pelicula: { nombre: '', formato: '', imagen: '' }, 
    cine: 'CINEMAX - PLAZA ANGAMOS',
    fechaHora: '19 MAYO - 3:45pm',
    asientos: [] as string[],
    boletos: [],
    snacks: [],
    totalPagar: 0
  };

  constructor() { }

  // Permite que las pantallas vean qué hay en la reserva
  obtenerResumen() {
    return this.reserva;
  }

  // Corregido: Ahora usa 'this.reserva' en lugar de 'resumen'
  guardarPelicula(movie: any) {
    this.reserva.pelicula = {
      nombre: movie.nombre || movie.title,
      formato: movie.formato || '2D - Doblada',
      imagen: movie.imagen || movie.poster_path
    };
  }

  // Guarda los asientos que elijas en el Paso 1
  guardarAsientos(asientosSeleccionados: string[]) {
    this.reserva.asientos = asientosSeleccionados;
  }

  private seleccionSnacks: any[] = [];

  guardarSnacks(snacks: any[]) {
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