import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { VenueService } from '../../../services/venue.service';
import { LocationService } from '../../../services/location.service';
import * as L from 'leaflet'; 

export interface Venue {
  idVenue?: number;
  nameVenue: string;
  addressVenue: string;
  phoneNumber: string;
  status: string;
  idDepartment?: number;
  idProvince?: number;
  idDistrict?: number;
  departmentName?: string;
  provinceName?: string;
  districtName?: string;
  imageUrl?: string;
  latitude?: number;
  longitude?: number;
}

@Component({
  selector: 'app-venues',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './venues.html',
  styleUrl: './venues.css'
})
export class Venues implements OnInit {
  
  esEdicion: boolean = false;
  sedeAEditarId: number = 0;
  listaSedes: Venue[] = [];

  listaDepartamentos: any[] = [];
  listaProvincias: any[] = [];
  listaDistritos: any[] = [];

  cargando: boolean = false;
  textoBusquedaMapa: string = '';

  map: L.Map | undefined;
  marker: L.Marker | undefined;

  nuevaSede = {
    nameVenue: '',
    addressVenue: '',
    phoneNumber: '',
    status: 'Activo',
    idDepartment: 0, 
    idProvince: 0,   
    idDistrict: 0,
    imageUrl: '',
    latitude: -12.046374, 
    longitude: -77.042793
  };

  constructor(
    private venueService: VenueService, 
    private locationService: LocationService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    this.cargarSedes();
    this.cargarDepartamentos();
  }

  cargarSedes() {
    this.cargando = true; 
    this.venueService.getVenues().subscribe({
      next: (datos) => {
        this.listaSedes = datos;
        this.cargando = false; 
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error("Error al traer sedes:", err);
        this.cargando = false; 
        this.cdr.detectChanges(); 
      }
    });
  }

  cargarDepartamentos() {
    this.locationService.getDepartments().subscribe(datos => {
      this.listaDepartamentos = datos;
      this.cdr.detectChanges();
    });
  }

  onDepartamentoChange() {
    this.nuevaSede.idProvince = 0;
    this.nuevaSede.idDistrict = 0;
    this.listaProvincias = [];
    this.listaDistritos = [];

    if (this.nuevaSede.idDepartment > 0) {
      this.locationService.getProvinces(this.nuevaSede.idDepartment).subscribe(datos => {
        this.listaProvincias = datos;
        this.cdr.detectChanges();
      });
    }
  }

  onProvinciaChange() {
    this.nuevaSede.idDistrict = 0;
    this.listaDistritos = [];

    if (this.nuevaSede.idProvince > 0) {
      this.locationService.getDistricts(this.nuevaSede.idProvince).subscribe(datos => {
        this.listaDistritos = datos;
        this.cdr.detectChanges();
      });
    }
  }

  prepararCreacion() {
    this.esEdicion = false;
    this.sedeAEditarId = 0;
    this.nuevaSede = {
      nameVenue: '',
      addressVenue: '',
      phoneNumber: '',
      status: 'Activo',
      idDepartment: 0, 
      idProvince: 0,   
      idDistrict: 0,
      imageUrl: '',
      latitude: -12.046374, 
      longitude: -77.042793
    };
    this.listaProvincias = [];
    this.listaDistritos = [];
    setTimeout(() => this.initMap(), 400);
  }

  prepararEdicion(sede: any) {
    this.esEdicion = true;
    this.sedeAEditarId = sede.idVenue || 0;
    
    this.nuevaSede = {
      nameVenue: sede.nameVenue,
      addressVenue: sede.addressVenue,
      phoneNumber: sede.phoneNumber,
      status: sede.status,
      idDepartment: sede.idDepartment || 0,
      idProvince: sede.idProvince || 0,
      idDistrict: sede.idDistrict || 0,
      imageUrl: sede.imageUrl || '',
      latitude: sede.latitude || -12.046374,
      longitude: sede.longitude || -77.042793
    };

    if (this.nuevaSede.idDepartment > 0) {
      this.locationService.getProvinces(this.nuevaSede.idDepartment).subscribe(provincias => {
        this.listaProvincias = provincias;
        
        if (this.nuevaSede.idProvince > 0) {
          this.locationService.getDistricts(this.nuevaSede.idProvince).subscribe(distritos => {
            this.listaDistritos = distritos;
            this.cdr.detectChanges(); 
          });
        }
      });
    }
    setTimeout(() => this.initMap(), 400);
  }

