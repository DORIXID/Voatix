// auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // получаем заголовок
    const authHeader = this.auth.getAuthHeader();
    
    // делаем новый запрос на основе старого, добавляя заголовок с baseAuth
    if (authHeader) {
      const modifiedReq = req.clone({
        setHeaders: {
          Authorization: authHeader
        }
      });
      return next.handle(modifiedReq);
    }

    // Если заголовка нет, пропускаем запрос без изменений
    return next.handle(req);
  }
}