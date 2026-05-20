import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('cinemax_token');

  // <-- AQUÍ ESTÁ EL CAMBIO: Agregamos '/locations' para que viaje con token
  const esRutaAdmin = req.url.includes('/admin') || 
                      req.url.includes('/users') || 
                      req.url.includes('/venues') || 
                      req.url.includes('/locations');

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