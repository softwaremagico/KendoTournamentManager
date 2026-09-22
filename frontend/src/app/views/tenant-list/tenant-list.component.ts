import {Component, OnInit} from '@angular/core';
import {CreateTenantRequest, Tenant, TenantService} from '../../services/tenant.service';
import {UserSessionService} from '../../services/user-session.service';
import {UserRoles} from '../../services/rbac/user-roles';

@Component({
  standalone: false,
  selector: 'app-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.scss']
})
export class TenantListComponent implements OnInit {
  tenants: Tenant[] = [];
  error = '';
  readonly request: CreateTenantRequest = {tenant: '', username: '', password: '', name: '', lastname: ''};

  constructor(private readonly tenantService: TenantService, private readonly userSessionService: UserSessionService) {
  }

  ngOnInit(): void {
    if (!this.isSuperAdmin()) {
      this.error = 'No tienes permisos para administrar tenants.';
      return;
    }
    this.load();
  }

  create(): void {
    this.tenantService.create(this.request).subscribe({
      next: () => {
        this.request.tenant = '';
        this.request.username = '';
        this.request.password = '';
        this.request.name = '';
        this.request.lastname = '';
        this.load();
      },
      error: () => this.error = 'No se pudo crear el tenant.'
    });
  }

  save(tenant: Tenant): void {
    this.tenantService.update(tenant).subscribe({
      next: () => this.load(),
      error: () => this.error = 'No se pudo actualizar el tenant.'
    });
  }

  isSuperAdmin(): boolean {
    return this.userSessionService.getUser()?.roles?.includes(UserRoles.SUPER_ADMIN) ?? false;
  }

  private load(): void {
    this.error = '';
    this.tenantService.getAll().subscribe({
      next: tenants => this.tenants = tenants,
      error: () => this.error = 'No se pudieron cargar los tenants.'
    });
  }
}
