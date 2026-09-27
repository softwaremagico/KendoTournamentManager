import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CreateTenantRequest, Tenant, TenantService} from '../../services/tenant.service';
import {UserSessionService} from '../../services/user-session.service';
import {UserRoles} from '../../services/rbac/user-roles';
import {DatatableColumn} from '@biit-solutions/wizardry-theme/table';
import {TranslocoService} from '@jsverse/transloco';
import {Type} from '@biit-solutions/wizardry-theme/inputs';
import {AuthenticatedUser} from '../../models/authenticated-user';

type TenantUser = AuthenticatedUser & { assigned: boolean };

@Component({
  standalone: false,
  selector: 'app-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.scss']
})
export class TenantListComponent implements OnInit {
  protected readonly Type = Type;
  tenants: Tenant[] = [];
  error = '';
  request: CreateTenantRequest = this.emptyRequest();
  columns: DatatableColumn[] = [];
  createPopup = false;
  editPopup = false;
  confirmDelete = false;
  usersPopup = false;
  target: Tenant | null = null;
  users: TenantUser[] = [];
  userColumns: DatatableColumn[] = [];

  constructor(private readonly tenantService: TenantService, private readonly userSessionService: UserSessionService,
              private readonly transloco: TranslocoService, private readonly cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    if (!this.isSuperAdmin()) {
      this.error = 'No tienes permisos para administrar tenants.';
      return;
    }
    this.columns = [
      new DatatableColumn(this.transloco.translate('tenantName'), 'name'),
      new DatatableColumn(this.transloco.translate('active'), 'active')
    ];
    this.userColumns = [
      new DatatableColumn(this.transloco.translate('username'), 'username'),
      new DatatableColumn(this.transloco.translate('name'), 'name'),
      new DatatableColumn(this.transloco.translate('lastname'), 'lastname'),
      new DatatableColumn(this.transloco.translate('assigned'), 'assigned')
    ];
    this.load();
  }

  create(): void {
    this.tenantService.create(this.request).subscribe({
      next: () => {
        this.request = this.emptyRequest();
        this.createPopup = false;
        this.load();
      },
      error: () => this.error = 'No se pudo crear el tenant.'
    });
  }

  save(tenant: Tenant): void {
    this.tenantService.update(tenant).subscribe({
      next: () => {
        this.editPopup = false;
        this.target = null;
        this.load();
      },
      error: () => this.error = 'No se pudo actualizar el tenant.'
    });
  }

  delete(tenant: Tenant): void {
    this.tenantService.delete(tenant.id).subscribe({
      next: () => {
        this.confirmDelete = false;
        this.target = null;
        this.load();
      },
      error: () => this.error = 'No se pudo eliminar el tenant.'
    });
  }

  isSuperAdmin(): boolean {
    return this.userSessionService.getUser()?.roles?.includes(UserRoles.SUPER_ADMIN) ?? false;
  }

  openCreate(): void {
    this.request = this.emptyRequest();
    this.createPopup = true;
  }

  openEdit(tenant: Tenant): void {
    this.target = {...tenant};
    this.editPopup = true;
  }

  openDelete(tenant: Tenant): void {
    this.target = tenant;
    this.confirmDelete = true;
  }

  openUsers(tenant: Tenant): void {
    this.target = tenant;
    this.users = [];
    this.usersPopup = true;
    this.tenantService.getUsers(tenant.id).subscribe({
      next: users => {
        const usersByUsername = new Map<string, TenantUser>();
        users.forEach(user => {
          const tenantUser = usersByUsername.get(user.username);
          if (tenantUser) {
            tenantUser.assigned ||= user.tenantId === tenant.id;
          } else {
            usersByUsername.set(user.username, {...user, assigned: user.tenantId === tenant.id});
          }
        });
        this.users = [...usersByUsername.values()].sort((first, second) =>
          first.username.localeCompare(second.username));
        this.cdr.detectChanges();
      },
      error: () => this.error = 'No se pudieron cargar los usuarios.'
    });
  }

  assignUsers(users: TenantUser[]): void {
    const unassignedUsers = users.filter(user => !user.assigned);
    if (!this.target || unassignedUsers.length === 0) {
      return;
    }
    this.tenantService.assignUsers(this.target.id, unassignedUsers.map(user => user.username)).subscribe({
      next: () => {
        unassignedUsers.forEach(user => user.assigned = true);
        this.users = [...this.users];
        this.cdr.markForCheck();
      },
      error: () => this.error = 'No se pudieron asignar los usuarios.'
    });
  }

  unassignUsers(users: TenantUser[]): void {
    const assignedUsers = users.filter(user => user.assigned);
    if (!this.target || assignedUsers.length === 0) {
      return;
    }
    this.tenantService.unassignUsers(this.target.id, assignedUsers.map(user => user.username)).subscribe({
      next: () => {
        assignedUsers.forEach(user => user.assigned = false);
        this.users = [...this.users];
        this.cdr.markForCheck();
      },
      error: () => this.error = 'No se pudieron desasignar los usuarios.'
    });
  }

  private emptyRequest(): CreateTenantRequest {
    return {tenant: '', username: '', password: '', name: '', lastname: ''};
  }

  private load(): void {
    this.error = '';
    this.tenantService.getAll().subscribe({
      next: tenants => {
        this.tenants = [...tenants];
        this.cdr.markForCheck();
      },
      error: () => this.error = 'No se pudieron cargar los tenants.'
    });
  }
}
