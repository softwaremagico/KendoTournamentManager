import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {AuthenticatedUser} from '../models/authenticated-user';

export interface Tenant {
  id: number;
  name: string;
  active: boolean;
}

export interface CreateTenantRequest {
  tenant: string;
  username: string;
  password: string;
  name?: string;
  lastname?: string;
}

@Injectable({providedIn: 'root'})
export class TenantService {
  private readonly url: string = this.environmentService.getBackendUrl() + '/auth/tenants';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService) {
  }

  getAll(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(this.url);
  }

  getActiveNames(): Observable<string[]> {
    return this.http.get<string[]>(this.environmentService.getBackendUrl() + '/auth/public/tenants');
  }

  getAvailable(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(`${this.url}/available`);
  }

  create(request: CreateTenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.url, request);
  }

  update(tenant: Tenant): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.url}/${tenant.id}`, {name: tenant.name, active: tenant.active});
  }

  delete(tenantId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${tenantId}`);
  }

  getUsers(tenantId: number): Observable<AuthenticatedUser[]> {
    return this.http.get<AuthenticatedUser[]>(`${this.url}/${tenantId}/users`);
  }

  assignUsers(tenantId: number, usernames: string[]): Observable<void> {
    return this.http.post<void>(`${this.url}/${tenantId}/users`, {usernames});
  }

  unassignUsers(tenantId: number, usernames: string[]): Observable<void> {
    return this.http.delete<void>(`${this.url}/${tenantId}/users`, {body: {usernames}});
  }
}
