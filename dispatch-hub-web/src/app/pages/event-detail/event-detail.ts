import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import {
  ApiService,
  DeliveryAttempt,
  EventRecord,
  FailureSummary,
  JobSummary,
} from '../../core/api.service';
import { AuthService } from '../../core/auth.service';

interface JobView {
  job: JobSummary;
  attempts: DeliveryAttempt[];
  summary: FailureSummary | null;
  loadingAttempts: boolean;
  loadingSummary: boolean;
  retrying: boolean;
}

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
  ],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.scss',
})
export class EventDetailPage implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly event = signal<EventRecord | null>(null);
  readonly jobViews = signal<JobView[]>([]);
  readonly attemptColumns = ['attemptNumber', 'httpStatus', 'durationMs', 'errorMessage', 'createdAt'];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.load(id);
  }

  load(eventId: string): void {
    this.loading.set(true);
    this.api.getEvent(eventId).subscribe({
      next: (ev) => {
        this.event.set(ev);
        const views: JobView[] = (ev.jobs || []).map((job) => ({
          job,
          attempts: [],
          summary: null,
          loadingAttempts: true,
          loadingSummary: false,
          retrying: false,
        }));
        this.jobViews.set(views);
        this.loading.set(false);
        views.forEach((v, idx) => this.loadAttempts(idx));
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message || 'Failed to load event', 'Dismiss', {
          duration: 4000,
        });
      },
    });
  }

  private loadAttempts(index: number): void {
    const views = [...this.jobViews()];
    const view = views[index];
    if (!view) {
      return;
    }
    this.api.listAttempts(view.job.id).subscribe({
      next: (attempts) => {
        const next = [...this.jobViews()];
        next[index] = { ...next[index], attempts, loadingAttempts: false };
        this.jobViews.set(next);
      },
      error: () => {
        const next = [...this.jobViews()];
        next[index] = { ...next[index], loadingAttempts: false };
        this.jobViews.set(next);
      },
    });
  }

  retry(index: number): void {
    if (!this.auth.isAdmin) {
      this.snack.open('Admin role required to retry', 'Dismiss', { duration: 3500 });
      return;
    }
    const view = this.jobViews()[index];
    if (!view) {
      return;
    }
    const next = [...this.jobViews()];
    next[index] = { ...view, retrying: true };
    this.jobViews.set(next);
    this.api.retryJob(view.job.id).subscribe({
      next: () => {
        const updated = [...this.jobViews()];
        updated[index] = { ...updated[index], retrying: false };
        this.jobViews.set(updated);
        this.snack.open('Retry accepted', 'OK', { duration: 2500 });
        const ev = this.event();
        if (ev) {
          this.load(ev.id);
        }
      },
      error: (err) => {
        const updated = [...this.jobViews()];
        updated[index] = { ...updated[index], retrying: false };
        this.jobViews.set(updated);
        this.snack.open(err?.error?.message || 'Retry failed', 'Dismiss', { duration: 4000 });
      },
    });
  }

  aiSummary(index: number): void {
    const view = this.jobViews()[index];
    if (!view) {
      return;
    }
    const next = [...this.jobViews()];
    next[index] = { ...view, loadingSummary: true };
    this.jobViews.set(next);
    this.api.aiSummary(view.job.id).subscribe({
      next: (summary) => {
        const updated = [...this.jobViews()];
        updated[index] = { ...updated[index], summary, loadingSummary: false };
        this.jobViews.set(updated);
      },
      error: (err) => {
        const updated = [...this.jobViews()];
        updated[index] = { ...updated[index], loadingSummary: false };
        this.jobViews.set(updated);
        this.snack.open(err?.error?.message || 'AI summary failed', 'Dismiss', { duration: 4000 });
      },
    });
  }
}
