import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShowtimeService } from '../../../services/showtime.service';
import { MovieService } from '../../../services/movie.service';
import { RoomService } from '../../../services/room.service';
import { VenueService } from '../../../services/venue.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-showtimes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './showtimes.html',
  styleUrls: ['./showtimes.css']
})
export class AdminShowtimes implements OnInit {

  // Datos de sesión
  idVenueSesion: number | null = null;
  roleUsuario: string = '';
  esGerGeneral: boolean = false;

  // Catálogos
  movies: any[] = [];
  rooms: any[] = [];
  sedes: any[] = [];

  sedeSeleccionadaId: number | null = null;

  // Lista y filtros
  showtimes: any[] = [];
  filtroFecha: string = '';
  filtroSala: number | null = null;
  filtroFormato: string = '';

  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  // Modal
  isEditMode = false;
  currentShowtimeId: number | null = null;
  currentShowtime: any = this.emptyShowtime();
  horaFinEstimada: string = '';

  constructor(
    private showtimeService: ShowtimeService,
    private movieService: MovieService,
    private roomService: RoomService,
    private venueService: VenueService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.filtroFecha = new Date().toISOString().split('T')[0];
    this.idVenueSesion = this.authService.getIdVenue();
    this.roleUsuario   = this.authService.getRole();
    this.esGerGeneral  = this.idVenueSesion === null;

    this.cargarPeliculas();

    if (this.esGerGeneral) {
      this.cargarSedes();
    } else {
      this.cargarSalas(this.idVenueSesion!);
    }
  }


  cargarPeliculas(): void {
    this.movieService.getMoviesByStatus('Cartelera').subscribe(res => {
      this.movies = res;
      this.cdr.detectChanges();
    });
  }

  cargarSedes(): void {
    this.venueService.getVenues().subscribe(res => {
      this.sedes = res;
      this.cdr.detectChanges();
    });
  }

  cargarSalas(idVenue: number): void {
    this.roomService.getRoomsByVenue(idVenue).subscribe(res => {
      this.rooms = (res as any[]).filter((r: any) => r.status === 'Activo');
      this.cdr.detectChanges();
      this.cargarFunciones();
    });
  }

  // Gerente General selecciona sede en el selector
  onSedeChange(): void {
    this.rooms = [];
    this.showtimes = [];
    this.filtroSala = null;
    if (this.sedeSeleccionadaId) {
      this.cargarSalas(this.sedeSeleccionadaId);
    }
  }

  // Devuelve el idVenue efectivo (según rol)
  get idVenueEfectivo(): number | null {
    return this.esGerGeneral ? this.sedeSeleccionadaId : this.idVenueSesion;
  }


  cargarFunciones(): void {
    if (!this.idVenueEfectivo || !this.filtroFecha) return;
    this.cargando = true;
    this.showtimeService.getShowtimesByVenue(this.idVenueEfectivo, this.filtroFecha).subscribe({
      next: res => {
        this.showtimes = res;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.cargando = false;
        this.mostrarError(this.extraerMensaje(err, 'Error al cargar las funciones.'));
      }
    });
  }

  get funcionesFiltradas(): any[] {
    return this.showtimes.filter(f => {
      const porSala    = !this.filtroSala    || f.idRoom === +this.filtroSala;
      const porFormato = !this.filtroFormato || f.languageFormat === this.filtroFormato;
      return porSala && porFormato;
    });
  }

  get formatos(): string[] {
    return Array.from(new Set(this.showtimes.map(f => f.languageFormat)));
  }


  abrirModalNuevo(): void {
    this.isEditMode = false;
    this.currentShowtimeId = null;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.currentShowtime = this.emptyShowtime();
    this.horaFinEstimada = '';
    this.cdr.detectChanges();
  }

  abrirModalEditar(f: any): void {
    this.isEditMode = true;
    this.currentShowtimeId = f.idShowtime;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.currentShowtime = {
      idMovie: f.idMovie,
      idRoom: f.idRoom,
      showDate: f.showDate,
      startTime: f.startTime,
      languageFormat: f.languageFormat,
      baseTicketPrice: f.baseTicketPrice
    };
    this.calcularHoraFin();
    this.cdr.detectChanges();
  }

