import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = `${environment.apiUrl}/locations`;

  constructor(private http: HttpClient) {}

  getDepartments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/departments`);
  }

  getProvinces(idDepartment: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/provinces/${idDepartment}`);
  }

  getDistricts(idProvince: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/districts/${idProvince}`);
  }
}
