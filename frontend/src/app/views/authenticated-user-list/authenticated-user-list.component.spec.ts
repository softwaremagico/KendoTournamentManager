import {DatePipe} from '@angular/common';
import {of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {AuthenticatedUserListComponent} from './authenticated-user-list.component';
import {UserService} from '../../services/user.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {UserSessionService} from '../../services/user-session.service';
import {TranslocoService} from '@jsverse/transloco';
import {AuthenticatedUser} from '../../models/authenticated-user';
import {UserRoles} from '../../services/rbac/user-roles';

describe('AuthenticatedUserListComponent', () => {
  let component: AuthenticatedUserListComponent;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let systemOverloadServiceMock: SystemOverloadService;

  const createUser = (username: string): AuthenticatedUser => ({
    id: username.length,
    username,
    name: 'Name',
    lastname: 'Lastname',
    roles: [UserRoles.VIEWER],
    createdAt: new Date(),
    updatedAt: new Date()
  } as unknown as AuthenticatedUser);

  beforeEach(() => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getAll', 'delete']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getUser']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    systemOverloadServiceMock = {
      isTransactionalBusy: {
        next: jasmine.createSpy('next')
      }
    } as unknown as SystemOverloadService;

    (translocoServiceSpy.translate as jasmine.Spy).and.callFake((key: string) => key);
    (translocoServiceSpy.selectTranslate as jasmine.Spy).and.callFake((key: string) => of(`${key}-translated`));
    userServiceSpy.getAll.and.returnValue(of([]));

    component = new AuthenticatedUserListComponent(
      userServiceSpy,
      rbacServiceSpy,
      systemOverloadServiceMock,
      userSessionServiceSpy,
      translocoServiceSpy,
      biitSnackbarServiceSpy,
      new DatePipe('en-US')
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should create a datePipe transform function', () => {
    const transformed = component.datePipe().transform(0);

    expect(transformed).toBeTruthy();
  });

  it('should initialize columns and trigger loadData on ngAfterViewInit', () => {
    spyOn(component, 'loadData');

    component.ngAfterViewInit();

    expect((component as any).columns.length).toBe(9);
    expect(component.loadData).toHaveBeenCalled();
  });

  it('should load users and clone data on loadData', () => {
    const user = createUser('john');
    userServiceSpy.getAll.and.returnValue(of([user]));

    component.loadData();

    expect((systemOverloadServiceMock.isTransactionalBusy.next as jasmine.Spy)).toHaveBeenCalledWith(true);
    expect((component as any).loading).toBeFalse();
    expect((component as any).users.length).toBe(1);
    expect((component as any).users[0]).not.toBe(user);
    expect((systemOverloadServiceMock.isTransactionalBusy.next as jasmine.Spy)).toHaveBeenCalledWith(false);
  });

  it('should set target with default VIEWER role on addElement', () => {
    component.addElement();

    expect((component as any).target).toBeTruthy();
    expect((component as any).target.roles[0]).toBe(UserRoles.VIEWER);
  });

  it('should set target on editElement only when authenticated user exists', () => {
    const user = createUser('john');

    component.editElement(user);
    expect((component as any).target).toBe(user);

    component.editElement(null as unknown as AuthenticatedUser);
    expect((component as any).target).toBe(user);
  });

  it('should block deleting current user', () => {
    const current = createUser('self');
    userSessionServiceSpy.getUser.and.returnValue(current);

    component.deleteElement([current], true);

    expect(userServiceSpy.delete).not.toHaveBeenCalled();
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'youCannotDeleteYourself',
      NotificationType.WARNING
    );
  });

  it('should delete users and show success notification', () => {
    const user1 = createUser('u1');
    const user2 = createUser('u2');
    userSessionServiceSpy.getUser.and.returnValue(createUser('self'));
    userServiceSpy.delete.and.returnValues(of(user1), of(user2));
    spyOn(component, 'loadData');
    (component as any).confirm = true;

    component.deleteElement([user1, user2], true);

    expect(userServiceSpy.delete).toHaveBeenCalledTimes(2);
    expect((component as any).confirm).toBeFalse();
    expect(component.loadData).toHaveBeenCalled();
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'infoAuthenticatedUserDeleted-translated',
      NotificationType.SUCCESS
    );
  });

  it('should return user names as comma separated string', () => {
    const value = component.getUserNames([createUser('a'), createUser('b')]);

    expect(value).toBe('a, b');
    expect(component.getUserNames(undefined as unknown as AuthenticatedUser[])).toBe('');
  });

  it('should notify and reload on onSaved', () => {
    spyOn(component, 'loadData');
    (component as any).target = createUser('x');

    component.onSaved(createUser('y'));

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'infoAuthenticatedUserStored',
      NotificationType.INFO
    );
    expect(component.loadData).toHaveBeenCalled();
    expect((component as any).target).toBeNull();
  });
});


