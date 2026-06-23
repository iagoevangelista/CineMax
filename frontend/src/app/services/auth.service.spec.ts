import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../enviroments/environment';

/**
 * Pruebas Unitarias — AuthService
 * CINEMAX · Módulo Auth & Seguridad (Integrante 1)
 *
 * Estrategia: HttpClientTestingModule intercepta las llamadas HTTP reales.
 * localStorage se espía con spyOn para no contaminar entre tests.
 *
 * CPs cubiertos desde la perspectiva del servicio Angular:
 *   login()           → almacena token en localStorage
 *   isLoggedIn()      → detecta token presente / ausente
 *   logout()          → elimina el token
 *   getRole()         → decodifica el claim "role" del JWT
 *   getPermissions()  → decodifica el claim "permissions"
 *   hasPermission()   → verifica permiso individual
 *   register()        → almacena token al registrarse
 *   forgotPassword()  → POST correcto al endpoint
 *   resetPassword()   → POST correcto al endpoint
 *   getEmail()        → decodifica sub del JWT
 *   getFirstName()    → decodifica firstName del JWT
 */

// JWT de prueba con payload conocido.
// Payload: { sub: "juan@test.com", role: "CLIENTE", firstName: "Juan",
//            permissions: ["VER_PELICULAS"], idVenue: 3, exp: 9999999999 }
const MOCK_JWT =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
  'eyJzdWIiOiJqdWFuQHRlc3QuY29tIiwicm9sZSI6IkNMSUVOVEUiLCJmaXJzdE5hbWUiOiJKdWFuIiwicGVybWlzc2lvbnMiOlsiVkVSX1BFTElDVUxBUyJdLCJpZFZlbnVlIjozLCJleHAiOjk5OTk5OTk5OTl9.' +
  'signature';

// JWT con role prefijado "ROLE_ADMIN" para probar el strip de prefijo
const MOCK_JWT_ADMIN =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
  'eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsInJvbGUiOiJST0xFX0FETUlOIiwiZmlyc3ROYW1lIjoiQWRtaW4iLCJwZXJtaXNzaW9ucyI6W10sImV4cCI6OTk5OTk5OTk5OX0.' +
  'signature';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/auth`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    // Limpiamos localStorage antes de cada test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify(); // asegura que no haya requests HTTP pendientes
    localStorage.clear();
  });

  // ── login() ────────────────────────────────────────────────────────────────

  it('login(): debe hacer POST a /login y guardar el token en localStorage', () => {
    const credentials = { email: 'juan@test.com', password: 'Password1' };

    service.login(credentials).subscribe((res) => {
      expect(res.token).toBe(MOCK_JWT);
    });

    const req = httpMock.expectOne(`${apiUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);

    req.flush({ token: MOCK_JWT });

    expect(localStorage.getItem('cinemax_token')).toBe(MOCK_JWT);
  });

  it('login(): si la respuesta NO trae token, NO debe modificar localStorage', () => {
    service.login({ email: 'x@x.com', password: '123' }).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/login`);
    req.flush({ message: 'Credenciales inválidas' }); // sin campo token

    expect(localStorage.getItem('cinemax_token')).toBeNull();
  });

  // ── isLoggedIn() ───────────────────────────────────────────────────────────

  it('isLoggedIn(): retorna TRUE cuando hay token en localStorage', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('isLoggedIn(): retorna FALSE cuando NO hay token en localStorage', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  // ── logout() ───────────────────────────────────────────────────────────────

  it('logout(): debe eliminar el token de localStorage', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    service.logout();
    expect(localStorage.getItem('cinemax_token')).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  // ── getRole() ──────────────────────────────────────────────────────────────

  it('getRole(): retorna el rol decodificado del JWT (sin prefijo ROLE_)', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.getRole()).toBe('CLIENTE');
  });

  it('getRole(): strips el prefijo ROLE_ cuando el backend lo incluye', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT_ADMIN);
    expect(service.getRole()).toBe('ADMIN');
  });

  it('getRole(): retorna "INVITADO" cuando no hay token', () => {
    expect(service.getRole()).toBe('INVITADO');
  });

  // ── getPermissions() & hasPermission() ────────────────────────────────────

  it('getPermissions(): retorna el array de permisos del JWT', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.getPermissions()).toEqual(['VER_PELICULAS']);
  });

  it('getPermissions(): retorna array vacío si no hay token', () => {
    expect(service.getPermissions()).toEqual([]);
  });

  it('hasPermission(): retorna TRUE para un permiso que el usuario tiene', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.hasPermission('VER_PELICULAS')).toBeTrue();
  });

  it('hasPermission(): retorna FALSE para un permiso que el usuario NO tiene', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.hasPermission('GESTIONAR_USUARIOS')).toBeFalse();
  });

  // ── register() ─────────────────────────────────────────────────────────────

  it('register(): debe hacer POST a /register y guardar el token en localStorage', () => {
    const data = {
      firstName: 'Ana',
      lastName: 'López',
      email: 'ana@test.com',
      password: 'Password1',
      documentNumber: '11223344',
      idDocumentType: 1,
    };

    service.register(data).subscribe((res) => {
      expect(res.token).toBe(MOCK_JWT);
    });

    const req = httpMock.expectOne(`${apiUrl}/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: MOCK_JWT });

    expect(localStorage.getItem('cinemax_token')).toBe(MOCK_JWT);
  });

  // ── forgotPassword() ───────────────────────────────────────────────────────

  it('forgotPassword(): debe hacer POST a /forgot-password con el email correcto', () => {
    service.forgotPassword('juan@test.com').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/forgot-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'juan@test.com' });
    req.flush({ message: 'Si el correo existe, se ha enviado un enlace de recuperación.' });
  });

  // ── resetPassword() ────────────────────────────────────────────────────────

  it('resetPassword(): debe hacer POST a /reset-password con token y nueva contraseña', () => {
    service.resetPassword('token-valido', 'NuevaPass1').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/reset-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'token-valido', newPassword: 'NuevaPass1' });
    req.flush({ message: 'Contraseña restablecida con éxito.' });
  });

  // ── getEmail() & getFirstName() ────────────────────────────────────────────

  it('getEmail(): retorna el email (sub) decodificado del JWT', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.getEmail()).toBe('juan@test.com');
  });

  it('getEmail(): retorna null si no hay token', () => {
    expect(service.getEmail()).toBeNull();
  });

  it('getFirstName(): retorna el firstName decodificado del JWT', () => {
    localStorage.setItem('cinemax_token', MOCK_JWT);
    expect(service.getFirstName()).toBe('Juan');
  });

  it('getFirstName(): retorna null si no hay token', () => {
    expect(service.getFirstName()).toBeNull();
  });
});