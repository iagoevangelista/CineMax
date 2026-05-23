import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. ¿Tiene sesión iniciada?
  if (!authService.isLoggedIn()) {
    router.navigate(['/']);
    return false;
  }

  // 2. ¿La ruta exige roles específicos? (MODIFICADO PARA ARREGLOS)
  const rolesPermitidos = route.data['expectedRoles'] as Array<string>; 
  const rolUsuario = authService.getRole();

  // Si la ruta pide roles, validamos si el rol del usuario está dentro de esa lista
  if (rolesPermitidos && rolesPermitidos.length > 0) {
    if (!rolesPermitidos.includes(rolUsuario)) {
      alert('Acceso Denegado: No tienes permisos para ver esta pantalla.');
      router.navigate(['/admin/dashboard']); // Lo mandamos a un lugar seguro
      return false;
    }
  }

  return true; // Si todo está bien, lo deja pasar
};