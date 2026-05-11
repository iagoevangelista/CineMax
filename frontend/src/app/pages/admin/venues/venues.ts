import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Vital para poder usar *ngFor y pintar tablas

// 1. Nuestro "Contrato" temporal de cómo es una Sede
// Tu nuevo "Contrato" en Angular debe ser idéntico al DTO de Java
export interface Venue {
  idVenue: number;         // Antes era solo id
  nameVenue: string;       // Antes era nombre
  addressVenue: string;    // Antes era direccion
  status: string;          // Antes era estado
  departmentName?: string; // Le ponemos "?" porque es opcional mostrarlo
  provinceName?: string;
  districtName?: string;
}

@Component({
  selector: 'app-venues',
  standalone: true,
  imports: [CommonModule], 
  templateUrl: './venues.html',
  styleUrl: './venues.css'
})

export class Venues {
  // 2. Nuestra "Base de datos falsa" temporal (Mocking)
  listaSedes: Venue[] = [
    { idVenue: 1, nameVenue: 'CineMax San Miguel', addressVenue: 'Av. La Marina 2000', status: 'Activo' },
    { idVenue: 2, nameVenue: 'CineMax Surco', addressVenue: 'Jockey Plaza', status: 'Inactivo' },
    { idVenue: 3, nameVenue: 'CineMax MegaPlaza', addressVenue: 'Independencia', status: 'Activo' }
  ];
}