  guardarSede() {
    // 1. Validaciones
    if (!this.nuevaSede.nameVenue.trim() || !this.nuevaSede.addressVenue.trim()) {
      alert("El nombre y la dirección del cine son obligatorios.");
      return;
    }

    if (this.nuevaSede.idDepartment <= 0 || this.nuevaSede.idProvince <= 0 || this.nuevaSede.idDistrict <= 0) {
        alert("Por favor, selecciona Departamento, Provincia y Distrito.");
        return;
    }

    // 2. VENTANA DE CONFIRMACIÓN
    const mensajeConfirmacion = this.esEdicion 
      ? '¿Estás seguro de actualizar los datos de esta sede?' 
      : '¿Estás seguro de registrar esta nueva sede en el sistema?';
      
    if (!confirm(mensajeConfirmacion)) {
      return; // Si el usuario cancela, detenemos el proceso
    }

    // 3. Empaquetado
    const formData = new FormData();
    formData.append('venue', new Blob([JSON.stringify(this.nuevaSede)], { type: 'application/json' }));
    if (this.imagenSeleccionada) {
      formData.append('image', this.imagenSeleccionada);
    }

    // 4. Envío al Backend
    if (this.esEdicion) {
      this.venueService.updateVenue(this.sedeAEditarId, formData).subscribe({
        next: () => {
          alert('¡Sede actualizada con éxito!');
          this.cerrarModal();
          this.cargarSedes();
        },
        error: (err) => {
          const msg = err.error?.message || 'Error al actualizar la sede';
          alert(msg);
        }
      });
    } else {
      this.venueService.createVenue(formData).subscribe({
          next: () => {
              alert('¡Sede creada con éxito!');
              this.cerrarModal();
              this.cargarSedes();
          },
          error: (err) => {
              const msg = err.error?.message || 'Error al guardar la sede';
              alert(msg);
          }
      });
    }
  }

  toggleEstado(sede: any) {
    const nuevoEstado = sede.status === 'Activo' ? 'Inactivo' : 'Activo';
    const accionTexto = sede.status === 'Activo' ? 'desactivar (ocultar)' : 'reactivar';
    
    // 1. VENTANA DE CONFIRMACIÓN PARA ELIMINAR/DESACTIVAR
    if (!confirm(`¿Estás completamente seguro de ${accionTexto} la sede "${sede.nameVenue}"?`)) {
      return; // Si cancela, no hacemos nada
    }

    // 2. Preparamos los datos COMPLETOS para que Java no los rechace por el @Valid
    const requestBody = {
      nameVenue: sede.nameVenue,
      addressVenue: sede.addressVenue,
      phoneNumber: sede.phoneNumber,
      status: nuevoEstado,
      
      // ¡SOLUCIÓN AL ERROR! Ahora mandamos las IDs de ubicación obligatorias
      idDepartment: sede.idDepartment || 0,
      idProvince: sede.idProvince || 0,
      idDistrict: sede.idDistrict || 0,
      
      // ¡Y mandamos las coordenadas para no perderlas en el mapa!
      latitude: sede.latitude,
      longitude: sede.longitude
    };

    // 3. Empaquetamos
    const formData = new FormData();
    formData.append('venue', new Blob([JSON.stringify(requestBody)], { type: 'application/json' }));
    
    // 4. Enviamos al backend
    this.venueService.updateVenue(sede.idVenue, formData).subscribe({
      next: () => {
        alert(`¡Sede ${accionTexto}a con éxito!`);
        this.cargarSedes(); // Refresca la tabla
      },
      error: (err) => {
        const msg = err.error?.message || 'Error al cambiar el estado de la sede';
        alert(msg);
      }
    });
  }

