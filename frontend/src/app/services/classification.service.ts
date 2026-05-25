import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ClassificationService {
    private apiUrl = 'http://localhost:8080/api/v1/classifications';

    constructor(private http: HttpClient) {}

    getAllClassifications(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
    }
}