import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';

import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const requestToSend = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(requestToSend).pipe(
    tap({
      error: (error: { status?: number; message?: string }) => {
        if (requestToSend.url.startsWith('/api/')) {
          console.error('[CryptoFolio][HTTP] error', {
            method: requestToSend.method,
            url: requestToSend.url,
            status: error?.status,
            message: error?.message
          });
        }
      }
    })
  );
};
