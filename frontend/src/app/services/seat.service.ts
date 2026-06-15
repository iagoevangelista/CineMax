import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../enviroments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeatService {
  private apiUrl = `${environment.apiUrl}/seats`;

  constructor(private http: HttpClient) {}

  getSeatsStatusByShowtime(idShowtime: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?idShowtime=${idShowtime}`);
  }

  getSeatsByRoom(idRoom: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/room/${idRoom}`);
  }

  updateSeat(idSeat: number, seat: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${idSeat}`, seat);
  }
}
