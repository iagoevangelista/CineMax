import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BookingService, SnackEntry } from '../../../services/booking';

interface SnackItem {
  idSnack: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  idSnackCategory: number;
  cantidad: number;
}

interface Category {
  idSnackCategory: number;
  name: string;
}

@Component({
  selector: 'app-snacks',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './snacks.html',
  styleUrl: './snacks.css'
})
export class Snacks implements OnInit {

  resumen: any;
  categorias: Category[] = [];
  snacks: SnackItem[] = [];
  categoriaActiva: number | null = null;

  cargando = true;
  error = '';

  private readonly apiUrl = 'http://localhost:8080/api/v1';

  constructor(
    private router: Router,
    private bookingService: BookingService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.resumen = this.bookingService.obtenerResumen();

    // Redirigir si no hay tickets seleccionados
    if (!this.resumen.tickets?.length) {
      this.router.navigate(['/tickets']);
      return;
    }

    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;

    // Cargar categorías
    this.http.get<Category[]>(`${this.apiUrl}/snack-categories`).subscribe({
      next: (cats) => {
        this.categorias = cats;
        if (cats.length) this.categoriaActiva = cats[0].idSnackCategory;
        this.cdr.detectChanges();
      },
      error: () => { /* categorías opcionales */ }
    });

    // Cargar snacks
    this.http.get<any[]>(`${this.apiUrl}/snacks`).subscribe({
      next: (res) => {
        const activos = res.filter(s => s.status === 'Activo');

        this.snacks = activos.map(s => ({
          idSnack:          s.idSnack,
          name:             s.name,
          description:      s.description,
          price:            Number(s.price),
          imageUrl:         s.imageUrl || '',
          idSnackCategory:  s.idSnackCategory,
          cantidad:         0
        }));

        // Restaurar snacks previos si el usuario regresó
        const snacksPrevios = this.resumen.snacks as SnackEntry[];
        if (snacksPrevios?.length) {
          snacksPrevios.forEach(prev => {
            const item = this.snacks.find(s => s.idSnack === prev.idSnack);
            if (item) item.cantidad = prev.cantidad;
          });
        }

        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'No se pudo cargar la confitería. Puedes continuar sin snacks.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  get snacksFiltrados(): SnackItem[] {
    if (this.categoriaActiva === null) return this.snacks;
    return this.snacks.filter(s => s.idSnackCategory === this.categoriaActiva);
  }

  get carrito(): SnackItem[] {
    return this.snacks.filter(s => s.cantidad > 0);
  }

  get totalSnacks(): number {
    return this.snacks.reduce((t, s) => t + s.price * s.cantidad, 0);
  }

  get totalItems(): number {
    return this.snacks.reduce((t, s) => t + s.cantidad, 0);
  }

  get totalTickets(): number {
    return this.bookingService.calcularTotalTickets();
  }

  get totalGeneral(): number {
    return this.totalTickets + this.totalSnacks;
  }

  sumar(snack: SnackItem): void {
    snack.cantidad++;
  }

  restar(snack: SnackItem): void {
    if (snack.cantidad > 0) snack.cantidad--;
  }

  setCategoriaActiva(id: number): void {
    this.categoriaActiva = id;
  }

  continuarAlPago(): void {
    const seleccionados: SnackEntry[] = this.carrito.map(s => ({
      idSnack:  s.idSnack,
      nombre:   s.name,
      precio:   s.price,
      cantidad: s.cantidad,
      subtotal: s.price * s.cantidad,
      imagen:   s.imageUrl
    }));

    this.bookingService.guardarSnacks(seleccionados);
    this.router.navigate(['/payment']);
  }

  saltarSnacks(): void {
    this.bookingService.guardarSnacks([]);
    this.router.navigate(['/payment']);
  }

  regresar(): void {
    this.router.navigate(['/tickets']);
  }
}