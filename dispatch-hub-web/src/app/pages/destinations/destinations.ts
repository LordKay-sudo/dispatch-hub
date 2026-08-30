import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ApiService, Destination } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-destinations',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
  ],
  templateUrl: './destinations.html',
  styleUrl: './destinations.scss',
})
export class DestinationsPage implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly rows = signal<Destination[]>([]);
  readonly columns = ['name', 'targetUrl', 'enabled'];

  name = '';
  targetUrl = '';

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.api.listDestinations().subscribe({
      next: (list) => {
        this.rows.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message || 'Failed to load destinations', 'Dismiss', {
          duration: 4000,
        });
      },
    });
  }

  create(): void {
    if (!this.auth.isAdmin) {
      this.snack.open('Admin role required to create destinations', 'Dismiss', { duration: 3500 });
      return;
    }
    if (!this.name.trim() || !this.targetUrl.trim()) {
      this.snack.open('Name and target URL are required', 'Dismiss', { duration: 3500 });
      return;
    }
    this.saving.set(true);
    this.api
      .createDestination({
        name: this.name.trim(),
        targetUrl: this.targetUrl.trim(),
        enabled: true,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.name = '';
          this.targetUrl = '';
          this.snack.open('Destination created', 'OK', { duration: 2500 });
          this.reload();
        },
        error: (err) => {
          this.saving.set(false);
          this.snack.open(err?.error?.message || 'Create failed', 'Dismiss', { duration: 4000 });
        },
      });
  }
}
