import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  idDepartment?: number;
  idProvince?: number;
  idDistrict?: number;
  departmentName?: string;
  provinceName?: string;
  districtName?: string;
  imageUrl?: string;
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

  nuevaSede = {
    nameVenue: '',
    addressVenue: '',
    phoneNumber: '',
    status: 'Activo',
    idDepartment: 0, 
    idProvince: 0,   
    idDistrict: 0,
    imageUrl: ''
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
      imageUrl: '' 
    };
    this.listaProvincias = [];
    this.listaDistritos = [];
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
      imageUrl: sede.imageUrl || '' 
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
  }

  guardarSede() {
    if (this.nuevaSede.idDepartment <= 0 || this.nuevaSede.idProvince <= 0 || this.nuevaSede.idDistrict <= 0) {
        alert("Por favor, selecciona Departamento, Provincia y Distrito.");
        return;
    }

    // CREAMOS EL PAQUETE MULTIPART (FormData)
    const formData = new FormData();
    // 1. Metemos el JSON de la sede
    formData.append('venue', new Blob([JSON.stringify(this.nuevaSede)], { type: 'application/json' }));
    // 2. Metemos la imagen si es que seleccionaron una
    if (this.imagenSeleccionada) {
      formData.append('image', this.imagenSeleccionada);
    }

    if (this.esEdicion) {
      this.venueService.updateVenue(this.sedeAEditarId, formData).subscribe({
        next: () => {
          alert('¡Sede actualizada con éxito!');
          this.cerrarModal();
          this.cargarSedes();
        },
        error: (err) => alert('Error al actualizar la sede')
      });
    } else {
      this.venueService.createVenue(formData).subscribe({
          next: () => {
              alert('¡Sede creada con éxito!');
              this.cerrarModal();
              this.cargarSedes();
          },
          error: (err) => alert('Error al guardar la sede')
      });
    }
  }

  toggleEstado(sede: any) {
    const nuevoEstado = sede.status === 'Activo' ? 'Inactivo' : 'Activo';
    
    // 1. Preparamos los datos de la sede con el nuevo estado
    const requestBody = {
      nameVenue: sede.nameVenue,
      addressVenue: sede.addressVenue,
      phoneNumber: sede.phoneNumber,
      status: nuevoEstado,
      idDistrict: sede.idDistrict || 0
    };

    // 2. Empaquetamos en FormData (porque el Backend ahora lo exige)
    const formData = new FormData();
    formData.append('venue', new Blob([JSON.stringify(requestBody)], { type: 'application/json' }));
    
    this.venueService.updateVenue(sede.idVenue, formData).subscribe({
      next: () => {
        this.cargarSedes(); // Refresca la tabla
      },
      error: (err) => alert('Error al cambiar el estado de la sede')
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
        imageUrl: '' 
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

}