import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Verificar que el usuario esté logueado
  if (!authService.isLoggedIn()) {
    router.navigate(['/']);
    return false;
  }

  // Verificar permisos requeridos por la ruta
  const permisosRequeridos = route.data['expectedPermissions'] as string[] | undefined;

  if (permisosRequeridos && permisosRequeridos.length > 0) {
    const tienePermiso = permisosRequeridos.some(p => authService.hasPermission(p));

    if (!tienePermiso) {
      alert('Acceso Denegado: No tienes permisos para ver esta pantalla.');
      router.navigate(['/admin/dashboard']);
      return false;
    }
  }

  return true;
};
