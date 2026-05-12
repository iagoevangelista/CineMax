import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css'
})
export class MovieDetail implements OnInit {
  movie: any = null;
  loading = true;
  error = false;

  
  // Datos estáticos por ahora (luego conectamos al backend)
  movies: any[] = [
    {
      idMovie: 1,
      titleMovie: 'Super Mario Bros. La Película',
      posterUrl: 'https://cdn.apis.cineplanet.com.pe/CDN/media/entity/get/FilmPosterGraphic/HO00002775?referenceScheme=HeadOffice&allowPlaceHolder=true',
      synopsis: 'Mario y Luigi son dos fontaneros de Brooklyn transportados a un mundo mágico donde deberán enfrentarse a Bowser para salvar el reino.',
      director: 'Aaron Horvath, Michael Jelenic',
      duration_minutes: 92,
      status: 'Estreno',
      rating: 'ATP'
    },
    {
      idMovie: 2,
      titleMovie: 'La Posesión de la Momia',
      posterUrl: 'https://cdn.apis.cineplanet.com.pe/CDN/media/entity/get/FilmPosterGraphic/HO00002801?referenceScheme=HeadOffice&allowPlaceHolder=true',
      synopsis: 'Un grupo de arqueólogos desencadena una antigua maldición al profanar la tumba de una momia milenaria.',
      director: 'Desconocido',
      duration_minutes: 105,
      status: 'Estreno',
      rating: '+17'
    },
    {
      idMovie: 3,
      titleMovie: 'Proyecto Fin del Mundo',
      posterUrl: 'https://cdn.apis.cineplanet.com.pe/CDN/media/entity/get/FilmPosterGraphic/HO00002680?referenceScheme=HeadOffice&allowPlaceHolder=true',
      synopsis: 'Un ex agente especial debe salvar al mundo de una amenaza catastrófica en una carrera contra el tiempo.',
      director: 'Desconocido',
      duration_minutes: 118,
      status: 'Estreno',
      rating: 'ATP'
    },
    {
      idMovie: 4,
      titleMovie: 'Boulevard',
      posterUrl: 'https://cdn.apis.cineplanet.com.pe/CDN/media/entity/get/FilmPosterGraphic/HO00002837?referenceScheme=HeadOffice&allowPlaceHolder=true',
      synopsis: 'Una historia de amor y redención en las calles de una ciudad moderna.',
      director: 'Desconocido',
      duration_minutes: 98,
      status: 'Estreno',
      rating: '+14'
    }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}


  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.movie = this.movies.find(m => m.idMovie === id) || null;
    this.loading = false;
    if (!this.movie) this.error = true;
  }

  volver() {
    this.router.navigate(['/']);
  }


  empezarCompra() {
    if (this.movie) {
      this.bookingService.guardarPelicula(this.movie); 
      this.router.navigate(['/seats']);
    }
  }
}