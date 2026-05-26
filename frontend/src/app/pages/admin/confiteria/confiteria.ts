import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-confiteria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './confiteria.html',
  styleUrls: ['./confiteria.css']
})
export class Confiteria implements OnInit {

  snacks: any[] = [];
  categories: any[] = [];
  cargando: boolean = true;
  isEditMode: boolean = false;
  currentSnackId: number | null = null;
  selectedFile: File | null = null;

  currentSnack: any = {
    nameSnack: '', descriptionSnack: '', price: null,
    stock: null, status: 'Activo', idSnackCategory: null
  };

  private apiUrl = 'http://localhost:8080/api/v1';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarSnacks();
  }

  getHeaders() {
    return new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
  }

  cargarSnacks() {
    this.cargando = true;
    this.http.get<any[]>(`${this.apiUrl}/snacks`, { headers: this.getHeaders() }).subscribe({
      next: (res) => { this.snacks = res; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.cargando = false; }
    });
  }

  cargarCategorias() {
    this.http.get<any[]>(`${this.apiUrl}/snack-categories`, { headers: this.getHeaders() }).subscribe({
      next: (res) => { this.categories = res; this.cdr.detectChanges(); },
      error: (err) => console.error('Error categorías:', err)
    });
  }

  abrirModalNuevo() {
    this.isEditMode = false;
    this.currentSnackId = null;
    this.selectedFile = null;
    this.currentSnack = { nameSnack: '', descriptionSnack: '', price: null, stock: null, status: 'Activo', idSnackCategory: null };
  }

  abrirModalEditar(snack: any) {
    this.isEditMode = true;
    this.currentSnackId = snack.idSnack;
    this.selectedFile = null;
    this.currentSnack = {
      nameSnack: snack.nameSnack, descriptionSnack: snack.descriptionSnack,
      price: snack.price, stock: snack.stock, status: snack.status,
      idSnackCategory: snack.idSnackCategory
    };
  }

  onFileSelected(event: any) {
    if (event.target.files.length > 0) this.selectedFile = event.target.files[0];
  }

  guardarSnack() {
    const formData = new FormData();
    formData.append('snack', JSON.stringify(this.currentSnack));
    if (this.selectedFile) formData.append('file', this.selectedFile);

    const req = this.isEditMode && this.currentSnackId
      ? this.http.put(`${this.apiUrl}/snacks/${this.currentSnackId}`, formData, { headers: new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` }) })
      : this.http.post(`${this.apiUrl}/snacks`, formData, { headers: new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` }) });

    req.subscribe({
      next: () => { this.cargarSnacks(); alert(this.isEditMode ? 'Snack actualizado' : 'Snack creado'); },
      error: (err) => alert('Error: ' + err.message)
    });
  }

  inhabilitarSnack(id: number) {
    if (confirm('¿Inhabilitar este producto?')) {
      this.http.delete(`${this.apiUrl}/snacks/${id}`, { headers: this.getHeaders() }).subscribe({
        next: () => { this.cargarSnacks(); alert('Producto inhabilitado'); },
        error: (err) => alert('Error: ' + err.message)
      });
    }
  }
}