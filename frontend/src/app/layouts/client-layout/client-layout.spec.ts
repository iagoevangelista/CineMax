import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

import { ClientLayout } from './client-layout';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';

/**
 * Pruebas Unitarias — ClientLayout (Login, Registro, Recuperación)
 * CINEMAX · Módulo Auth & Seguridad (Integrante 1)
 * Formularios Reactivos (PASO 1 ya migrado)
 */
describe('ClientLayout — Auth Logic (Reactive Forms)', () => {
  let component: ClientLayout;
  let fixture: ComponentFixture<ClientLayout>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'login', 'register', 'forgotPassword', 'logout',
      'isLoggedIn', 'getRole', 'getEmail', 'getFirstName',
    ]);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getProfile'], {
      user$: of(null),
    });

    // Valores por defecto seguros para ngOnInit
    authServiceSpy.isLoggedIn.and.returnValue(false);
    userServiceSpy.getProfile.and.returnValue(of(null as any));

    await TestBed.configureTestingModule({
      imports: [ClientLayout, RouterTestingModule, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientLayout);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit y crea los FormGroups
  });

  // =========================================================================
  // iniciarSesion()
  // =========================================================================
  describe('iniciarSesion()', () => {

    it('CP-01: login exitoso — muestra mensaje de éxito y cargandoLogin vuelve a false', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of({ token: 'jwt-mock' }));
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getRole.and.returnValue('CLIENTE');
      authServiceSpy.getEmail.and.returnValue('juan@test.com');
      spyOn(document, 'getElementById').and.returnValue(null);

      // ✅ Así se setean valores en Formularios Reactivos
      component.loginForm.setValue({ email: 'juan@test.com', password: 'Password1' });
      component.iniciarSesion();

      expect(component.mensajeAuth).toBe('¡Sesión iniciada con éxito! Entrando...');
      expect(component.esErrorAuth).toBeFalse();
      expect(component.cargandoLogin).toBeFalse();
      tick(1500);
    }));

    it('CP-01b: login exitoso con rol ADMIN — redirige a /admin/dashboard', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of({ token: 'jwt-admin' }));
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getRole.and.returnValue('ADMIN');
      authServiceSpy.getEmail.and.returnValue('admin@test.com');
      spyOn(document, 'getElementById').and.returnValue(null);

      const routerSpy = spyOn((component as any).router, 'navigate');

      component.loginForm.setValue({ email: 'admin@test.com', password: 'Admin1234' });
      component.iniciarSesion();
      tick(1500);

      expect(routerSpy).toHaveBeenCalledWith(['/admin/dashboard']);
    }));

    it('CP-02: formulario inválido (campos vacíos) — NO llama al servicio y muestra error', () => {
      // El form nace vacío e inválido por defecto
      component.loginForm.setValue({ email: '', password: '' });
      component.iniciarSesion();

      expect(authServiceSpy.login).not.toHaveBeenCalled();
      expect(component.esErrorAuth).toBeTrue();
      expect(component.mensajeAuth).toBe('Por favor ingresa un correo válido y tu contraseña.');
    });

    it('CP-02b: email con formato inválido — formulario inválido, no llama al servicio', () => {
      component.loginForm.setValue({ email: 'no-es-email', password: 'Password1' });
      component.iniciarSesion();

      expect(authServiceSpy.login).not.toHaveBeenCalled();
      expect(component.esErrorAuth).toBeTrue();
    });

    it('CP-02c: credenciales incorrectas — muestra mensaje de error fijo', () => {
      authServiceSpy.login.and.returnValue(
        throwError(() => ({ error: { message: 'Bad credentials' } }))
      );

      component.loginForm.setValue({ email: 'juan@test.com', password: 'WrongPass1' });
      component.iniciarSesion();

      expect(component.esErrorAuth).toBeTrue();
      expect(component.mensajeAuth).toBe('Correo o contraseña incorrectos.');
      expect(component.cargandoLogin).toBeFalse();
    });

    it('CP-07: usuario inactivo — error del backend se muestra como mensaje fijo al usuario', () => {
      authServiceSpy.login.and.returnValue(
        throwError(() => ({ error: { message: 'User is disabled' } }))
      );

      component.loginForm.setValue({ email: 'inactivo@test.com', password: 'Password1' });
      component.iniciarSesion();

      // El componente muestra siempre el mensaje genérico (no expone la razón técnica)
      expect(component.esErrorAuth).toBeTrue();
      expect(component.mensajeAuth).toBe('Correo o contraseña incorrectos.');
      expect(component.cargandoLogin).toBeFalse();
    });

    it('cargandoLogin es TRUE mientras espera respuesta y FALSE al terminar', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of({ token: 'jwt-mock' }).pipe(delay(100)));
      authServiceSpy.getRole.and.returnValue('CLIENTE');
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getEmail.and.returnValue('juan@test.com');
      spyOn(document, 'getElementById').and.returnValue(null);

      component.loginForm.setValue({ email: 'juan@test.com', password: 'Password1' });
      component.iniciarSesion();

      expect(component.cargandoLogin).toBeTrue();
      tick(100);
      expect(component.cargandoLogin).toBeFalse();
      tick(1500);
    }));
  });

  // =========================================================================
  // registrarse()
  // =========================================================================
  describe('registrarse()', () => {

    // Helper para rellenar el registerForm completo y válido
    function llenarFormularioRegistroValido() {
      component.registerForm.setValue({
        firstName: 'Ana', lastName: 'López',
        idDocumentType: 1, documentNumber: '11223344',
        email: 'ana@test.com', password: 'Password1',
      });
    }

    it('CP-08: registro exitoso — muestra mensaje y cambia vista a login tras timeout', fakeAsync(() => {
      authServiceSpy.register.and.returnValue(of({ token: 'jwt-nuevo' }));
      llenarFormularioRegistroValido();
      component.registrarse();

      expect(component.esErrorAuth).toBeFalse();
      expect(component.mensajeAuth).toBe('¡Registro exitoso! Redirigiendo al login...');

      tick(2000);
      expect(component.vistaActiva).toBe('login');
    }));

    it('CP-09: email duplicado — muestra el mensaje exacto que viene del servidor', () => {
      authServiceSpy.register.and.returnValue(
        throwError(() => ({
          error: { message: 'El correo electrónico ya se encuentra registrado.' }
        }))
      );
      llenarFormularioRegistroValido();
      component.registrarse();

      expect(component.esErrorAuth).toBeTrue();
      expect(component.mensajeAuth).toBe('El correo electrónico ya se encuentra registrado.');
    });

    it('CP-08 validación local: formulario inválido — NO llama al servicio', () => {
      // Dejamos el form vacío (inválido)
      component.registerForm.setValue({
        firstName: '', lastName: '',
        idDocumentType: 1, documentNumber: '',
        email: '', password: '',
      });
      component.registrarse();

      expect(authServiceSpy.register).not.toHaveBeenCalled();
      expect(component.esErrorAuth).toBeTrue();
      expect(component.mensajeAuth).toBe('Por favor completa todos los campos correctamente.');
    });

    it('error genérico del servidor — usa mensaje fallback', () => {
      authServiceSpy.register.and.returnValue(
        throwError(() => ({ error: {} })) // sin campo message
      );
      llenarFormularioRegistroValido();
      component.registrarse();

      expect(component.mensajeAuth).toBe('Error al registrarse. Intenta de nuevo.');
    });
  });

  // =========================================================================
  // solicitarRecuperacion()
  // =========================================================================
  describe('solicitarRecuperacion()', () => {

    it('CP-15: email válido — muestra éxito, limpia el form y loadingRecuperacion vuelve a false', fakeAsync(() => {
      authServiceSpy.forgotPassword.and.returnValue(
        of({ message: '¡Enlace enviado! Revisa tu bandeja de entrada.' })
      );

      component.recuperarForm.setValue({ correoRecuperacion: 'juan@test.com' });
      component.solicitarRecuperacion();

      expect(component.loadingRecuperacion).toBeTrue(); // durante el delay
      tick(3000);

      expect(component.loadingRecuperacion).toBeFalse();
      expect(component.mensajeRecuperacion?.tipo).toBe('success');
      expect(component.mensajeRecuperacion?.texto).toContain('Enlace enviado');
      // El form se resetea con recuperarForm.reset()
      expect(component.recuperarForm.value.correoRecuperacion).toBeFalsy();
    }));

    it('CP-14: formulario inválido (email vacío) — NO llama al servicio, muestra error', () => {
      component.recuperarForm.setValue({ correoRecuperacion: '' });
      component.solicitarRecuperacion();

      expect(authServiceSpy.forgotPassword).not.toHaveBeenCalled();
      expect(component.mensajeRecuperacion?.tipo).toBe('danger');
      expect(component.mensajeRecuperacion?.texto).toBe(
        'Por favor ingresa un correo electrónico válido.'
      );
    });

    it('CP-14b: email con formato inválido — formulario inválido, no llama al servicio', () => {
      component.recuperarForm.setValue({ correoRecuperacion: 'no-es-email' });
      component.solicitarRecuperacion();

      expect(authServiceSpy.forgotPassword).not.toHaveBeenCalled();
      expect(component.mensajeRecuperacion?.tipo).toBe('danger');
    });

    it('CP-14c: email inexistente — servidor responde 200 (silent fail), muestra mensaje genérico de éxito', fakeAsync(() => {
      authServiceSpy.forgotPassword.and.returnValue(
        of({ message: 'Si el correo existe, se ha enviado un enlace de recuperación.' })
      );

      component.recuperarForm.setValue({ correoRecuperacion: 'noexiste@test.com' });
      component.solicitarRecuperacion();
      tick(3000);

      // El componente no puede distinguir si el email existe o no
      expect(component.mensajeRecuperacion?.tipo).toBe('success');
    }));

    it('error de servidor — muestra mensaje de danger con el texto del error', fakeAsync(() => {
      authServiceSpy.forgotPassword.and.returnValue(
        throwError(() => ({ error: { error: 'Error interno del servidor.' } }))
      );

      component.recuperarForm.setValue({ correoRecuperacion: 'juan@test.com' });
      component.solicitarRecuperacion();
      tick(3000);

      expect(component.mensajeRecuperacion?.tipo).toBe('danger');
      expect(component.mensajeRecuperacion?.texto).toBe('Error interno del servidor.');
    }));
  });

  // =========================================================================
  // Utilidades
  // =========================================================================
  describe('Utilidades del componente', () => {

    it('cambiarVista(): actualiza vistaActiva y limpia mensajeAuth', () => {
      component.mensajeAuth = 'mensaje previo';
      component.cambiarVista('registro');
      expect(component.vistaActiva).toBe('registro');
      expect(component.mensajeAuth).toBe('');
    });

    it('obtenerIniciales(): retorna las iniciales correctas', () => {
      expect(component.obtenerIniciales('Juan', 'Pérez')).toBe('JP');
      expect(component.obtenerIniciales('Ana', 'López')).toBe('AL');
    });

    it('obtenerIniciales(): retorna "U" si los nombres están vacíos', () => {
      expect(component.obtenerIniciales('', '')).toBe('U');
    });

    it('toggleMostrarPassword(): alterna entre true y false', () => {
      expect(component.mostrarPassword).toBeFalse();
      component.toggleMostrarPassword();
      expect(component.mostrarPassword).toBeTrue();
      component.toggleMostrarPassword();
      expect(component.mostrarPassword).toBeFalse();
    });

    it('cerrarSesion(): llama a logout() y establece isLogged=false', () => {
      spyOn(window, 'alert');
      // Ahora espiamos la función de nuestro componente, no la del navegador
      spyOn(component, 'recargarPagina').and.stub(); 
      
      component.isLogged = true;
      component.cerrarSesion();
      
      expect(authServiceSpy.logout).toHaveBeenCalled();
      expect(component.isLogged).toBeFalse();
      expect(component.recargarPagina).toHaveBeenCalled(); // Confirmamos que intentó recargar
    });

    it('verificarSesion(): isLogged=true si hay sesión activa', () => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getEmail.and.returnValue('juan@test.com');
      component.verificarSesion();
      expect(component.isLogged).toBeTrue();
      expect(component.userEmail).toBe('juan@test.com');
    });

    it('verificarSesion(): isLogged=false si no hay sesión', () => {
      authServiceSpy.isLoggedIn.and.returnValue(false);
      component.verificarSesion();
      expect(component.isLogged).toBeFalse();
    });
  });
});