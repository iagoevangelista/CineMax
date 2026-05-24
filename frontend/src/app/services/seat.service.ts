import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SeatService {
  
  // URL directa a tu controlador de Asientos
  private apiUrl = 'http://localhost:8080/api/v1/seats';

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