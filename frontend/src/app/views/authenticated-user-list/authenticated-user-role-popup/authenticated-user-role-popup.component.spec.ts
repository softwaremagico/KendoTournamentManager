import {of} from 'rxjs';
import {AuthenticatedUserRolePopupComponent} from './authenticated-user-role-popup.component';
import {RbacService} from '../../../services/rbac/rbac.service';
import {TranslocoService} from '@jsverse/transloco';
import {UserService} from '../../../services/user.service';
import {MessageService} from '../../../services/message.service';
import {UserRoles} from '../../../services/rbac/user-roles';

describe('AuthenticatedUserRolePopupComponent', () => {
  let component: AuthenticatedUserRolePopupComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);

    (translocoServiceSpy.translate as jasmine.Spy).and.callFake((key: string) => key);

    component = new AuthenticatedUserRolePopupComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      userServiceSpy,
      messageServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should translate all roles on init and set selected role from user', () => {
    component.user = {
      username: 'john',
      roles: [UserRoles.EDITOR]
    } as any;

    component.ngOnInit();

    expect((component as any).translatedRoles.length).toBe(UserRoles.toArray().length);
    expect(component.selectedRole).toBe(UserRoles.EDITOR);
    expect(translocoServiceSpy.translate).toHaveBeenCalled();
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.closed, 'emit');

    component.closeDialog();

    expect(component.closed.emit).toHaveBeenCalled();
  });

  it('should update user role and emit onSaved when saveAction succeeds', () => {
    const user = {
      username: 'john',
      roles: [UserRoles.VIEWER]
    } as any;
    component.user = user;
    component.selectedRole = UserRoles.ADMIN;
    userServiceSpy.update.and.returnValue(of(user));
    spyOn(component.saved, 'emit');

    component.saveAction();

    expect(user.roles).toEqual([UserRoles.ADMIN]);
    expect(userServiceSpy.update).toHaveBeenCalledOnceWith(component.user as any);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledOnceWith('roleChanged');
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should not call update when user is undefined', () => {
    component.user = null;
    component.selectedRole = UserRoles.ADMIN;

    component.saveAction();

    expect(userServiceSpy.update).not.toHaveBeenCalled();
  });

  it('should not call update when selected role is invalid', () => {
    component.user = {
      username: 'john',
      roles: [UserRoles.VIEWER]
    } as any;
    component.selectedRole = 'INVALID_ROLE';

    component.saveAction();

    expect(userServiceSpy.update).not.toHaveBeenCalled();
  });
});

