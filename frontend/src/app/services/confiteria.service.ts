import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../enviroments/environment';

@Injectable({
  providedIn: 'root'
})
export class ConfiteriaService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
  }

  cargarCategorias(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/snack-categories`, { headers: this.getHeaders() });
  }

  cargarSnacks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/snacks`, { headers: this.getHeaders() });
  }

  crearSnack(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/snacks`, formData, { headers: this.getHeaders() });
  }

  actualizarSnack(id: number, formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/snacks/${id}`, formData, { headers: this.getHeaders() });
  }

  inhabilitarSnack(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/snacks/${id}`, { headers: this.getHeaders() });
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/snack-categories`);
  }

  getAllSnacks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/snacks`);
  }

  getSnackById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/snacks/${id}`);
  }

  getSnacksByCategory(idCategory: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/snacks/category/${idCategory}`);
  }

  createSnack(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/snacks`, formData);
  }

  updateSnack(id: number, formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/snacks/${id}`, formData);
  }

  deleteSnack(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/snacks/${id}`);
  }
}