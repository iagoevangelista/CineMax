import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ShowtimeService {
    private apiUrl = 'http://localhost:8080/api/v1/showtimes';

    constructor(private http: HttpClient) {}

  // Llama a: GET /api/v1/showtimes/{id}/summary
    getShowtimeSummary(idShowtime: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/${idShowtime}/summary`);
    }

    getShowtimesByMovie(idMovie: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}?idMovie=${idMovie}`);
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