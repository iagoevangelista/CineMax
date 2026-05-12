import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  // Inyectamos los servicios necesarios (en guards funcionales se usa inject)
  const authService = inject(AuthService);
  const router = inject(Router);

  // Si el usuario tiene un token guardado... ¡Pase usted!
  if (authService.isLoggedIn()) {
    return true;
  } 
  
  // Si NO tiene token... ¡Lo pateamos a la página principal!
  else {
    router.navigate(['/']); 
    return false;
  }
};