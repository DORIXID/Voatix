import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private username = '';
  private password = '';
  private authorized = false;
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {
  }

  setCredentials(username: string, password: string) {
    this.username = username;
    this.password = password;
    this.authorized = false;
  }

  setAuthorized(value: boolean) {
    this.authorized = value;
  }

  getAuthHeader(): string {
    const token = localStorage.getItem('jwt');
    return token ? `Bearer ${token}` : '';
  }

  isLoggedIn(): boolean {
    if (this.authorized) return true;
    // Also consider stored token or cookie as logged-in state
    const token = localStorage.getItem('jwt');
    if (token && token.trim().length > 0) return true;
    if (this.getTokenFromCookie()) return true;
    return false;
  }

  getUsername() {
    return this.username;
  }
  getPassword() {
    return this.password;
  }

  // Get token from cookie (used by backend)
  getTokenFromCookie(): string | null {
    const name = 'token=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');
    for (let cookie of cookieArray) {
      cookie = cookie.trim();
      if (cookie.indexOf(name) === 0) {
        return cookie.substring(name.length);
      }
    }
    return null;
  }

  register(nickname: string, password: string, eMail: string) {
    const registrationData = {
      nickname: nickname,
      password: password,
      eMail: eMail
    };
    return this.http.post(`${this.apiUrl}/users/new`, registrationData);
  }

  logout() {
    // Удаляем токен из localStorage
    localStorage.removeItem('jwt');
    
    // Очищаем куки с токеном
    document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    
    // Сбрасываем состояние авторизации
    this.authorized = false;
    this.username = '';
    this.password = '';
  }
}
