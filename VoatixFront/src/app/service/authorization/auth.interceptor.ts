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
    // Не добавляем заголовок на эндпоинт логина
    if (req.url.includes('/api/login/new')) {
      return next.handle(req);
    }

    // Если сервер выставляет cookie с токеном, не добавляем дублирующий Authorization
    const cookieToken = this.auth.getTokenFromCookie();
    if (cookieToken) {
      return next.handle(req);
    }

    // получаем заголовок из localStorage (если есть)
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