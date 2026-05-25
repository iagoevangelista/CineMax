import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Movie {
  idMovie: number;
  titleMovie: string;
  synopsis: string;
  durationMinutes: number;
  posterUrl: string;
  releaseDate: string;
  director: string;
  status: string;
  isActive?: boolean;
  premiereWeek: boolean;
  rating?: string;
  classificationName?: string;
  genreNames?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class MovieService {
  private apiUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  getMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.apiUrl}/movies?status=Cartelera`);
  }

  getMovieById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.apiUrl}/movies/${id}`);
  }

  getMoviesByStatus(status: string): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.apiUrl}/movies?status=${status}`);
  }

  createMovie(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/movies`, formData);
  }

  updateMovie(id: number, formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/movies/${id}`, formData);
  }

  deleteMovie(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/movies/${id}`);
  }
  
}