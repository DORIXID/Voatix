import { ChangeDetectorRef, Component } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../service/authorization/auth.service';
import { HttpClient } from '@angular/common/http';
import { JwtResponse } from '../service/authorization/jwt-response.model';
import { CommonModule } from '@angular/common';
import { MatFormField } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';


@Component({
  selector: 'login-form-component',
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    CommonModule,
    MatButtonModule,
    MatFormField],
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss']
})
export class LoginFormComponent {
  username = '';
  password = '';
  error = '';

  public hide = true;
  constructor(private router: Router, private auth: AuthService, private http: HttpClient, private cdr: ChangeDetectorRef){}

  loginBtnClick(){
    this.http
      .post<JwtResponse>('/api/login/new', {
        username: this.username,
        password: this.password
      })
      .subscribe({
        next: (response) => {
          localStorage.setItem('jwt', response.token); // сохраняем токен
          this.auth.setAuthorized(true);
          this.error = '';
          this.router.navigate(['/main/ideas']);
        },
        error: () => {
          this.auth.setAuthorized(false);
          this.error = 'Неверный логин или пароль';
          this.cdr.detectChanges();
        }
      });
  }
}