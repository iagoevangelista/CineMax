import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RoomService } from '../../../services/room.service';
import { VenueService } from '../../../services/venue.service';
import { AuthService } from '../../../services/auth.service';
import { SeatService } from '../../../services/seat.service';
import { UserService } from '../../../services/user.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './rooms.html',
  styleUrl: './rooms.css'
})
export class Rooms implements OnInit {

  listaSalas: any[] = [];
  listaSedes: any[] = [];

  rolUsuario = '';
  sedeAsignadaId = 0;
  esAdminGlobal = false;
  sedeFiltroId = 0;
  formEnviado = false;

  form!: FormGroup;

  // Matriz de asientos
  salaSeleccionadaMatriz: any = null;
  matrizFilas: { fila: string; asientos: any[] }[] = [];
  pincelSeleccionado = 'OCULTO';

  constructor(
    private fb: FormBuilder,
    private roomService: RoomService,
    private venueService: VenueService,
    private authService: AuthService,
    private userService: UserService,
    private seatService: SeatService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.construirForm();
    this.rolUsuario = this.authService.getRole();

    if (this.rolUsuario === 'ROLE_GERENTE_GENERAL' || this.rolUsuario === 'GERENTE_GENERAL') {
      this.esAdminGlobal = true;
      this.cargarSedes();
    } else {
      this.userService.getProfile().subscribe(perfil => {
        this.sedeAsignadaId = perfil.idVenue;
        this.form.get('idVenue')!.setValue(this.sedeAsignadaId);
        this.cargarSalasPorSede(this.sedeAsignadaId);
      });
    }
  }

  // ── Formulario ────────────────────────────────────────────────────────────

  private construirForm(): void {
    this.form = this.fb.group({
      nameRoom:    ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      numRows:     [null, [Validators.required, Validators.min(1), Validators.max(26)]],
      seatsPerRow: [null, [Validators.required, Validators.min(1), Validators.max(50)]],
      capacity:    [{ value: 0, disabled: true }],
      status:      ['Activo'],
      idVenue:     [0]
    });
  
    this.form.get('numRows')!.valueChanges.subscribe(() => this.actualizarCapacidad());
    this.form.get('seatsPerRow')!.valueChanges.subscribe(() => this.actualizarCapacidad());
  }

  ctrl(name: string) { return this.form.get(name)!; }

  invalido(name: string): boolean {
    const c = this.ctrl(name);
    return c.invalid && (c.touched || this.formEnviado);
  }

  get capacidadCalculada(): number {
    const rows = this.ctrl('numRows').value ?? 0;
    const cols = this.ctrl('seatsPerRow').value ?? 0;
    return rows > 0 && cols > 0 ? rows * cols : 0;
  }

  private actualizarCapacidad(): void {
    this.ctrl('capacity').setValue(this.capacidadCalculada);
  }

  // ── Datos ─────────────────────────────────────────────────────────────────

  cargarSedes(): void {
    this.venueService.getVenues().subscribe(sedes => {
      this.listaSedes = sedes;
      this.cdr.detectChanges();
    });
  }

  onSedeChange(): void {
    if (this.sedeFiltroId > 0) {
      this.cargarSalasPorSede(this.sedeFiltroId);
    } else {
      this.listaSalas = [];
    }
  }

  cargarSalasPorSede(idVenue: number): void {
    this.roomService.getRoomsByVenue(idVenue).subscribe(salas => {
      this.listaSalas = salas;
      this.cdr.detectChanges();
    });
  }

  // ── Modal Nueva Sala ──────────────────────────────────────────────────────

  prepararCreacion(): void {
    const idVenue = this.esAdminGlobal ? this.sedeFiltroId : this.sedeAsignadaId;
    this.formEnviado = false;
    this.form.reset({ nameRoom: '', numRows: null, seatsPerRow: null, capacity: 0, status: 'Activo', idVenue });
  }

  guardarSala(): void {
    this.formEnviado = true;
    this.form.markAllAsTouched();
    if (this.form.invalid || this.capacidadCalculada === 0) return;

    const idVenue = this.esAdminGlobal ? this.sedeFiltroId : this.sedeAsignadaId;
    if (idVenue <= 0) { alert('No se ha seleccionado a qué sede pertenece la sala.'); return; }

    const payload = {
      nameRoom:    this.ctrl('nameRoom').value,
      numRows:     this.ctrl('numRows').value,
      seatsPerRow: this.ctrl('seatsPerRow').value,
      capacity:    this.capacidadCalculada,
      status:      'Activo',
      idVenue
    };

    if (!confirm(`¿Registrar "${payload.nameRoom}" con ${payload.capacity} asientos?`)) return;

    this.roomService.createRoom(payload).subscribe({
      next: () => {
        alert('¡Sala creada con éxito!');
        document.getElementById('btnCerrarModalSala')?.click();
        this.cargarSalasPorSede(idVenue);
      },
      error: err => alert(err.error?.message || 'Error al guardar la sala')
    });
  }

  // ── Matriz de asientos ────────────────────────────────────────────────────

  abrirMatriz(sala: any): void {
    this.salaSeleccionadaMatriz = sala;
    this.seatService.getSeatsByRoom(sala.idRoom).subscribe(asientos => {
      const agrupado = asientos.reduce((acc: any, a: any) => {
        if (!acc[a.rowName]) acc[a.rowName] = [];
        acc[a.rowName].push(a);
        return acc;
      }, {});
      this.matrizFilas = Object.keys(agrupado).map(k => ({ fila: k, asientos: agrupado[k] }));
      this.cdr.detectChanges();
    });
  }

  seleccionarPincel(tipo: string): void { this.pincelSeleccionado = tipo; }

  aplicarPincel(asiento: any): void {
    switch (this.pincelSeleccionado) {
      case 'ACTIVO':        asiento.status = 'ACTIVO';        asiento.idSeatType = 1; break;
      case 'MANTENIMIENTO': asiento.status = 'MANTENIMIENTO'; asiento.idSeatType = 1; break;
      case 'OCULTO':        asiento.status = 'OCULTO';        asiento.idSeatType = 1; break;
      case 'WHEELCHAIR':    asiento.status = 'ACTIVO';        asiento.idSeatType = 2; break;
      default: return;
    }
    this.seatService.updateSeat(asiento.idSeat, asiento).subscribe({
      next: () => this.cdr.detectChanges(),
      error: () => alert('Error al actualizar la butaca.')
    });
  }

  obtenerClaseAsiento(asiento: any): string {
    if (asiento.status === 'OCULTO')        return 'seat-oculto';
    if (asiento.status === 'MANTENIMIENTO') return 'seat-mantenimiento';
    if (asiento.idSeatType === 2)           return 'seat-wheelchair';
    return 'seat-activo';
  }
}