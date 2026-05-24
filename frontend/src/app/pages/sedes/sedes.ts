import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // <-- IMPORTANTE: Añadir para el buscador (ngModel)
import { RouterModule, Router } from '@angular/router';
import { VenueService } from '../../services/venue.service';
import * as L from 'leaflet';

@Component({
  selector: 'app-sedes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // <-- NO OLVIDES AÑADIR FormsModule AQUÍ
  templateUrl: './sedes.html',
  styleUrl: './sedes.css'
})
export class Sedes implements OnInit {
  sedes: any[] = [];
  departamentos: string[] = [];
  departamentoSeleccionado: string = '';
  
  textoBusqueda: string = ''; // Nueva variable para el buscador
  sedesMostradas: any[] = []; // El arreglo final que se pinta en el HTML
  
  // Variables para el Modal del Mapa
  sedeDetalle: any = null;
  map: L.Map | undefined;
  marker: L.Marker | undefined;

  constructor(
    private venueService: VenueService, 
    private router: Router, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.venueService.getPublicVenues().subscribe(data => {
      this.sedes = data;
      this.departamentos = [...new Set(this.sedes.map(s => s.departmentName))].filter(d => d);
      
      // Auto-seleccionar el primer departamento apenas cargan los datos
      if (this.departamentos.length > 0) {
        this.departamentoSeleccionado = this.departamentos[0];
        this.aplicarFiltros(); 
      }
      
      // ¡ESTO ARREGLA EL ERROR DE QUE NO SALGA A LA PRIMERA!
      this.cdr.detectChanges(); 
    });
  }

  seleccionarDepartamento(dep: string) {
    this.departamentoSeleccionado = dep;
    this.textoBusqueda = ''; // Limpiamos el buscador al cambiar de ciudad
    this.aplicarFiltros();
  }

  // Nueva función inteligente que filtra por ciudad y por el texto del buscador
  aplicarFiltros() {
    this.sedesMostradas = this.sedes.filter(s => {
      const coincideDep = s.departmentName === this.departamentoSeleccionado;
      const coincideTexto = s.nameVenue.toLowerCase().includes(this.textoBusqueda.toLowerCase()) || 
                            s.districtName?.toLowerCase().includes(this.textoBusqueda.toLowerCase());
      
      return coincideDep && coincideTexto;
    });
  }

  irACartelera(sedeId: number) {
    this.router.navigate(['/movies'], { queryParams: { sede: sedeId } });
  }

  verDetalles(sede: any) {
    this.sedeDetalle = sede;
    setTimeout(() => this.initMap(sede), 300); 
  }

  cerrarDetalles() {
    this.sedeDetalle = null;
  }

  initMap(sede: any) {
    if (this.map) this.map.remove();
    
    const lat = sede.latitude || -12.046374;
    const lng = sede.longitude || -77.042793;

    this.map = L.map('mapa-cliente').setView([lat, lng], 16);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(this.map);

    const customIcon = L.icon({
      iconUrl: 'https://cdn-icons-png.flaticon.com/512/2776/2776067.png',
      iconSize: [40, 40],
      iconAnchor: [20, 40]
    });

    this.marker = L.marker([lat, lng], { icon: customIcon }).addTo(this.map);
    this.map.invalidateSize();
  }
}