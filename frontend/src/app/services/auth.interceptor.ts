import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('cinemax_token');

  // Ajustamos la lógica de rutas:
  const esRutaAdmin = req.url.includes('/admin') || 
                       req.url.includes('/users') || 
                       req.url.includes('/venues') || 
                       req.url.includes('/locations') ||
                       req.url.includes('/rooms') ||  
                       req.url.includes('/seats');   

  if (token && esRutaAdmin) {
    // Si el body es FormData, NO enviamos 'Content-Type' (el navegador lo gestiona solo)
    if (req.body instanceof FormData) {
      const cloned = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
      return next(cloned);
    } 
    
    // Si es JSON normal, sí enviamos cabeceras completas
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