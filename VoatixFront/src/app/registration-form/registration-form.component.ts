import { Component, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from "@angular/router";
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { AuthService } from '../service/authorization/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'registration-form-component',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterLink,
    RouterModule,
    MatButtonModule,
    CommonModule],
  templateUrl: './registration-form.component.html',
  styleUrls: ['./registration-form.component.scss']
})
export class RegistrationFormComponent {

  constructor(private router: Router, private authService: AuthService) { }

  public username = '';
  public email = '';
  public password = '';
  public passwordConfirm = '';
  public hide = true;
  public isLoading = false;
  public errorMessage = '';

  registrationBtnClick() {
    this.errorMessage = '';

    // Validation
    if (!this.username.trim()) {
      this.errorMessage = 'Пожалуйста, введите логин';
      return;
    }

    if (this.username.length < 6) {
      this.errorMessage = 'Логин должен быть не менее 6 символов';
      return;
    }

    if (this.username.length > 30) {
      this.errorMessage = 'Логин должен быть не более 30 символов';
      return;
    }

    if (!this.email.trim()) {
      this.errorMessage = 'Пожалуйста, введите email';
      return;
    }

    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Пожалуйста, введите корректный email';
      return;
    }

    if (!this.password) {
      this.errorMessage = 'Пожалуйста, введите пароль';
      return;
    }

    if (this.password.length < 6) {
      this.errorMessage = 'Пароль должен быть не менее 6 символов';
      return;
    }

    if (this.password.length > 30) {
      this.errorMessage = 'Пароль должен быть не более 30 символов';
      return;
    }

    if (this.password !== this.passwordConfirm) {
      this.errorMessage = 'Пароли не совпадают';
      return;
    }

    this.isLoading = true;
    this.authService.register(this.username, this.password, this.email).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка регистрации. Попробуйте снова';
      }
    });
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}