import {ChangeDetectorRef} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {of, throwError} from 'rxjs';
import {MessageService} from '../../services/message.service';
import {UserRoles} from '../../services/rbac/user-roles';
import {Tenant, TenantService} from '../../services/tenant.service';
import {UserSessionService} from '../../services/user-session.service';
import {TenantListComponent} from './tenant-list.component';

describe('TenantListComponent', () => {
  let tenants: jasmine.SpyObj<TenantService>;
  let session: jasmine.SpyObj<UserSessionService>;
  let messages: jasmine.SpyObj<MessageService>;
  let cdr: jasmine.SpyObj<ChangeDetectorRef>;
  let component: TenantListComponent;
  const tenant: Tenant = {id: 7, name: 'Kendo', active: true};

  beforeEach(() => {
    tenants = jasmine.createSpyObj<TenantService>('TenantService', [
      'getAll', 'create', 'update', 'delete', 'getUsers', 'assignUsers', 'unassignUsers'
    ]);
    tenants.getAll.and.returnValue(of([tenant]));
    session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['getUser']);
    const translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    (translate.translate.and as any).callFake((key: string) => key);
    cdr = jasmine.createSpyObj<ChangeDetectorRef>('ChangeDetectorRef', ['detectChanges', 'markForCheck']);
    messages = jasmine.createSpyObj<MessageService>('MessageService', ['infoMessage', 'warningMessage']);
    component = new TenantListComponent(tenants, session, translate, cdr, messages);
    component.assignedCell = {first: undefined} as any;
  });

  it('does not load tenants for a non-super-admin', () => {
    session.getUser.and.returnValue(undefined);
    component.ngOnInit();
    expect(component.error).toContain('permisos');
    expect(tenants.getAll).not.toHaveBeenCalled();
  });

  it('loads tenants and builds columns for a super-admin', () => {
    session.getUser.and.returnValue({roles: [UserRoles.SUPER_ADMIN]} as any);
    component.ngOnInit();
    component.ngAfterViewInit();
    expect(tenants.getAll).toHaveBeenCalled();
    expect(component.tenants).toEqual([tenant]);
    expect(component.columns).toHaveSize(2);
    expect(component.userColumns).toHaveSize(4);
    expect(cdr.detectChanges).toHaveBeenCalled();
  });

  it('creates, edits and deletes a tenant and reloads the list', () => {
    tenants.create.and.returnValue(of(tenant));
    tenants.update.and.returnValue(of(tenant));
    tenants.delete.and.returnValue(of(undefined));
    component.openCreate();
    expect(component.createPopup).toBeTrue();
    component.request.tenant = 'Kendo';
    component.create();
    expect(tenants.create).toHaveBeenCalled();
    expect(component.createPopup).toBeFalse();
    component.openEdit(tenant);
    expect(component.target).toEqual(tenant);
    expect(component.target).not.toBe(tenant);
    component.save(tenant);
    expect(component.editPopup).toBeFalse();
    component.openDelete(tenant);
    expect(component.confirmDelete).toBeTrue();
    component.delete(tenant);
    expect(tenants.delete).toHaveBeenCalledWith(7);
    expect(component.confirmDelete).toBeFalse();
    expect(tenants.getAll).toHaveBeenCalledTimes(3);
  });

  it('reports a failed tenant update', () => {
    tenants.update.and.returnValue(throwError(() => new Error('offline')));
    component.save(tenant);
    expect(component.error).toBe('No se pudo actualizar el tenant.');
  });

  it('deduplicates users and updates tenant membership', () => {
    tenants.getUsers.and.returnValue(of([
      {username: 'aki', tenantId: 1},
      {username: 'aki', tenantId: 7},
      {username: 'bea', tenantId: 1}
    ] as any));
    tenants.assignUsers.and.returnValue(of(undefined));
    tenants.unassignUsers.and.returnValue(of(undefined));
    component.openUsers(tenant);
    expect(component.users.map(user => user.username)).toEqual(['aki', 'bea']);
    expect(component.users.map(user => user.assigned)).toEqual([true, false]);
    component.assignUsers(component.users);
    expect(tenants.assignUsers).toHaveBeenCalledWith(7, ['bea']);
    component.unassignUsers(component.users);
    expect(tenants.unassignUsers).toHaveBeenCalledWith(7, ['aki', 'bea']);
    expect(messages.infoMessage).toHaveBeenCalledWith('tenantUsersUnassigned');
  });
});
