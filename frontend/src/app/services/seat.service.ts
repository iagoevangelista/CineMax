import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SeatService {
  private apiUrl = 'http://localhost:8080/api/v1/seats';

  constructor(private http: HttpClient) {}

  // Llama a: GET /api/v1/seats?idShowtime={id}
  getSeatsStatusByShowtime(idShowtime: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?idShowtime=${idShowtime}`);
  }
}