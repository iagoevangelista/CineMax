import { jwtDecode } from 'jwt-decode';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

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

register(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data).pipe(
      tap((response: any) => {
        // Atrapamos el token que el backend ya nos envía al registrar
        if (response.token) {
          localStorage.setItem('cinemax_token', response.token);
        }
      })
    );
  }
// Solicitar el correo de recuperación
  forgotPassword(email: string) {
    // Ajusta la URL base si es diferente en tu entorno
    return this.http.post('http://localhost:8080/api/v1/auth/forgot-password', { email });
  }

  // Enviar la nueva contraseña con el token
  resetPassword(token: string, newPassword: string) {
    return this.http.post('http://localhost:8080/api/v1/auth/reset-password', { token, newPassword });
  }

  getEmail(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        // Spring Security guarda el correo (username) en la variable 'sub' por defecto
        return decoded.sub || decoded.email || null;
      } catch (e) {
        return null;
      }
    }
    return null;
  }

}
