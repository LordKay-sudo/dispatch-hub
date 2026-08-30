import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ApiService, Destination, EventRecord, OpsSummary } from '../../core/api.service';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
  ],
  templateUrl: './events.html',
  styleUrl: './events.scss',
})
export class EventsPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly submitting = signal(false);
  readonly rows = signal<EventRecord[]>([]);
  readonly destinations = signal<Destination[]>([]);
  readonly summary = signal<OpsSummary | null>(null);
  readonly columns = ['createdAt', 'idempotencyKey', 'jobs', 'actions'];

  idempotencyKey = '';
  payloadText = '{\n  "hello": "world"\n}';
  destinationId = '';

  ngOnInit(): void {
    this.reload();
    this.api.listDestinations().subscribe({
      next: (list) => this.destinations.set(list),
      error: () => undefined,
    });
  }

  reload(): void {
    this.loading.set(true);
    this.api.listEvents().subscribe({
      next: (list) => {
        this.rows.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message || 'Failed to load events', 'Dismiss', {
          duration: 4000,
        });
      },
    });
    this.api.opsSummary().subscribe({
      next: (s) => this.summary.set(s),
      error: () => this.summary.set(null),
    });
  }

  submit(): void {
    if (!this.idempotencyKey.trim()) {
      this.snack.open('Idempotency key is required', 'Dismiss', { duration: 3500 });
      return;
    }
    let payload: Record<string, unknown>;
    try {
      payload = JSON.parse(this.payloadText) as Record<string, unknown>;
      if (payload === null || Array.isArray(payload) || typeof payload !== 'object') {
        throw new Error('Payload must be a JSON object');
      }
    } catch (e) {
      this.snack.open(e instanceof Error ? e.message : 'Invalid JSON payload', 'Dismiss', {
        duration: 4000,
      });
      return;
    }

    this.submitting.set(true);
    this.api
      .submitEvent({
        idempotencyKey: this.idempotencyKey.trim(),
        payload,
        destinationId: this.destinationId || undefined,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.idempotencyKey = '';
          this.snack.open('Event submitted', 'OK', { duration: 2500 });
          this.reload();
        },
        error: (err) => {
          this.submitting.set(false);
          this.snack.open(err?.error?.message || 'Submit failed', 'Dismiss', { duration: 4000 });
        },
      });
  }
}