  cerrarModal() {
  // 1. Cerramos el modal haciendo click en el botón "Cancelar"
  document.getElementById('btnCerrarModalSede')?.click();

  // 2. Esperamos 400ms (lo que dura la animación de cierre) para limpiar los campos
  setTimeout(() => {
      this.esEdicion = false;
      this.nuevaSede = { 
        nameVenue: '', 
        addressVenue: '', 
        phoneNumber: '', 
        status: 'Activo', 
        idDepartment: 0, 
        idProvince: 0, 
        idDistrict: 0,
        imageUrl: '',
        latitude: -12.046374,
        longitude: -77.042793 
      };
    }, 400);
  }

  // Variables para controlar la imagen
  imagenSeleccionada: File | null = null;
  imagenPrevia: string | null = null;

  // Eventos del Drag & Drop
  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    // Aquí podrías añadir una clase para cambiar el color de la caja al pasar el mouse
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.procesarArchivo(files[0]);
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.procesarArchivo(file);
    }
  }

  procesarArchivo(file: File) {
    // Validamos que sea imagen
    if (file.type.match(/image\/*/) == null) {
      alert("Solo se permiten imágenes (JPG, PNG, etc).");
      return;
    }
    
    this.imagenSeleccionada = file;
    
    // Generamos la vista previa usando FileReader
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (_event) => {
      this.imagenPrevia = reader.result as string;
      this.cdr.detectChanges();
    };
  }

  quitarImagen() {
    this.imagenSeleccionada = null;
    this.imagenPrevia = null;
    this.nuevaSede.imageUrl = ''; 
  }

  initMap() {
    // Si ya existe un mapa anterior, lo destruimos para crear uno limpio
    if (this.map) {
      this.map.remove();
    }

    // Dibujamos el mapa centrado en las coordenadas de la sede
    this.map = L.map('mapa-sede').setView([this.nuevaSede.latitude, this.nuevaSede.longitude], 15);

    // Cargamos el diseño visual del mapa desde OpenStreetMap (¡Es gratis!)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    // Configuramos el ícono del Pin rojo (para evitar bugs visuales de Angular)
    const customIcon = L.icon({
      iconUrl: 'https://cdn-icons-png.flaticon.com/512/2776/2776067.png', // Un pin bonito
      iconSize: [35, 35],
      iconAnchor: [17, 35]
    });

    // Colocamos el pin inicial
    this.marker = L.marker([this.nuevaSede.latitude, this.nuevaSede.longitude], { icon: customIcon }).addTo(this.map);

    // EVENTO MÁGICO: Cuando el usuario hace clic en el mapa
    this.map.on('click', (e: any) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;
      
      // Actualizamos variables para guardar en la BD
      this.nuevaSede.latitude = lat;
      this.nuevaSede.longitude = lng;

      // Movemos el pin visualmente
      if (this.marker) {
        this.marker.setLatLng([lat, lng]);
      }
    });

    // Arreglo para que el mapa no se vea gris dentro del Modal de Bootstrap
    this.map.invalidateSize();
  }

  buscarUbicacionLibre() {
    if (!this.textoBusquedaMapa || !this.textoBusquedaMapa.trim()) {
      alert('Escribe un lugar de referencia para buscar en el mapa.');
      return;
    }

    // Le agregamos "Perú" automáticamente para que no busque cosas en otros países
    const query = `${this.textoBusquedaMapa}, Perú`;
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;

    fetch(url)
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lon = parseFloat(data[0].lon);

          this.nuevaSede.latitude = lat;
          this.nuevaSede.longitude = lon;

          // Movemos el mapa a la ubicación encontrada (Zoom 15 para ver las calles)
          if (this.map && this.marker) {
            this.map.setView([lat, lon], 15);
            this.marker.setLatLng([lat, lon]);
          }
          this.cdr.detectChanges();
        } else {
          // Si Nominatim no lo encuentra, le damos un buen consejo al gerente
          alert('No se encontró el lugar exacto. Intenta buscar solo por el nombre del distrito o ciudad (Ej: "Cayma, Arequipa") y luego mueve el mapa con el ratón.');
        }
      })
      .catch(err => {
        console.error('Error en búsqueda de mapa:', err);
        alert('Hubo un error de conexión con el mapa.');
      });
  }
  
}