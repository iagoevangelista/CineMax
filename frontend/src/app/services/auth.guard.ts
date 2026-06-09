import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/']);
    return false;
  }

  const rolesPermitidos = route.data['expectedRoles'] as Array<string>; 
  const rolUsuario = authService.getRole();

  if (rolesPermitidos && rolesPermitidos.length > 0) {
    if (!rolesPermitidos.includes(rolUsuario)) {
      alert('Acceso Denegado: No tienes permisos para ver esta pantalla.');
      router.navigate(['/admin/dashboard']); 
      return false;
    }
  }

  return true; 
};