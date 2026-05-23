import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private apiUrl = 'http://localhost:8080/api/v1/rooms'; 

  constructor(private http: HttpClient) {}

  getAllRooms(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getRoomsByVenue(idVenue: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/venue/${idVenue}`);
  }

  createRoom(room: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, room);
  }

  updateRoom(idRoom: number, room: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${idRoom}`, room);
  }
}