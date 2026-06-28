import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { Users } from './users';
import { UserService } from '../../../services/user.service';
import { VenueService } from '../../../services/venue.service';
import { RoleService } from '../../../services/role.service';


describe('Users (admin)', () => {

  let component: Users;
  let fixture: ComponentFixture<Users>;

  // Mocks de servicios
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockVenueService: jasmine.SpyObj<VenueService>;
  let mockRoleService: jasmine.SpyObj<RoleService>;

  // Datos de prueba reutilizables
  const rolesMock = [
    { idRole: 1, roleName: 'ADMIN' },
    { idRole: 2, roleName: 'GERENTE_GENERAL' },
    { idRole: 3, roleName: 'GERENTE_DE_MARKETING' },
    { idRole: 4, roleName: 'CLIENTE' },
    { idRole: 5, roleName: 'GERENTE_DE_OPERACIONES' },
  ];

  const usuariosMock = [
    { idUser: 1, firstName: 'Carlos', lastName: 'Ramos', email: 'c@test.com', roleName: 'ADMIN', venueName: null, status: 'Activo' },
    { idUser: 2, firstName: 'María', lastName: 'López', email: 'm@test.com', roleName: 'GERENTE_DE_OPERACIONES', venueName: 'Sede Lima', status: 'Inactivo' },
  ];

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', ['getUsers', 'createUser', 'updateUserRole', 'deleteUser', 'activateUser']);
    mockVenueService = jasmine.createSpyObj('VenueService', ['getVenues', 'getAvailableVenuesForRole']);
    mockRoleService = jasmine.createSpyObj('RoleService', ['getRoles']);

    // Respuestas por defecto — cada test puede sobreescribirlas
    mockUserService.getUsers.and.returnValue(of(usuariosMock));
    mockRoleService.getRoles.and.returnValue(of(rolesMock));
    mockVenueService.getVenues.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [Users, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: UserService, useValue: mockUserService },
        { provide: VenueService, useValue: mockVenueService },
        { provide: RoleService, useValue: mockRoleService },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Users);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit
  });

  // ── Inicialización ─────────────────────────────────────────────────────────

  describe('Inicialización', () => {

    it('debe crear el componente correctamente', () => {
      expect(component).toBeTruthy();
    });

    it('debe llamar a getUsers y getRoles al inicializarse', () => {
      expect(mockUserService.getUsers).toHaveBeenCalledTimes(1);
      expect(mockRoleService.getRoles).toHaveBeenCalledTimes(1);
    });

    it('debe cargar la lista de usuarios desde el servicio', () => {
      expect(component.listaUsuarios.length).toBe(2);
      expect(component.listaUsuarios[0].email).toBe('c@test.com');
    });

    it('debe construir nuevoUsuarioForm con los campos esperados', () => {
      expect(component.nuevoUsuarioForm).toBeTruthy();
      expect(component.nuevoUsuarioForm.contains('firstName')).toBeTrue();
      expect(component.nuevoUsuarioForm.contains('lastName')).toBeTrue();
      expect(component.nuevoUsuarioForm.contains('email')).toBeTrue();
      expect(component.nuevoUsuarioForm.contains('password')).toBeTrue();
      expect(component.nuevoUsuarioForm.contains('idRole')).toBeTrue();
      expect(component.nuevoUsuarioForm.contains('idVenue')).toBeTrue();
    });

    it('debe inicializar nuevoUsuarioForm como inválido (campos vacíos)', () => {
      component.abrirModalNuevoUsuario();
      expect(component.nuevoUsuarioForm.valid).toBeFalse();
    });
  });

  // ── Validaciones del formulario "Nuevo Colaborador" ────────────────────────

  describe('nuevoUsuarioForm — validaciones', () => {

    beforeEach(() => {
      component.abrirModalNuevoUsuario(); // resetea el formulario limpio
    });

    it('CP-USR-05: firstName vacío hace el campo inválido', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: '' });
      expect(component.nuevoUsuarioForm.get('firstName')?.hasError('required')).toBeTrue();
    });

    it('firstName con menos de 2 caracteres hace el campo inválido', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: 'A' });
      expect(component.nuevoUsuarioForm.get('firstName')?.hasError('minlength')).toBeTrue();
    });

    it('firstName con 2+ caracteres hace el campo válido', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: 'Carlos' });
      expect(component.nuevoUsuarioForm.get('firstName')?.valid).toBeTrue();
    });

    it('CP-USR-06: email con formato inválido hace el campo inválido', () => {
      component.nuevoUsuarioForm.patchValue({ email: 'carlos.sin-arroba' });
      expect(component.nuevoUsuarioForm.get('email')?.hasError('email')).toBeTrue();
    });

    it('email vacío hace el campo inválido (required)', () => {
      component.nuevoUsuarioForm.patchValue({ email: '' });
      expect(component.nuevoUsuarioForm.get('email')?.hasError('required')).toBeTrue();
    });

    it('email válido hace el campo válido', () => {
      component.nuevoUsuarioForm.patchValue({ email: 'carlos@cinemax.com' });
      expect(component.nuevoUsuarioForm.get('email')?.valid).toBeTrue();
    });

    it('CP-USR-07: contraseña sin número viola el @Pattern', () => {
      component.nuevoUsuarioForm.patchValue({ password: 'abcdefgh' });
      expect(component.nuevoUsuarioForm.get('password')?.hasError('pattern')).toBeTrue();
    });

    it('contraseña sin letra viola el @Pattern', () => {
      component.nuevoUsuarioForm.patchValue({ password: '12345678' });
      expect(component.nuevoUsuarioForm.get('password')?.hasError('pattern')).toBeTrue();
    });

    it('contraseña con menos de 8 caracteres viola el @Pattern', () => {
      component.nuevoUsuarioForm.patchValue({ password: 'Abc123' });
      expect(component.nuevoUsuarioForm.get('password')?.hasError('pattern')).toBeTrue();
    });

    it('contraseña válida (8+ chars, letra y número) hace el campo válido', () => {
      component.nuevoUsuarioForm.patchValue({ password: 'Abcd1234' });
      expect(component.nuevoUsuarioForm.get('password')?.valid).toBeTrue();
    });

    it('idRole en 0 hace el campo inválido (placeholder "Seleccione un rol")', () => {
      component.nuevoUsuarioForm.patchValue({ idRole: 0 });
      expect(component.nuevoUsuarioForm.get('idRole')?.hasError('required')).toBeTrue();
    });

    it('formulario completo con datos válidos queda válido', () => {
      component.nuevoUsuarioForm.patchValue({
        firstName: 'Carlos', lastName: 'Ramos',
        email: 'carlos@cinemax.com', password: 'Abcd1234',
        idRole: 1, idVenue: 0,
        documentNumber: '70123456', idDocumentType: 1
      });
      expect(component.nuevoUsuarioForm.valid).toBeTrue();
    });
  });

  // ── campoInvalido() ────────────────────────────────────────────────────────

  describe('campoInvalido()', () => {

    it('devuelve false si el campo es válido y no se ha enviado el formulario', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: 'Carlos' });
      expect(component.campoInvalido('firstName')).toBeFalse();
    });

    it('devuelve true si el campo es inválido y el formulario fue enviado', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: '' });
      component.formEnviado = true;
      expect(component.campoInvalido('firstName')).toBeTrue();
    });

    it('devuelve true si el campo fue touched y es inválido', () => {
      component.nuevoUsuarioForm.get('firstName')?.markAsTouched();
      component.nuevoUsuarioForm.patchValue({ firstName: '' });
      expect(component.campoInvalido('firstName')).toBeTrue();
    });
  });

  // ── Filtros ────────────────────────────────────────────────────────────────

  describe('usuariosFiltrados', () => {

    it('filtra por estado Activo correctamente', () => {
      component.filtroEstado = 'Activo';
      expect(component.usuariosFiltrados.length).toBe(1);
      expect(component.usuariosFiltrados[0].status).toBe('Activo');
    });

    it('filtra por estado Inactivo correctamente', () => {
      component.filtroEstado = 'Inactivo';
      expect(component.usuariosFiltrados.length).toBe(1);
      expect(component.usuariosFiltrados[0].status).toBe('Inactivo');
    });

    it('TODOS muestra todos los usuarios sin filtrar', () => {
      component.filtroEstado = 'TODOS';
      expect(component.usuariosFiltrados.length).toBe(2);
    });

    it('filtra por rol correctamente', () => {
      component.filtroEstado = 'TODOS';
      component.filtroRol = 'ADMIN';
      expect(component.usuariosFiltrados.length).toBe(1);
      expect(component.usuariosFiltrados[0].roleName).toBe('ADMIN');
    });
  });

  // ── formatearRol() ─────────────────────────────────────────────────────────

  describe('formatearRol()', () => {

    it('convierte GERENTE_GENERAL a Gerente General', () => {
      expect(component.formatearRol('GERENTE_GENERAL')).toBe('Gerente General');
    });

    it('convierte GERENTE_DE_OPERACIONES a Gerente De Operaciones', () => {
      expect(component.formatearRol('GERENTE_DE_OPERACIONES')).toBe('Gerente De Operaciones');
    });

    it('convierte ADMIN a Admin', () => {
      expect(component.formatearRol('ADMIN')).toBe('Admin');
    });

    it('devuelve cadena vacía si recibe undefined o null', () => {
      expect(component.formatearRol('')).toBe('');
    });
  });

  // ── guardarNuevoUsuario() ──────────────────────────────────────────────────

  describe('guardarNuevoUsuario()', () => {

    it('no llama al servicio si el formulario es inválido', () => {
      component.abrirModalNuevoUsuario();
      // formulario vacío = inválido
      component.guardarNuevoUsuario();
      expect(mockUserService.createUser).not.toHaveBeenCalled();
    });

    it('llama a createUser si el formulario es válido', () => {
      mockUserService.createUser.and.returnValue(of({ idUser: 99, email: 'carlos@cinemax.com' } as any));

      component.nuevoUsuarioForm.patchValue({
        firstName: 'Carlos', lastName: 'Ramos',
        email: 'carlos@cinemax.com', password: 'Abcd1234',
        idRole: 1, idVenue: 0,
        documentNumber: '70123456', idDocumentType: 1
      });
      component.guardarNuevoUsuario();

      expect(mockUserService.createUser).toHaveBeenCalledTimes(1);
    });

    it('activa formEnviado = true al intentar guardar con formulario inválido', () => {
      component.abrirModalNuevoUsuario();
      component.formEnviado = false;
      component.guardarNuevoUsuario();
      expect(component.formEnviado).toBeTrue();
    });

    it('muestra errorServidor si el servicio devuelve un error', () => {
      mockUserService.createUser.and.returnValue(
        throwError(() => ({ error: { message: 'El correo ya está registrado.' } }))
      );

      component.nuevoUsuarioForm.patchValue({
        firstName: 'Carlos', lastName: 'Ramos',
        email: 'carlos@cinemax.com', password: 'Abcd1234',
        idRole: 1, idVenue: 0,
        documentNumber: '70123456', idDocumentType: 1
      });
      component.guardarNuevoUsuario();

      expect(component.errorServidor).toContain('correo ya está registrado');
    });
  });

  // ── abrirModalNuevoUsuario() ───────────────────────────────────────────────

  describe('abrirModalNuevoUsuario()', () => {

    it('resetea el formulario a valores vacíos', () => {
      component.nuevoUsuarioForm.patchValue({ firstName: 'Carlos', email: 'c@test.com' });
      component.abrirModalNuevoUsuario();
      expect(component.nuevoUsuarioForm.get('firstName')?.value).toBe('');
      expect(component.nuevoUsuarioForm.get('email')?.value).toBe('');
    });

    it('resetea formEnviado a false', () => {
      component.formEnviado = true;
      component.abrirModalNuevoUsuario();
      expect(component.formEnviado).toBeFalse();
    });

    it('resetea errorServidor a null', () => {
      component.errorServidor = 'algún error previo';
      component.abrirModalNuevoUsuario();
      expect(component.errorServidor).toBeNull();
    });
  });
});