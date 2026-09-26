import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EnvironmentService} from '../environment.service';

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

  create(request: CreateTenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.url, request);
  }

  update(tenant: Tenant): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.url}/${tenant.id}`, {name: tenant.name, active: tenant.active});
  }
}
