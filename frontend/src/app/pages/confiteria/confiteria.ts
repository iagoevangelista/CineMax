import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-confiteria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './confiteria.html',
  styleUrl: './confiteria.css'
})
export class Confiteria implements OnInit {

  snacks: any[] = [];
  categories: any[] = [];
  cargando = true;
  categoriaActiva: any = null;
  carrito: { snack: any; cantidad: number }[] = [];

  private apiUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient, private ngZone: NgZone) {}

  ngOnInit() {
    setTimeout(() => {
      this.cargarDatos();
    }, 100);
  }

  cargarDatos() {
    this.ngZone.run(() => {
      this.http.get<any[]>(`${this.apiUrl}/snack-categories`).subscribe({
        next: (cats) => {
          this.categories = cats;
          if (cats.length) this.categoriaActiva = cats[0];
        }
      });

      this.http.get<any[]>(`${this.apiUrl}/snacks`).subscribe({
        next: (res) => {
          this.snacks = res.filter(s => s.status === 'Activo');
          this.cargando = false;
        },
        error: () => { this.cargando = false; }
      });
    });
  }

  get snacksFiltrados() {
    if (!this.categoriaActiva) return this.snacks;
    return this.snacks.filter(s => s.idSnackCategory === this.categoriaActiva.idSnackCategory);
  }

  agregarAlCarrito(snack: any) {
    const item = this.carrito.find(c => c.snack.idSnack === snack.idSnack);
    if (item) {
      item.cantidad++;
    } else {
      this.carrito.push({ snack, cantidad: 1 });
    }
  }

  quitarDelCarrito(snack: any) {
    const idx = this.carrito.findIndex(c => c.snack.idSnack === snack.idSnack);
    if (idx === -1) return;
    if (this.carrito[idx].cantidad > 1) {
      this.carrito[idx].cantidad--;
    } else {
      this.carrito.splice(idx, 1);
    }
  }

  cantidadEnCarrito(snack: any): number {
    return this.carrito.find(c => c.snack.idSnack === snack.idSnack)?.cantidad ?? 0;
  }

  get totalCarrito(): number {
    return this.carrito.reduce((acc, c) => acc + c.snack.price * c.cantidad, 0);
  }

  get totalItems(): number {
    return this.carrito.reduce((acc, c) => acc + c.cantidad, 0);
  }
}