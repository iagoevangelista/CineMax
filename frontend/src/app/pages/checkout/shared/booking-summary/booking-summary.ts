import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingResumen } from '../../../../services/booking';


export interface BookingSummarySections {
  pelicula:  boolean;
  cine:      boolean;
  asientos:  boolean;
  tickets:   boolean;
  snacks:    boolean;
  total:     boolean;
}

// Por defecto muestra todo
const DEFAULT_SECTIONS: BookingSummarySections = {
  pelicula: true,
  cine:     true,
  asientos: true,
  tickets:  true,
  snacks:   true,
  total:    true,
};

@Component({
  selector: 'app-booking-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-summary.html',
  styleUrl: './booking-summary.css'
})
export class BookingSummaryComponent {

  @Input() resumen!: BookingResumen;

  @Input() set secciones(val: Partial<BookingSummarySections>) {
    this._secciones = { ...DEFAULT_SECTIONS, ...val };
  }
  get secciones(): BookingSummarySections { return this._secciones; }
  private _secciones: BookingSummarySections = { ...DEFAULT_SECTIONS };

  get totalTickets(): number {
    return this.resumen?.tickets?.reduce((sum, t) => sum + t.subtotal, 0) ?? 0;
  }

  get totalSnacks(): number {
    return this.resumen?.snacks?.reduce((sum, s) => sum + s.subtotal, 0) ?? 0;
  }

  get totalFinal(): number {
    return this.totalTickets + this.totalSnacks;
  }

  get asientosTexto(): string {
    const lista = this.resumen?.asientos;
    return lista?.length ? lista.join(', ') : 'Ninguno';
  }
}