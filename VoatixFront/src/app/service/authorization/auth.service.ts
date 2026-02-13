import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private username = '';
  private password = '';
  private authorized = false;
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {
    // Очищаем старый HS256 токен при инициализации
    localStorage.removeItem('jwt');
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
    return this.authorized;
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
}
