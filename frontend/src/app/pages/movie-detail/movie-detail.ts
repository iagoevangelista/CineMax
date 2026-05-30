import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingService } from '../../services/booking';
import { MovieService } from '../../services/movie.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css'
})
export class MovieDetail implements OnInit {
  movie: any = null;
  loading = true;
  error = false;
  idShowtime = 8;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private movieService: MovieService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.movie = null;
      this.loading = true;
      this.error = false;
      const id = Number(params.get('id'));
      this.movieService.getMovieById(id).subscribe({
        next: (data) => {
          this.movie = data;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.error = true;
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    });
  }
    volver() {
      this.router.navigate(['/movies']);
  }


  /*
  empezarCompra(idShowtime: number) {
    if (!idShowtime) return;
    this.bookingService.iniciarReserva(idShowtime);
    this.router.navigate(['/seats'], { queryParams: { idShowtime } });
  }
    */


  empezarCompra(){
    this.idShowtime = 8;
    const show = this.idShowtime
    this.bookingService.iniciarReserva(this.idShowtime);
    this.router.navigate(['/seats'], { queryParams: { show } })
    
  }
  
}