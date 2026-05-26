import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ShowtimeService {
  private apiUrl = 'http://localhost:8080/api/v1/showtimes';

  constructor(private http: HttpClient) {}

  // Para la cartelera pública
  getShowtimesByMovie(idMovie: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?idMovie=${idMovie}`);
  }

  // Para el panel de gestión — funciones de UNA sede en una fecha
  getShowtimesByVenue(idVenue: number, date: string): Observable<any[]> {
    const params = new HttpParams().set('idVenue', idVenue).set('date', date);
    return this.http.get<any[]>(`${this.apiUrl}/by-venue`, { params });
  }

  getShowtimeSummary(idShowtime: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${idShowtime}/summary`);
  }

  getTicketFares(idShowtime: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idShowtime}/fares`);
  }

  createShowtime(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }

  updateShowtime(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }

  cancelShowtime(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}