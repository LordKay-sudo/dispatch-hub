import { Injectable, signal } from '@angular/core';

const TOKEN_KEY = 'dh_token';
const TENANT_KEY = 'dh_tenantId';
const ROLE_KEY = 'dh_role';
const USER_KEY = 'dh_username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly token = signal<string | null>(sessionStorage.getItem(TOKEN_KEY));
  readonly tenantId = signal<string | null>(sessionStorage.getItem(TENANT_KEY));
  readonly role = signal<string | null>(sessionStorage.getItem(ROLE_KEY));
  readonly username = signal<string | null>(sessionStorage.getItem(USER_KEY));

  get isLoggedIn(): boolean {
    return !!this.token();
  }

  get isAdmin(): boolean {
    return this.role() === 'ADMIN';
  }

  login(session: {
    accessToken: string;
    tenantId: string;
    role: string;
    username: string;
  }): void {
    sessionStorage.setItem(TOKEN_KEY, session.accessToken);
    sessionStorage.setItem(TENANT_KEY, session.tenantId);
    sessionStorage.setItem(ROLE_KEY, session.role);
    sessionStorage.setItem(USER_KEY, session.username);
    this.token.set(session.accessToken);
    this.tenantId.set(session.tenantId);
    this.role.set(session.role);
    this.username.set(session.username);
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(TENANT_KEY);
    sessionStorage.removeItem(ROLE_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.tenantId.set(null);
    this.role.set(null);
    this.username.set(null);
  }

  requireTenantId(): string {
    const id = this.tenantId();
    if (!id) {
      throw new Error('Not authenticated');
    }
    return id;
  }
}