  calcularHoraFin(): void {
    const movie = this.movies.find(m => m.idMovie === +this.currentShowtime.idMovie);
    if (!movie || !this.currentShowtime.startTime) { this.horaFinEstimada = ''; return; }
    const [h, m] = this.currentShowtime.startTime.split(':').map(Number);
    const totalMin = h * 60 + m + movie.durationMinutes + 30;
    const hFin = Math.floor(totalMin / 60) % 24;
    const mFin = totalMin % 60;
    this.horaFinEstimada = `${String(hFin).padStart(2, '0')}:${String(mFin).padStart(2, '0')}`;
  }

  guardarFuncion(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.currentShowtime.idMovie)        return this.mostrarError('Selecciona una película.');
    if (!this.currentShowtime.idRoom)         return this.mostrarError('Selecciona una sala.');
    if (!this.currentShowtime.showDate)       return this.mostrarError('Selecciona una fecha.');
    if (!this.currentShowtime.startTime)      return this.mostrarError('Selecciona una hora de inicio.');
    if (!this.currentShowtime.languageFormat) return this.mostrarError('Selecciona el formato.');
    if (!this.currentShowtime.baseTicketPrice || this.currentShowtime.baseTicketPrice <= 0)
      return this.mostrarError('El precio debe ser mayor a 0.');

    this.guardando = true;
    const payload = { ...this.currentShowtime };
    payload.idMovie = +payload.idMovie;
    payload.idRoom  = +payload.idRoom;
    payload.baseTicketPrice = +payload.baseTicketPrice;

    const op = this.isEditMode
      ? this.showtimeService.updateShowtime(this.currentShowtimeId!, payload)
      : this.showtimeService.createShowtime(payload);

    op.subscribe({
      next: () => {
        this.guardando = false;
        this.mensajeExito = this.isEditMode ? 'Función actualizada.' : 'Función programada correctamente.';
        this.cargarFunciones();
        setTimeout(() => this.cerrarModal(), 1500);
      },
      error: err => {
        this.guardando = false;
        this.mostrarError(this.extraerMensaje(err, 'Error al guardar la función.'));
      }
    });
  }

  cancelarFuncion(f: any): void {
    if (!confirm(`¿Cancelar la función de "${f.titleMovie}" el ${f.showDate} a las ${f.startTime}?`)) return;
    this.showtimeService.cancelShowtime(f.idShowtime).subscribe({
      next: () => { this.mensajeExito = 'Función cancelada.'; this.cargarFunciones(); },
      error: err => this.mostrarError(this.extraerMensaje(err, 'Error al cancelar.'))
    });
  }

  cerrarModal(): void {
    const btn = document.getElementById('btnCerrarModal');
    if (btn) btn.click();
  }

  extraerMensaje(err: any, fallback: string): string {
    if (!err) return fallback;
    const body = err.error;
    if (typeof body === 'string' && body.trim().length > 0) return body;
    if (typeof body === 'object' && body !== null) {
      return body.message || body.error || JSON.stringify(body);
    }
    return err.message || fallback;
  }

  mostrarError(msg: string): void {
    this.mensajeError = msg;
    this.cdr.detectChanges();
  }

  emptyShowtime(): any {
    return {
      idMovie: null, idRoom: null,
      showDate: this.filtroFecha || new Date().toISOString().split('T')[0],
      startTime: '', languageFormat: 'Doblada 2D', baseTicketPrice: 15.00
    };
  }

  getBadgeClase(status: string): string {
    switch (status) {
      case 'Programada': return 'badge bg-success';
      case 'En Curso':   return 'badge bg-warning text-dark';
      case 'Finalizada': return 'badge bg-secondary';
      case 'Cancelada':  return 'badge bg-danger';
      default:           return 'badge bg-light text-dark';
    }
  }
}