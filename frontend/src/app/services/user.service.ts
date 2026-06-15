import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../enviroments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  private userSubject = new BehaviorSubject<any>(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {}

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  updateUserRole(idUser: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${idUser}/role`, payload);
  }

  createUser(userData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, userData);
  }

  deleteUser(idUser: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${idUser}`);
  }

  activateUser(idUser: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${idUser}/activate`, {});
  }

  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/profile`).pipe(
      tap(user => this.userSubject.next(user))
    );
  }

  updateProfile(profileData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profile`, profileData);
  }

  updateLocalUser(user: any) {
    this.userSubject.next(user);
  }
}
