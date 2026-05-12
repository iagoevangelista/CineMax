import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/v1/users'; 

  constructor(private http: HttpClient) { }

  // 1. Método para listar usuarios (que ya tenías)
  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // 2. Método para actualizar el rol (que ya tenías)
  updateUserRole(idUser: number, idRole: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${idUser}/role`, { idRole: idRole });
  }

  // 3. <--- ¡ESTE ES EL QUE FALTA! Método para crear el usuario --->
  createUser(userData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, userData);
  }
}