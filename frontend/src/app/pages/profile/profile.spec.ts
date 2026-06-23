import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';

import { Profile } from './profile';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

describe('Profile', () => {

  let component: Profile;
  let fixture: ComponentFixture<Profile>;

  let mockUserService: jasmine.SpyObj<UserService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const perfilMock = {
    idUser: 1,
    firstName: 'María',
    lastName: 'López',
    email: 'maria@cinemax.com',
    phone: '987654321',
    datebirth: '1995-03-20',
    idDocumentType: 1,
    documentNumber: '70123456',
    imageUrl: ''
  };

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', [
      'getProfile', 'updateProfile', 'deleteMyAccount', 'updateLocalUser'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'logout']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate'], { events: of() });

    // Por defecto: usuario autenticado y perfil cargado
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockUserService.getProfile.and.returnValue(of(perfilMock));

    await TestBed.configureTestingModule({
      imports: [Profile, ReactiveFormsModule],
      providers: [
        FormBuilder,
        ChangeDetectorRef,
        { provide: UserService, useValue: mockUserService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit
  });

  // ── Inicialización ─────────────────────────────────────────────────────────

  describe('Inicialización', () => {

    it('debe crear el componente correctamente', () => {
      expect(component).toBeTruthy();
    });

    it('redirige a / si el usuario no está autenticado', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);
      component.ngOnInit();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('llama a getProfile al inicializarse', () => {
      expect(mockUserService.getProfile).toHaveBeenCalled();
    });

    it('carga los datos del perfil en datosForm al inicializarse', () => {
      expect(component.datosForm.get('firstName')?.value).toBe('María');
      expect(component.datosForm.get('lastName')?.value).toBe('López');
    });

    it('guarda los campos de solo lectura en perfilSoloLectura', () => {
      expect(component.perfilSoloLectura.email).toBe('maria@cinemax.com');
      expect(component.perfilSoloLectura.documentNumber).toBe('70123456');
    });

    it('construye datosForm y seguridadForm al inicializarse', () => {
      expect(component.datosForm).toBeTruthy();
      expect(component.seguridadForm).toBeTruthy();
    });
  });

  // ── datosForm — validaciones ───────────────────────────────────────────────

  describe('datosForm — validaciones', () => {

    it('CP-PRF-02: firstName vacío hace el campo inválido (required)', () => {
      component.datosForm.patchValue({ firstName: '' });
      expect(component.datosForm.get('firstName')?.hasError('required')).toBeTrue();
    });

    it('firstName con menos de 2 caracteres viola minlength', () => {
      component.datosForm.patchValue({ firstName: 'A' });
      expect(component.datosForm.get('firstName')?.hasError('minlength')).toBeTrue();
    });

    it('firstName con 2+ caracteres es válido', () => {
      component.datosForm.patchValue({ firstName: 'María' });
      expect(component.datosForm.get('firstName')?.valid).toBeTrue();
    });

    it('lastName vacío hace el campo inválido (required)', () => {
      component.datosForm.patchValue({ lastName: '' });
      expect(component.datosForm.get('lastName')?.hasError('required')).toBeTrue();
    });

    it('teléfono con letras viola el patrón', () => {
      component.datosForm.patchValue({ phone: 'abcdefgh' });
      expect(component.datosForm.get('phone')?.hasError('pattern')).toBeTrue();
    });

    it('teléfono con menos de 7 dígitos viola el patrón', () => {
      component.datosForm.patchValue({ phone: '123456' });
      expect(component.datosForm.get('phone')?.hasError('pattern')).toBeTrue();
    });

    it('teléfono con 9 dígitos es válido', () => {
      component.datosForm.patchValue({ phone: '987654321' });
      expect(component.datosForm.get('phone')?.valid).toBeTrue();
    });

    it('teléfono vacío es válido (es opcional)', () => {
      component.datosForm.patchValue({ phone: '' });
      expect(component.datosForm.get('phone')?.valid).toBeTrue();
    });

    it('fecha de nacimiento futura viola el validador personalizado', () => {
      const fechaFutura = new Date();
      fechaFutura.setFullYear(fechaFutura.getFullYear() + 1);
      const fechaStr = fechaFutura.toISOString().split('T')[0];
      component.datosForm.patchValue({ datebirth: fechaStr });
      expect(component.datosForm.get('datebirth')?.hasError('fechaFutura')).toBeTrue();
    });

    it('fecha de nacimiento pasada es válida', () => {
      component.datosForm.patchValue({ datebirth: '1995-03-20' });
      expect(component.datosForm.get('datebirth')?.valid).toBeTrue();
    });
  });

  // ── seguridadForm — validaciones ───────────────────────────────────────────

  describe('seguridadForm — validaciones', () => {

    it('oldPassword vacío hace el campo inválido', () => {
      component.seguridadForm.patchValue({ oldPassword: '' });
      expect(component.seguridadForm.get('oldPassword')?.hasError('required')).toBeTrue();
    });

    it('CP-USR-07: newPassword sin número viola el @Pattern', () => {
      component.seguridadForm.patchValue({ newPassword: 'abcdefgh' });
      expect(component.seguridadForm.get('newPassword')?.hasError('pattern')).toBeTrue();
    });

    it('newPassword sin letra viola el @Pattern', () => {
      component.seguridadForm.patchValue({ newPassword: '12345678' });
      expect(component.seguridadForm.get('newPassword')?.hasError('pattern')).toBeTrue();
    });

    it('newPassword con menos de 8 caracteres viola el @Pattern', () => {
      component.seguridadForm.patchValue({ newPassword: 'Abc123' });
      expect(component.seguridadForm.get('newPassword')?.hasError('pattern')).toBeTrue();
    });

    it('newPassword válida (8+ chars, letra y número) hace el campo válido', () => {
      component.seguridadForm.patchValue({ newPassword: 'Abcd1234' });
      expect(component.seguridadForm.get('newPassword')?.valid).toBeTrue();
    });

    it('confirmPassword distinto a newPassword activa passwordMismatch en el grupo', () => {
      component.seguridadForm.patchValue({
        oldPassword: 'Actual123',
        newPassword: 'Abcd1234',
        confirmPassword: 'OtraClave9'
      });
      expect(component.seguridadForm.hasError('passwordMismatch')).toBeTrue();
    });

    it('confirmPassword igual a newPassword no activa passwordMismatch', () => {
      component.seguridadForm.patchValue({
        oldPassword: 'Actual123',
        newPassword: 'Abcd1234',
        confirmPassword: 'Abcd1234'
      });
      expect(component.seguridadForm.hasError('passwordMismatch')).toBeFalse();
    });

    it('formulario de seguridad completo y válido queda válido', () => {
      component.seguridadForm.patchValue({
        oldPassword: 'Actual123',
        newPassword: 'Abcd1234',
        confirmPassword: 'Abcd1234'
      });
      expect(component.seguridadForm.valid).toBeTrue();
    });
  });

  // ── confirmPasswordNoCoincide ──────────────────────────────────────────────

  describe('confirmPasswordNoCoincide (getter)', () => {

    it('devuelve false si las contraseñas coinciden y el campo fue tocado', () => {
      component.seguridadForm.patchValue({
        newPassword: 'Abcd1234', confirmPassword: 'Abcd1234'
      });
      component.seguridadForm.get('confirmPassword')?.markAsTouched();
      expect(component.confirmPasswordNoCoincide).toBeFalse();
    });

    it('devuelve true si las contraseñas no coinciden y el campo fue tocado', () => {
      component.seguridadForm.patchValue({
        newPassword: 'Abcd1234', confirmPassword: 'Distinta9'
      });
      component.seguridadForm.get('confirmPassword')?.markAsTouched();
      expect(component.confirmPasswordNoCoincide).toBeTrue();
    });

    it('devuelve false si el formulario no fue enviado ni el campo tocado', () => {
      component.seguridadForm.patchValue({
        newPassword: 'Abcd1234', confirmPassword: 'Distinta9'
      });
      component.formSeguridadEnviado = false;
      expect(component.confirmPasswordNoCoincide).toBeFalse();
    });
  });

  // ── campoDatosInvalido() ───────────────────────────────────────────────────

  describe('campoDatosInvalido()', () => {

    it('devuelve false si el campo es válido', () => {
      component.datosForm.patchValue({ firstName: 'María' });
      expect(component.campoDatosInvalido('firstName')).toBeFalse();
    });

    it('devuelve true si el campo es inválido y formDatosEnviado es true', () => {
      component.datosForm.patchValue({ firstName: '' });
      component.formDatosEnviado = true;
      expect(component.campoDatosInvalido('firstName')).toBeTrue();
    });

    it('devuelve true si el campo fue touched y es inválido', () => {
      component.datosForm.get('firstName')?.markAsTouched();
      component.datosForm.patchValue({ firstName: '' });
      expect(component.campoDatosInvalido('firstName')).toBeTrue();
    });
  });

  // ── campoSeguridadInvalido() ───────────────────────────────────────────────

  describe('campoSeguridadInvalido()', () => {

    it('devuelve false si el campo es válido', () => {
      component.seguridadForm.patchValue({ oldPassword: 'Actual123' });
      expect(component.campoSeguridadInvalido('oldPassword')).toBeFalse();
    });

    it('devuelve true si el campo es inválido y formSeguridadEnviado es true', () => {
      component.seguridadForm.patchValue({ oldPassword: '' });
      component.formSeguridadEnviado = true;
      expect(component.campoSeguridadInvalido('oldPassword')).toBeTrue();
    });
  });

  // ── guardarDatosPersonales() ───────────────────────────────────────────────

  describe('guardarDatosPersonales()', () => {

    it('no llama al servicio si datosForm es inválido', () => {
      component.datosForm.patchValue({ firstName: '' });
      component.guardarDatosPersonales();
      expect(mockUserService.updateProfile).not.toHaveBeenCalled();
    });

    it('activa formDatosEnviado al intentar guardar con formulario inválido', () => {
      component.datosForm.patchValue({ firstName: '' });
      component.formDatosEnviado = false;
      component.guardarDatosPersonales();
      expect(component.formDatosEnviado).toBeTrue();
    });

    it('llama a updateProfile si el formulario es válido', () => {
      mockUserService.updateProfile.and.returnValue(of(perfilMock));
      mockUserService.updateLocalUser = jasmine.createSpy();

      component.datosForm.patchValue({
        firstName: 'María', lastName: 'López',
        phone: '987654321', datebirth: '1995-03-20'
      });
      component.guardarDatosPersonales();

      expect(mockUserService.updateProfile).toHaveBeenCalledTimes(1);
    });

    it('muestra mensaje de éxito tras actualizar correctamente', () => {
      mockUserService.updateProfile.and.returnValue(of(perfilMock));
      mockUserService.updateLocalUser = jasmine.createSpy();

      component.datosForm.patchValue({
        firstName: 'María', lastName: 'López',
        phone: '987654321', datebirth: '1995-03-20'
      });
      component.guardarDatosPersonales();

      expect(component.mensaje).toContain('actualizados correctamente');
      expect(component.esError).toBeFalse();
    });

    it('muestra mensaje de error si el servicio falla', () => {
      mockUserService.updateProfile.and.returnValue(
        throwError(() => ({ error: { message: 'Error al actualizar.' } }))
      );

      component.datosForm.patchValue({
        firstName: 'María', lastName: 'López',
        phone: '987654321', datebirth: '1995-03-20'
      });
      component.guardarDatosPersonales();

      expect(component.esError).toBeTrue();
      expect(component.mensaje).toContain('Error al actualizar');
    });
  });

  // ── actualizarContrasena() ─────────────────────────────────────────────────

  describe('actualizarContrasena()', () => {

    it('no llama al servicio si seguridadForm es inválido', () => {
      component.seguridadForm.reset();
      component.actualizarContrasena();
      expect(mockUserService.updateProfile).not.toHaveBeenCalled();
    });

    it('activa formSeguridadEnviado al intentar con formulario inválido', () => {
      component.seguridadForm.reset();
      component.formSeguridadEnviado = false;
      component.actualizarContrasena();
      expect(component.formSeguridadEnviado).toBeTrue();
    });

    it('CP-PRF-03: muestra error si la contraseña actual es incorrecta (error del servidor)', () => {
      mockUserService.updateProfile.and.returnValue(
        throwError(() => ({ error: { message: 'Contraseña actual incorrecta.' } }))
      );

      component.seguridadForm.patchValue({
        oldPassword: 'incorrecta123',
        newPassword: 'Abcd1234',
        confirmPassword: 'Abcd1234'
      });
      component.actualizarContrasena();

      expect(component.esError).toBeTrue();
      expect(component.mensaje).toContain('Contraseña actual incorrecta');
    });

    it('muestra mensaje de éxito y resetea el formulario al cambiar correctamente', () => {
      mockUserService.updateProfile.and.returnValue(of(perfilMock));

      component.seguridadForm.patchValue({
        oldPassword: 'Actual123',
        newPassword: 'Abcd1234',
        confirmPassword: 'Abcd1234'
      });
      component.actualizarContrasena();

      expect(component.mensaje).toContain('Contraseña cambiada con éxito');
      expect(component.esError).toBeFalse();
      expect(component.seguridadForm.get('newPassword')?.value).toBeFalsy();
    });
  });

  // ── eliminarCuenta() ───────────────────────────────────────────────────────

  // ── eliminarCuenta() ───────────────────────────────────────────────────────

  describe('eliminarCuenta()', () => {

    let timeoutOriginal: any;

    beforeEach(() => {
      // Simula que el usuario confirma el diálogo
      spyOn(window, 'confirm').and.returnValue(true);
      
      // Evita que el cuadro de alerta congele la pantalla
      spyOn(window, 'alert'); 

      // 1. Guardamos la función real del cronómetro de Chrome
      timeoutOriginal = window.setTimeout;

      // 2. Creamos un cronómetro "espía" y lo interceptamos
      spyOn(window, 'setTimeout').and.callFake((fn: any, delay: any) => {
        // Si detectamos que es nuestro timeout de 500ms (el del reinicio), lo matamos
        if (delay === 500) {
          return 0; 
        }
        // Para cualquier otro cronómetro interno de Angular, lo dejamos pasar normal
        return timeoutOriginal(fn, delay);
      });
    });

    it('llama a deleteMyAccount si el usuario confirma', () => {
      mockUserService.deleteMyAccount.and.returnValue(of(undefined));
      component.eliminarCuenta();
      expect(mockUserService.deleteMyAccount).toHaveBeenCalledTimes(1);
    });

    it('no llama a deleteMyAccount si el usuario cancela el diálogo', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);
      component.eliminarCuenta();
      expect(mockUserService.deleteMyAccount).not.toHaveBeenCalled();
    });

    it('llama a logout y redirige a / tras eliminar correctamente', () => {
      mockUserService.deleteMyAccount.and.returnValue(of(undefined));
      component.eliminarCuenta();
      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('muestra mensaje de error si el servicio rechaza (colaborador intentando autoeliminarse)', () => {
      mockUserService.deleteMyAccount.and.returnValue(
        throwError(() => ({
          error: { message: 'Los colaboradores no pueden eliminar su propia cuenta.' }
        }))
      );
      component.eliminarCuenta();
      expect(component.esError).toBeTrue();
      expect(component.mensaje).toContain('no pueden eliminar su propia cuenta');
    });
  });

  // ── inicialNombre / inicialApellido ────────────────────────────────────────

  describe('Getters de iniciales', () => {

    it('inicialNombre devuelve la primera letra del firstName en mayúscula', () => {
      component.datosForm.patchValue({ firstName: 'maría' });
      expect(component.inicialNombre).toBe('M');
    });

    it('inicialApellido devuelve la primera letra del lastName en mayúscula', () => {
      component.datosForm.patchValue({ lastName: 'lópez' });
      expect(component.inicialApellido).toBe('L');
    });

    it('inicialNombre devuelve cadena vacía si firstName está vacío', () => {
      component.datosForm.patchValue({ firstName: '' });
      expect(component.inicialNombre).toBe('');
    });
  });
});