import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { VenueService } from '../../../services/venue.service';
import { LocationService } from '../../../services/location.service';

export interface Venue {
  idVenue?: number;
  nameVenue: string;
  addressVenue: string;
  phoneNumber: string;
  status: string;
  departmentName?: string;
  provinceName?: string;
  districtName?: string;
}

@Component({
  selector: 'app-venues',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './venues.html',
  styleUrl: './venues.css'
})
export class Venues implements OnInit {
  
  listaSedes: Venue[] = [];

  // <-- NUEVO: Listas para llenar los combos
  listaDepartamentos: any[] = [];
  listaProvincias: any[] = [];
  listaDistritos: any[] = [];

  // Inicializamos en 0 para que por defecto diga "Seleccione..."
  nuevaSede = {
    nameVenue: '',
    addressVenue: '',
    phoneNumber: '',
    status: 'Activo',
    idDepartment: 0, 
    idProvince: 0,   
    idDistrict: 0    
  };

  // Inyectamos el LocationService
  constructor(private venueService: VenueService, private locationService: LocationService) {}

  ngOnInit() {
    this.cargarSedes();
    this.cargarDepartamentos(); // <-- NUEVO: Al cargar la pantalla, traemos los departamentos
  }

  cargarSedes() {
    this.venueService.getVenues().subscribe((datos) => {
      this.listaSedes = datos;
    });
  }

  // <-- NUEVO: Método para traer departamentos
  cargarDepartamentos() {
    this.locationService.getDepartments().subscribe(datos => {
      this.listaDepartamentos = datos;
    });
  }

  // <-- NUEVO: Se ejecuta cuando el admin elige un Departamento
  onDepartamentoChange() {
    // 1. Limpiamos provincia y distrito porque el departamento cambió
    this.nuevaSede.idProvince = 0;
    this.nuevaSede.idDistrict = 0;
    this.listaProvincias = [];
    this.listaDistritos = [];

    // 2. Si eligió un departamento válido, buscamos sus provincias
    if (this.nuevaSede.idDepartment > 0) {
      this.locationService.getProvinces(this.nuevaSede.idDepartment).subscribe(datos => {
        this.listaProvincias = datos;
      });
    }
  }

  // <-- NUEVO: Se ejecuta cuando el admin elige una Provincia
  onProvinciaChange() {
    // 1. Limpiamos el distrito porque la provincia cambió
    this.nuevaSede.idDistrict = 0;
    this.listaDistritos = [];

    // 2. Si eligió una provincia válida, buscamos sus distritos
    if (this.nuevaSede.idProvince > 0) {
      this.locationService.getDistricts(this.nuevaSede.idProvince).subscribe(datos => {
        this.listaDistritos = datos;
      });
    }
  }

  guardarSede() {
    // Validamos que haya seleccionado hasta el distrito
    if (this.nuevaSede.idDistrict === 0) {
      alert("Debes seleccionar un distrito válido.");
      return;
    }

    this.venueService.createVenue(this.nuevaSede).subscribe({
    next: (sedeGuardada) => {
      // 1. Mensaje de éxito
      alert('¡Sede creada con éxito!');

      // 2. Cerramos el modal usando el ID del botón de cerrar o el fondo
      document.getElementById('modalNuevaSede')?.click();
      
      // 3. ¡ESTO ES LO MÁS IMPORTANTE! 
      // Volvemos a pedir la lista al servidor para que la tabla se actualice sola
      this.cargarSedes(); 

      // 4. Limpiamos el formulario para la siguiente vez
      this.nuevaSede = { 
        nameVenue: '', addressVenue: '', phoneNumber: '', 
        status: 'Activo', idDepartment: 0, idProvince: 0, idDistrict: 0 
      };
    },
    error: (err) => alert('Error al guardar')
  });
  }
}
