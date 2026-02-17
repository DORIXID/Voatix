import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../service/authorization/auth.service';
import { HttpClient } from '@angular/common/http';
import { JwtResponse } from '../service/authorization/jwt-response.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';


@Component({
  selector: 'login-form-component',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    CommonModule,
    MatButtonModule],
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss']
})
export class LoginFormComponent {
  username = '';
  password = '';
  error = '';

  public hide = true;
  constructor(private router: Router, private auth: AuthService, private http: HttpClient, private cdr: ChangeDetectorRef, private route: ActivatedRoute){}

  loginBtnClick(){
    this.http
      .post<JwtResponse>('/api/login/new', {
        username: this.username,
        password: this.password
      }, { withCredentials: true })
      .subscribe({
        next: (response) => {
          localStorage.setItem('jwt', response.token); // сохраняем токен
          this.auth.setAuthorized(true);
          this.error = '';
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/main/ideas';
          this.router.navigateByUrl(returnUrl);
        },
        error: (err) => {
          console.error('Login error:', err);
          this.auth.setAuthorized(false);
          this.error = 'Неверный логин или пароль';
          this.cdr.detectChanges();
        }
      });
  }
}