import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('cinemax_token');
  
  const esRutaAdmin = req.url.includes('/admin') || 
                      req.url.includes('/users') || 
                      req.url.includes('/venues') || 
                      req.url.includes('/locations') ||
                      req.url.includes('/rooms') ||  
                      req.url.includes('/seats') ||
                      (req.url.includes('/movies') && req.method !== 'GET');

  if (token && esRutaAdmin) {
    if (req.body instanceof FormData) {
      const cloned = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
      return next(cloned);
    } 
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    return next(cloned);
  }

  return next(req);
};