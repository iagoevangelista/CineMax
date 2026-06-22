import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. Obtener el token de forma segura
  const token = localStorage.getItem('cinemax_token');
  const hasToken = token && token.trim() !== '' && token !== 'null' && token !== 'undefined';

  // 2. Definir rutas que son ESTRICTAMENTE públicas en el sistema
  const esRutaPublica = 
    req.url.includes('/api/v1/auth/login') ||
    req.url.includes('/api/v1/auth/register') ||
    req.url.includes('/api/v1/auth/forgot') ||
    (req.method === 'GET' && req.url.includes('/api/v1/snacks')) ||
    (req.method === 'GET' && req.url.includes('/api/v1/movies')) ||
    (req.method === 'GET' && req.url.includes('/api/v1/showtimes'));

  // 3. Lógica de clonación de la petición
  if (hasToken && !esRutaPublica) {
    if (req.body instanceof FormData) {
      return next(req.clone({ 
        setHeaders: { Authorization: `Bearer ${token}` } 
      }));
    }
    
    return next(req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }));
  }

  // Si no hay token o es una ruta pública, enviamos la petición limpia con su Content-Type por defecto
  const headers: { [key: string]: string } = {};
  if (!(req.body instanceof FormData) && req.method !== 'GET') {
    headers['Content-Type'] = 'application/json';
  }

  return next(req.clone({ setHeaders: headers }));
};