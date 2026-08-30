import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  username = '';
  password = '';
  tenantCode = 'acme';
  loading = signal(false);

  submit(): void {
    if (!this.username || !this.password || !this.tenantCode) {
      this.snack.open('Username, password, and tenant code are required', 'Dismiss', {
        duration: 3500,
      });
      return;
    }
    this.loading.set(true);
    this.api
      .login({
        username: this.username,
        password: this.password,
        tenantCode: this.tenantCode,
      })
      .subscribe({
        next: (res) => {
          this.auth.login({
            accessToken: res.accessToken,
            tenantId: res.tenantId,
            role: res.role,
            username: res.username,
          });
          this.loading.set(false);
          void this.router.navigate(['/app/events']);
        },
        error: (err) => {
          this.loading.set(false);
          const msg = err?.error?.message || err?.message || 'Login failed';
          this.snack.open(String(msg), 'Dismiss', { duration: 4000 });
        },
      });
  }
}
