import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface LoginRequest {
  username: string;
  password: string;
  tenantCode: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  tenantId: string;
  tenantCode: string;
  role: string;
}

export interface Destination {
  id: string;
  name: string;
  targetUrl: string;
  hasSecret: boolean;
  enabled: boolean;
}

export interface CreateDestinationRequest {
  name: string;
  targetUrl: string;
  secret?: string;
  enabled?: boolean;
}

export interface JobSummary {
  id: string;
  destinationId: string;
  status: string;
  attemptCount: number;
}

export interface EventRecord {
  id: string;
  idempotencyKey: string;
  payload: string;
  createdAt: string;
  jobs: JobSummary[];
}

export interface SubmitEventRequest {
  idempotencyKey: string;
  payload: Record<string, unknown>;
  destinationId?: string;
}

export interface DeliveryAttempt {
  id: string;
  attemptNumber: number;
  httpStatus: number | null;
  durationMs: number;
  errorMessage: string | null;
  createdAt: string;
}

export interface FailureSummary {
  explanation: string;
  suggestedAction: string;
  aiGenerated: boolean;
  provider: string;
}

export interface OpsSummary {
  totalJobs: number;
  pending: number;
  running: number;
  success: number;
  failed: number;
  dead: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly base = environment.apiBaseUrl;

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/api/v1/auth/login`, body);
  }

  listDestinations(): Observable<Destination[]> {
    const tenantId = this.auth.requireTenantId();
    return this.http.get<Destination[]>(`${this.base}/api/v1/tenants/${tenantId}/destinations`);
  }

  createDestination(body: CreateDestinationRequest): Observable<Destination> {
    const tenantId = this.auth.requireTenantId();
    return this.http.post<Destination>(`${this.base}/api/v1/tenants/${tenantId}/destinations`, body);
  }

  listEvents(): Observable<EventRecord[]> {
    const tenantId = this.auth.requireTenantId();
    return this.http.get<EventRecord[]>(`${this.base}/api/v1/tenants/${tenantId}/events`);
  }

  getEvent(eventId: string): Observable<EventRecord> {
    const tenantId = this.auth.requireTenantId();
    return this.http.get<EventRecord>(`${this.base}/api/v1/tenants/${tenantId}/events/${eventId}`);
  }

  submitEvent(body: SubmitEventRequest): Observable<EventRecord> {
    const tenantId = this.auth.requireTenantId();
    return this.http.post<EventRecord>(`${this.base}/api/v1/tenants/${tenantId}/events`, body);
  }

  listAttempts(jobId: string): Observable<DeliveryAttempt[]> {
    const tenantId = this.auth.requireTenantId();
    return this.http.get<DeliveryAttempt[]>(
      `${this.base}/api/v1/tenants/${tenantId}/jobs/${jobId}/attempts`,
    );
  }

  retryJob(jobId: string): Observable<void> {
    const tenantId = this.auth.requireTenantId();
    return this.http.post<void>(`${this.base}/api/v1/tenants/${tenantId}/jobs/${jobId}/retry`, {});
  }

  aiSummary(jobId: string): Observable<FailureSummary> {
    const tenantId = this.auth.requireTenantId();
    return this.http.post<FailureSummary>(
      `${this.base}/api/v1/tenants/${tenantId}/jobs/${jobId}/ai-summary`,
      {},
    );
  }

  opsSummary(): Observable<OpsSummary> {
    const tenantId = this.auth.requireTenantId();
    return this.http.get<OpsSummary>(`${this.base}/api/v1/tenants/${tenantId}/ops-summary`);
  }
}
