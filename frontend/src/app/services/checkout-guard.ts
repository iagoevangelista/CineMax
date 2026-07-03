import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { BookingService } from './booking';

export const checkoutGuard: CanActivateFn = (route) => {
  const booking = inject(BookingService);
  const router = inject(Router);
  const resumen = booking.obtenerResumen();
  const path = route.routeConfig?.path;

  if (path === 'tickets' && !resumen.asientos?.length) {
    return router.parseUrl('/seats');
  }

  if (path === 'snacks' && !resumen.tickets?.length) {
    return router.parseUrl('/tickets');
  }

  if (path === 'payment' && !resumen.tickets?.length && !resumen.snacks?.length) {
    return router.parseUrl('/seats');
  }

  return true;
};
