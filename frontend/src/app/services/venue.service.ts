import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Venue } from '../pages/admin/venues/venues'; // Ajusta la ruta a tu interfaz

@Injectable({
  providedIn: 'root'
})
export class VenueService {

  // La URL de tu backend en Spring Boot
  private apiUrl = 'http://localhost:8080/api/v1/venues';

  constructor(private http: HttpClient) { }

  // 1. Método para traer la lista (Reemplaza los datos falsos)
  getVenues(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.apiUrl);
  }

  // 2. Método para CREAR una nueva sede
  createVenue(venueData: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, venueData);
  }

  // NUEVO: Método para traer sedes filtradas según el rol
  getAvailableVenuesForRole(roleId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/available-for-role/${roleId}`);
  }

  // ... dentro de VenueService
  updateVenue(id: number, venueData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, venueData);
  }

  deleteVenue(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}