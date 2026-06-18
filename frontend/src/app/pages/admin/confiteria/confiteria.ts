import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfiteriaService } from '../../../services/confiteria.service';

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

  constructor(
    private confiteriaService: ConfiteriaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarSnacks();
  }

  cargarSnacks() {
    this.cargando = true;
    this.confiteriaService.cargarSnacks().subscribe({
      next: (res: any) => { this.snacks = res; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.cargando = false; }
    });
  }

  cargarCategorias() {
    this.confiteriaService.cargarCategorias().subscribe({
      next: (res: any) => { this.categories = res; this.cdr.detectChanges(); },
      error: (err: any) => console.error('Error categorías:', err)
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
    if (!this.currentSnack.nameSnack || this.currentSnack.nameSnack.trim() === '') {
      alert('El nombre del producto es obligatorio');
      return;
    }
    if (!this.currentSnack.price || Number(this.currentSnack.price) <= 0) {
      alert('El precio debe ser mayor a S/. 0.00');
      return;
    }
    
    const formData = new FormData();
    const snackData = JSON.stringify({
      nameSnack: this.currentSnack.nameSnack,
      descriptionSnack: this.currentSnack.descriptionSnack,
      price: Number(this.currentSnack.price),
      stock: Number(this.currentSnack.stock),
      status: this.currentSnack.status || 'Activo',
      idSnackCategory: Number(this.currentSnack.idSnackCategory)
    });
    formData.append('snack', snackData);
    if (this.selectedFile) formData.append('file', this.selectedFile);

    if (this.isEditMode && this.currentSnackId) {
      this.confiteriaService.actualizarSnack(this.currentSnackId, formData).subscribe({
        next: () => { this.cargarSnacks(); alert('Snack actualizado'); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    } else {
      this.confiteriaService.crearSnack(formData).subscribe({
        next: () => { this.cargarSnacks(); alert('Snack creado'); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

  inhabilitarSnack(id: number) {
    if (confirm('¿Inhabilitar este producto?')) {
      this.confiteriaService.inhabilitarSnack(id).subscribe({
        next: () => { this.cargarSnacks(); alert('Producto inhabilitado'); },
        error: (err: any) => alert('Error: ' + err.message)
      });
    }
  }

  habilitarSnack(id: number) {
    const snack = this.snacks.find(s => s.idSnack === id);
    if (!snack) return;
    const formData = new FormData();
    const snackData = JSON.stringify({
      nameSnack: snack.nameSnack,
      descriptionSnack: snack.descriptionSnack,
      price: Number(snack.price),
      stock: Number(snack.stock),
      status: 'Activo',
      idSnackCategory: Number(snack.idSnackCategory)
    });
    formData.append('snack', snackData);

    this.confiteriaService.actualizarSnack(id, formData).subscribe({
      next: () => { this.cargarSnacks(); alert('Producto habilitado'); },
      error: (err: any) => alert('Error: ' + err.message)
    });
  }
}