import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('cinemax_token');

  // Siempre adjuntamos el token si existe — el backend decide los permisos
  const esRutaProtegida = req.url.includes('/api/v1/') &&
    !req.url.includes('/api/v1/auth/login') &&
    !req.url.includes('/api/v1/auth/register') &&
    !req.url.includes('/api/v1/auth/forgot');

  if (token && esRutaProtegida) {
    if (req.body instanceof FormData) {
      return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
    }
    return next(req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }));
  }

  return next(req);
};