import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { Rooms } from './rooms';
import { RoomService } from '../../../services/room.service';
import { VenueService } from '../../../services/venue.service';
import { AuthService } from '../../../services/auth.service';
import { UserService } from '../../../services/user.service';
import { SeatService } from '../../../services/seat.service';

/**
 * Pruebas unitarias del componente Rooms (HU-16).
 *
 * Estrategia: TestBed mínimo con mocks de los 5 servicios.
 * Se prueban:
 *   - Inicialización del FormGroup (campos y validadores)
 *   - Validaciones de cada campo (required, min, max)
 *   - Lógica de negocio: invalido(), capacidadCalculada, obtenerClaseAsiento()
 *   - Flujo según rol: GERENTE_GENERAL vs gerente de sede
 *   - Integración con servicios: cargarSalasPorSede(), guardarSala()
 */
describe('Rooms (HU-16)', () => {

  let component: Rooms;
  let fixture: ComponentFixture<Rooms>;

  let mockRoomService: jasmine.SpyObj<RoomService>;
  let mockVenueService: jasmine.SpyObj<VenueService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockSeatService: jasmine.SpyObj<SeatService>;

  const salasMock = [
    { idRoom: 1, nameRoom: 'Sala A', numRows: 5, seatsPerRow: 10, capacity: 50, status: 'Activo', idVenue: 1 },
    { idRoom: 2, nameRoom: 'Sala B', numRows: 4, seatsPerRow: 8,  capacity: 32, status: 'Activo', idVenue: 1 },
  ];

  const sedesMock = [
    { idVenue: 1, nameVenue: 'Sede Lima', addressVenue: 'Av. Lima 123', phoneNumber: '987654321', status: 'Activo' },
    { idVenue: 2, nameVenue: 'Sede Miraflores', addressVenue: 'Calle Miraflores 45', phoneNumber: '912345678', status: 'Activo' },
  ];

  const perfilMock = { idUser: 10, firstName: 'Gerente', idVenue: 1 };

  beforeEach(async () => {
    mockRoomService = jasmine.createSpyObj('RoomService', ['getRoomsByVenue', 'createRoom', 'updateRoom']);
    mockVenueService = jasmine.createSpyObj('VenueService', ['getVenues']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getRole']);
    mockUserService = jasmine.createSpyObj('UserService', ['getProfile']);
    mockSeatService = jasmine.createSpyObj('SeatService', ['getSeatsByRoom', 'updateSeat']);

    // Por defecto: rol de gerente de sede (no global)
    mockAuthService.getRole.and.returnValue('ROLE_GERENTE_DE_OPERACIONES');
    mockUserService.getProfile.and.returnValue(of(perfilMock));
    mockRoomService.getRoomsByVenue.and.returnValue(of(salasMock));
    mockVenueService.getVenues.and.returnValue(of(sedesMock));

    await TestBed.configureTestingModule({
      imports: [Rooms, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: RoomService,  useValue: mockRoomService },
        { provide: VenueService, useValue: mockVenueService },
        { provide: AuthService,  useValue: mockAuthService },
        { provide: UserService,  useValue: mockUserService },
        { provide: SeatService,  useValue: mockSeatService },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Rooms);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit
  });

  // ── Inicialización ─────────────────────────────────────────────────────────

  describe('Inicialización', () => {

    it('debe crear el componente correctamente', () => {
      expect(component).toBeTruthy();
    });

    it('debe construir el FormGroup con todos los campos esperados', () => {
      expect(component.form).toBeTruthy();
      ['nameRoom', 'numRows', 'seatsPerRow', 'capacity', 'status', 'idVenue'].forEach(campo => {
        expect(component.form.contains(campo)).toBeTrue();
      });
    });

    it('gerente no-global llama a getProfile para obtener su sede', () => {
      expect(mockUserService.getProfile).toHaveBeenCalledTimes(1);
    });

    it('gerente no-global carga salas de su sede asignada', () => {
      expect(mockRoomService.getRoomsByVenue).toHaveBeenCalledWith(1);
      expect(component.listaSalas.length).toBe(2);
    });

    it('gerente GERENTE_GENERAL llama a getVenues y no a getProfile', async () => {
      // Re-configurar para rol global
      mockAuthService.getRole.and.returnValue('ROLE_GERENTE_GENERAL');
      mockUserService.getProfile.calls.reset();
      mockVenueService.getVenues.calls.reset();

      fixture = TestBed.createComponent(Rooms);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(mockVenueService.getVenues).toHaveBeenCalledTimes(1);
      expect(mockUserService.getProfile).not.toHaveBeenCalled();
      expect(component.esAdminGlobal).toBeTrue();
    });
  });

  // ── Validaciones del FormGroup ─────────────────────────────────────────────

  describe('FormGroup — validaciones', () => {

    beforeEach(() => {
      component.prepararCreacion();
    });

    it('CP-SAL-01: nameRoom vacío hace el campo inválido (required)', () => {
      component.form.patchValue({ nameRoom: '' });
      expect(component.form.get('nameRoom')?.hasError('required')).toBeTrue();
    });

    it('nameRoom con 1 carácter viola minlength(2)', () => {
      component.form.patchValue({ nameRoom: 'A' });
      expect(component.form.get('nameRoom')?.hasError('minlength')).toBeTrue();
    });

    it('nameRoom con 51 caracteres viola maxlength(50)', () => {
      component.form.patchValue({ nameRoom: 'N'.repeat(51) });
      expect(component.form.get('nameRoom')?.hasError('maxlength')).toBeTrue();
    });

    it('CP-SAL-02: numRows en null hace el campo inválido (required)', () => {
      component.form.patchValue({ numRows: null });
      expect(component.form.get('numRows')?.hasError('required')).toBeTrue();
    });

    it('numRows en 0 viola min(1)', () => {
      component.form.patchValue({ numRows: 0 });
      expect(component.form.get('numRows')?.hasError('min')).toBeTrue();
    });

    it('numRows en 27 viola max(26)', () => {
      component.form.patchValue({ numRows: 27 });
      expect(component.form.get('numRows')?.hasError('max')).toBeTrue();
    });

    it('numRows en 10 es válido', () => {
      component.form.patchValue({ numRows: 10 });
      expect(component.form.get('numRows')?.valid).toBeTrue();
    });

    it('CP-SAL-03: seatsPerRow en null hace el campo inválido (required)', () => {
      component.form.patchValue({ seatsPerRow: null });
      expect(component.form.get('seatsPerRow')?.hasError('required')).toBeTrue();
    });

    it('seatsPerRow en 51 viola max(50)', () => {
      component.form.patchValue({ seatsPerRow: 51 });
      expect(component.form.get('seatsPerRow')?.hasError('max')).toBeTrue();
    });
  });

  // ── invalido() ─────────────────────────────────────────────────────────────

  describe('invalido()', () => {

    it('devuelve false si el campo es válido y no se ha enviado el formulario', () => {
      component.form.patchValue({ nameRoom: 'Sala A' });
      expect(component.invalido('nameRoom')).toBeFalse();
    });

    it('devuelve true si el campo es inválido y formEnviado es true', () => {
      component.form.patchValue({ nameRoom: '' });
      component.formEnviado = true;
      expect(component.invalido('nameRoom')).toBeTrue();
    });

    it('devuelve true si el campo fue touched y tiene error', () => {
      component.form.get('nameRoom')?.markAsTouched();
      component.form.patchValue({ nameRoom: '' });
      expect(component.invalido('nameRoom')).toBeTrue();
    });
  });

  // ── capacidadCalculada ─────────────────────────────────────────────────────

  describe('capacidadCalculada', () => {

    it('devuelve numRows × seatsPerRow cuando ambos son positivos', () => {
      component.form.patchValue({ numRows: 5, seatsPerRow: 10 });
      expect(component.capacidadCalculada).toBe(50);
    });

    it('devuelve 0 si numRows es 0 o null', () => {
      component.form.patchValue({ numRows: 0, seatsPerRow: 10 });
      expect(component.capacidadCalculada).toBe(0);
    });

    it('devuelve 0 si seatsPerRow es 0 o null', () => {
      component.form.patchValue({ numRows: 5, seatsPerRow: 0 });
      expect(component.capacidadCalculada).toBe(0);
    });

    it('actualiza el campo capacity del form automáticamente al cambiar numRows', () => {
      component.form.patchValue({ numRows: 3, seatsPerRow: 10 });
      component.form.get('numRows')!.setValue(6);
      expect(component.form.get('capacity')?.value).toBe(60);
    });
  });

  // ── obtenerClaseAsiento() ──────────────────────────────────────────────────

  describe('obtenerClaseAsiento()', () => {

    it('devuelve seat-oculto para asientos con status OCULTO', () => {
      expect(component.obtenerClaseAsiento({ status: 'OCULTO', idSeatType: 1 })).toBe('seat-oculto');
    });

    it('devuelve seat-mantenimiento para asientos con status MANTENIMIENTO', () => {
      expect(component.obtenerClaseAsiento({ status: 'MANTENIMIENTO', idSeatType: 1 })).toBe('seat-mantenimiento');
    });

    it('devuelve seat-wheelchair para asientos con idSeatType 2 y status ACTIVO', () => {
      expect(component.obtenerClaseAsiento({ status: 'ACTIVO', idSeatType: 2 })).toBe('seat-wheelchair');
    });

    it('devuelve seat-activo para asientos normales activos', () => {
      expect(component.obtenerClaseAsiento({ status: 'ACTIVO', idSeatType: 1 })).toBe('seat-activo');
    });
  });

  // ── guardarSala() ──────────────────────────────────────────────────────────

  describe('guardarSala()', () => {

    beforeEach(() => {
      component.prepararCreacion();
      component.sedeAsignadaId = 1;
    });

    it('no llama al servicio si el formulario es inválido', () => {
      // Formulario vacío = inválido
      component.guardarSala();
      expect(mockRoomService.createRoom).not.toHaveBeenCalled();
    });

    it('no llama al servicio si capacidadCalculada es 0', () => {
      component.form.patchValue({ nameRoom: 'Sala X', numRows: 0, seatsPerRow: 0 });
      component.guardarSala();
      expect(mockRoomService.createRoom).not.toHaveBeenCalled();
    });

    it('activa formEnviado = true al intentar guardar con formulario inválido', () => {
      component.formEnviado = false;
      component.guardarSala();
      expect(component.formEnviado).toBeTrue();
    });
  });

  // ── prepararCreacion() ────────────────────────────────────────────────────

  describe('prepararCreacion()', () => {

    it('resetea el formulario a valores iniciales', () => {
      component.form.patchValue({ nameRoom: 'Sala Z', numRows: 10, seatsPerRow: 20 });
      component.prepararCreacion();
      expect(component.form.get('nameRoom')?.value).toBe('');
      expect(component.form.get('numRows')?.value).toBeNull();
      expect(component.form.get('seatsPerRow')?.value).toBeNull();
    });

    it('resetea formEnviado a false', () => {
      component.formEnviado = true;
      component.prepararCreacion();
      expect(component.formEnviado).toBeFalse();
    });
  });

  // ── cargarSalasPorSede() ───────────────────────────────────────────────────

  describe('cargarSalasPorSede()', () => {

    it('carga la lista de salas de la sede indicada', () => {
      mockRoomService.getRoomsByVenue.and.returnValue(of(salasMock));
      component.cargarSalasPorSede(1);
      expect(mockRoomService.getRoomsByVenue).toHaveBeenCalledWith(1);
      expect(component.listaSalas.length).toBe(2);
    });

    it('deja listaSalas vacía si el servicio no devuelve nada', () => {
      mockRoomService.getRoomsByVenue.and.returnValue(of([]));
      component.cargarSalasPorSede(2);
      expect(component.listaSalas.length).toBe(0);
    });
  });
});