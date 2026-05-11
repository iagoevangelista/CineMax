import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
        // Guardamos el token que nos devuelve AuthResponseDTO
        if (response.token) {
          localStorage.setItem('cinemax_token', response.token);
        }
      })
    );
  }

  getToken() {
    return localStorage.getItem('cinemax_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem('cinemax_token');
  }

  getRole(): string {
  const token = this.getToken();
  if (token) {
    const decoded: any = jwtDecode(token);
    return decoded.role || 'USUARIO'; // 'role' es el nombre que pusimos en el Backend
  }
  return 'INVITADO';
}

}