import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

/**
 * Pruebas Unitarias — authGuard (CanActivateFn)
 * CINEMAX · Módulo Auth & Seguridad (Integrante 1)
 *
 * El guard tiene 3 responsabilidades:
 *   1. Si el usuario NO está logueado → redirige a '/' y bloquea la ruta.
 *   2. Si está logueado y la ruta NO requiere permisos → deja pasar.
 *   3. Si está logueado pero le FALTAN permisos → alerta, redirige a /admin/dashboard y bloquea.
 *
 * Estrategia: ejecutamos el guard directamente con TestBed.runInInjectionContext()
 * para respetar el patrón funcional (CanActivateFn) de Angular 16+.
 */
describe('authGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  // Helpers para construir mocks de ActivatedRouteSnapshot con data personalizada
  function buildRoute(expectedPermissions?: string[]): ActivatedRouteSnapshot {
    const route = new ActivatedRouteSnapshot();
    route.data = expectedPermissions ? { expectedPermissions } : {};
    return route;
  }

  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isLoggedIn',
      'hasPermission',
    ]);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  // ── Usuario NO logueado ────────────────────────────────────────────────────

  it('debe retornar FALSE y redirigir a "/" si el usuario NO está logueado', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(buildRoute(), mockState)
    );

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  // ── Usuario logueado, ruta sin requisito de permisos ──────────────────────

  it('debe retornar TRUE si el usuario está logueado y la ruta no requiere permisos', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(buildRoute(), mockState) // sin expectedPermissions
    );

    expect(result).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('debe retornar TRUE si expectedPermissions está presente pero es un array vacío', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(buildRoute([]), mockState) // array vacío = sin requisito real
    );

    expect(result).toBeTrue();
  });

  // ── Usuario logueado, ruta CON requisito de permisos ──────────────────────

  it('debe retornar TRUE si el usuario tiene AL MENOS UNO de los permisos requeridos', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    // hasPermission devuelve true solo para 'GESTIONAR_PELICULAS'
    authServiceSpy.hasPermission.and.callFake(
      (p: string) => p === 'GESTIONAR_PELICULAS'
    );

    const result = TestBed.runInInjectionContext(() =>
      authGuard(
        buildRoute(['GESTIONAR_PELICULAS', 'GESTIONAR_USUARIOS']),
        mockState
      )
    );

    expect(result).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('debe retornar FALSE y redirigir a /admin/dashboard si NO tiene ningún permiso requerido', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.hasPermission.and.returnValue(false); // ningún permiso

    spyOn(window, 'alert'); // silenciamos el alert del guard

    const result = TestBed.runInInjectionContext(() =>
      authGuard(buildRoute(['GESTIONAR_USUARIOS']), mockState)
    );

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
    expect(window.alert).toHaveBeenCalledWith(
      'Acceso Denegado: No tienes permisos para ver esta pantalla.'
    );
  });

  // ── Flujo completo: no logueado con ruta que pide permisos ─────────────────

  it('debe redirigir a "/" (no a /admin/dashboard) si no está logueado, aunque la ruta tenga permisos', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(buildRoute(['GESTIONAR_USUARIOS']), mockState)
    );

    expect(result).toBeFalse();
    // La primera comprobación es isLoggedIn, debe ir a '/'
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    expect(routerSpy.navigate).not.toHaveBeenCalledWith(['/admin/dashboard']);
  });
});