import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Venue } from '../pages/admin/venues/venues';

@Injectable({
  providedIn: 'root'
})
export class VenueService {
  private apiUrl = `${environment.apiUrl}/venues`;

  constructor(private http: HttpClient) {}

  getVenues(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.apiUrl);
  }

  getPublicVenues(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/public`);
  }

  createVenue(venueData: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, venueData);
  }

  getAvailableVenuesForRole(roleId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/available-for-role/${roleId}`);
  }

  updateVenue(id: number, venueData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, venueData);
  }

  deleteVenue(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
