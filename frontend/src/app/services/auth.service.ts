import { jwtDecode } from 'jwt-decode';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../enviroments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
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
      let role = decoded.role || 'USUARIO';
      if (role.startsWith('ROLE_')) {
        role = role.replace('ROLE_', '');
      }
      return role;
    }
    return 'INVITADO';
  }

  // Devuelve la lista de permisos del usuario logueado, leídos directamente del claim "permissions" del JWT.
  getPermissions(): string[] {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return decoded.permissions || [];
      } catch (e) {
        return [];
      }
    }
    return [];
  }

  hasPermission(permission: string): boolean {
    return this.getPermissions().includes(permission);
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data).pipe(
      tap((response: any) => {
        if (response.token) {
          localStorage.setItem('cinemax_token', response.token);
        }
      })
    );
  }

  forgotPassword(email: string) {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string) {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  getEmail(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return decoded.sub || decoded.email || null;
      } catch (e) { return null; }
    }
    return null;
  }

  getIdVenue(): number | null {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return decoded.idVenue ?? null;
      } catch (e) { return null; }
    }
    return null;
  }

  getFirstName(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return decoded.firstName ?? null;
      } catch (e) { return null; }
    }
    return null;
  }
}
