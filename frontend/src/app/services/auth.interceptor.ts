import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('cinemax_token');

  // Solo agregar token en rutas de admin
  const esRutaAdmin = req.url.includes('/admin') || req.url.includes('/users') || req.url.includes('/venues');

  if (token && esRutaAdmin) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  return next(req);
};