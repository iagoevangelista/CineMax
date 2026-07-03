import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GenreService {
  private apiUrl = `${environment.apiUrl}/genres`;

  constructor(private http: HttpClient) {}

  getAllGenres(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}
