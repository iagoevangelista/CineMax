import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShowtimeService } from '../../../services/showtime.service';
import { MovieService } from '../../../services/movie.service';
import { RoomService } from '../../../services/room.service';

@Component({
  selector: 'app-admin-showtimes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './showtimes.html',
  styleUrls: ['./showtimes.css']
})
export class AdminShowtimes implements OnInit {
  movies: any[] = [];
  rooms: any[] = [];
  showtimes: any[] = [];
  
  cargando: boolean = false;
  filtroPeliculaId: number | null = null;
  filtroFecha: string = '';

  isEditMode: boolean = false;
  currentShowtimeId: number | null = null;
  currentShowtime: any = {
    idMovie: null, idRoom: null, showDate: '', startTime: '', 
    languageFormat: 'Doblada 2D', baseTicketPrice: 15.00
  };

  constructor(
    private showtimeService: ShowtimeService,
    private movieService: MovieService,
    private roomService: RoomService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.filtroFecha = new Date().toISOString().split('T')[0];
    this.cargarDataReal();
  }

  cargarDataReal() {
    // Obtenemos la data real del usuario desde localStorage
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const idSede = user.idVenue; // <--- AJUSTA ESTE CAMPO SI EN TU LOCALSTORAGE SE LLAMA DISTINTO

    this.roomService.getRoomsByVenue(idSede).subscribe((res: any) => this.rooms = res);
    
    this.movieService.getMoviesByStatus('Cartelera').subscribe((res: any) => {
      this.movies = res;
      if (this.movies.length > 0) {
        this.filtroPeliculaId = this.movies[0].idMovie;
        this.buscarFunciones();
      }
    });
  }

  buscarFunciones() {
    if (!this.filtroPeliculaId) return;
    this.cargando = true;
    this.showtimeService.getShowtimesByMovie(this.filtroPeliculaId).subscribe((res: any) => {
      this.showtimes = res.filter((f: any) => f.showDate === this.filtroFecha);
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  abrirModalNuevo() {
    this.isEditMode = false;
    this.currentShowtime = {
      idMovie: this.filtroPeliculaId,
      idRoom: this.rooms.length > 0 ? this.rooms[0].idRoom : null,
      showDate: this.filtroFecha,
      startTime: '',
      languageFormat: 'Doblada 2D',
      baseTicketPrice: 15.00
    };
  }

  abrirModalEditar(f: any) {
    this.isEditMode = true;
    this.currentShowtimeId = f.idShowtime;
    this.currentShowtime = { ...f };
  }

  guardarFuncion() {
    const op = this.isEditMode 
      ? this.showtimeService.updateShowtime(this.currentShowtimeId!, this.currentShowtime)
      : this.showtimeService.createShowtime(this.currentShowtime);

    op.subscribe({
      next: () => { alert("Guardado"); this.buscarFunciones(); },
      error: (err) => alert("Error: " + err.error)
    });
  }

  cancelarFuncion(id: number) {
    if (confirm('¿Cancelar función?')) {
      this.showtimeService.cancelShowtime(id).subscribe(() => this.buscarFunciones());
    }
  }
}