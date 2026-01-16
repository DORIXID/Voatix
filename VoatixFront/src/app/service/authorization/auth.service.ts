import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private username = '';
  private password = '';
  private authorized = false;

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
}
