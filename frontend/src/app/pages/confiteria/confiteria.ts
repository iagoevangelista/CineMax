import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfiteriaService } from '../../services/confiteria.service';

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
  error = false;
  categoriaActiva: any = null;
  carrito: { snack: any; cantidad: number }[] = [];

  constructor(private snackService: ConfiteriaService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.cargarDatos();

    setTimeout(() => {
      if (this.cargando) {
        this.cargando = false;
        this.error = true;
        this.cdr.detectChanges();
      }
    }, 5000);
  }

  cargarDatos() {
    this.snackService.cargarCategorias().subscribe({
      next: (cats: any[]) => {
        this.categories = cats;
        if (cats.length) this.categoriaActiva = cats[0];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.error = true;
        this.cdr.detectChanges();
      }
    });

    this.snackService.cargarSnacks().subscribe({
      next: (res: any[]) => {
        this.snacks = res.filter((s: any) => s.status === 'Activo');
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.error = true;
        this.cdr.detectChanges();
      }
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