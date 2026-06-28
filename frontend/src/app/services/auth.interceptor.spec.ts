import { HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';

import { authInterceptor } from './auth.interceptor';

describe('AuthInterceptor', () => {

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  // ============================================================
  // CP-INT-01
  // ============================================================
  it('CP-INT-01: debe agregar Authorization Bearer cuando existe token y la ruta es protegida', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const request = new HttpRequest(
      'GET',
      '/api/v1/users/profile'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.get('Authorization'))
        .toBe('Bearer jwt-prueba');

      expect(modifiedRequest.headers.get('Content-Type'))
        .toBe('application/json');

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-02
  // ============================================================
  it('CP-INT-02: NO debe agregar Authorization en login aunque exista token', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const request = new HttpRequest(
      'POST',
      '/api/v1/auth/login',
      {}
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-03
  // ============================================================
  it('CP-INT-03: NO debe agregar Authorization en register aunque exista token', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const request = new HttpRequest(
      'POST',
      '/api/v1/auth/register',
        {}
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-04
  // ============================================================
  it('CP-INT-04: NO debe agregar Authorization en forgot password aunque exista token', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const request = new HttpRequest(
      'POST',
      '/api/v1/auth/forgot',
      {}
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-05
  // ============================================================
  it('CP-INT-05: NO debe agregar Authorization cuando no existe token', () => {

    localStorage.removeItem('cinemax_token');

    const request = new HttpRequest(
      'GET',
      '/api/v1/users/profile'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-06
  // ============================================================
  it('CP-INT-06: NO debe usar token con valor "null"', () => {

    localStorage.setItem('cinemax_token', 'null');

    const request = new HttpRequest(
      'GET',
      '/api/v1/users/profile'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-07
  // ============================================================
  it('CP-INT-07: NO debe usar token con valor "undefined"', () => {

    localStorage.setItem('cinemax_token', 'undefined');

    const request = new HttpRequest(
      'GET',
      '/api/v1/users/profile'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-08
  // ============================================================
  it('CP-INT-08: NO debe usar token vacío', () => {

    localStorage.setItem('cinemax_token', '');

    const request = new HttpRequest(
      'GET',
      '/api/v1/users/profile'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-09
  // ============================================================
  it('CP-INT-09: debe preservar FormData agregando únicamente Authorization', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const formData = new FormData();
    formData.append('imagen', 'archivo.jpg');

    const request = new HttpRequest(
      'POST',
      '/api/v1/users/upload-photo',
      formData
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.get('Authorization'))
        .toBe('Bearer jwt-prueba');

      expect(modifiedRequest.headers.has('Content-Type'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

  // ============================================================
  // CP-INT-10
  // ============================================================
  it('CP-INT-10: GET público de películas NO debe agregar Authorization', () => {

    localStorage.setItem('cinemax_token', 'jwt-prueba');

    const request = new HttpRequest(
      'GET',
      '/api/v1/movies'
    );

    authInterceptor(request, (modifiedRequest) => {

      expect(modifiedRequest.headers.has('Authorization'))
        .toBeFalse();

      return of(new HttpResponse({ status: 200 }));
    });

  });

});