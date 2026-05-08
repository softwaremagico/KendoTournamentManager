import { of } from 'rxjs';
import { NotificationType } from '@biit-solutions/wizardry-theme/info';
import { AuthenticatedUserFormComponent } from './authenticated-user-form.component';
import { RbacService } from '../../services/rbac/rbac.service';
import { TranslocoService } from '@ngneat/transloco';
import { BiitSnackbarService } from '@biit-solutions/wizardry-theme/info';
import { UserService } from '../../services/user.service';
import { UserSessionService } from '../../services/user-session.service';
import { ActivityService } from '../../services/rbac/activity.service';
import { AuthenticatedUser } from '../../models/authenticated-user';
import { AuthenticatedUserFormValidationFields } from './authenticated-user-form-validation-fields';

describe('AuthenticatedUserFormComponent', () => {
  let component: AuthenticatedUserFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let sessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let activityServiceSpy: jasmine.SpyObj<ActivityService>;

  const buildValidUser = (id?: number): AuthenticatedUser => ({
    id,
    username: 'john.doe',
    name: 'John',
    lastname: 'Doe',
    password: 'SecurePass123',
    roles: []
  } as unknown as AuthenticatedUser);

  const loggedUser = buildValidUser(99);

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['add', 'update', 'updatePassword', 'updateUserPassword']);
    sessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getUser']);
    activityServiceSpy = jasmine.createSpyObj('ActivityService', ['isAllowed']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    activityServiceSpy.isAllowed.and.returnValue(false);
    translocoServiceSpy.translate.and.returnValue('validation error');
    sessionServiceSpy.getUser.and.returnValue(loggedUser);

    component = new AuthenticatedUserFormComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      biitSnackbarServiceSpy,
      userServiceSpy,
      sessionServiceSpy,
      activityServiceSpy
    );
    component.user = buildValidUser(1);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load the logged user in ngOnInit', () => {
    component.ngOnInit();

    expect(sessionServiceSpy.getUser).toHaveBeenCalled();
    expect((component as any).loggedUser).toEqual(loggedUser);
  });

  it('should generate password on ngOnInit for new user', () => {
    component.user = buildValidUser(undefined);

    component.ngOnInit();

    expect(component.user.password).toBeTruthy();
    expect((component as any).pwdVerification).toBe(component.user.password);
  });

  it('should not generate password on ngOnInit for existing user', () => {
    component.user = buildValidUser(5);
    component.user.password = 'existing-password';

    component.ngOnInit();

    expect(component.user.password).toBe('existing-password');
  });

  it('should validate successfully with valid user data', () => {
    component.user = buildValidUser(5);
    component.user.password = undefined as any;
    (component as any).pwdVerification = '';

    const result = (component as any).validate();

    expect(result).toBeTrue();
    expect((component as any).errors.size).toBe(0);
  });

  it('should fail validation when username has spaces', () => {
    component.user = buildValidUser(1);
    component.user.username = 'john doe';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(AuthenticatedUserFormValidationFields.USERNAME_INVALID)).toBeTrue();
  });

  it('should fail validation when name is missing', () => {
    component.user = buildValidUser(1);
    component.user.name = undefined as any;

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(AuthenticatedUserFormValidationFields.NAME_MANDATORY)).toBeTrue();
  });

  it('should fail validation when lastname is missing', () => {
    component.user = buildValidUser(1);
    component.user.lastname = undefined as any;

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(AuthenticatedUserFormValidationFields.LASTNAME_MANDATORY)).toBeTrue();
  });

  it('should fail validation when new user has no password', () => {
    component.user = buildValidUser(undefined);
    component.user.password = undefined as any;

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(AuthenticatedUserFormValidationFields.PASSWORD_MANDATORY)).toBeTrue();
  });

  it('should fail validation when password verification does not match for new user', () => {
    component.user = buildValidUser(undefined);
    component.user.password = 'Password1';
    (component as any).pwdVerification = 'DifferentPassword';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(AuthenticatedUserFormValidationFields.PASSWORD_MISMATCH)).toBeTrue();
  });

  it('should call update service when user has id on onSave', () => {
    component.user = buildValidUser(5);
    component.user.password = undefined as any;
    (component as any).pwdVerification = '';
    userServiceSpy.update.and.returnValue(of(component.user));
    spyOn(component.onSaved, 'emit');

    (component as any).onSave();

    expect(userServiceSpy.update).toHaveBeenCalledWith(component.user);
    expect(component.onSaved.emit).toHaveBeenCalled();
  });

  it('should call add service when user has no id on onSave', () => {
    component.user = buildValidUser(undefined);
    component.user.password = 'SecurePass123';
    (component as any).pwdVerification = 'SecurePass123';
    userServiceSpy.add.and.returnValue(of(component.user));
    spyOn(component.onSaved, 'emit');

    (component as any).onSave();

    expect(userServiceSpy.add).toHaveBeenCalledWith(component.user);
    expect(component.onSaved.emit).toHaveBeenCalled();
  });

  it('should show warning notification when validation fails on onSave', () => {
    component.user = buildValidUser(1);
    component.user.name = undefined as any;

    (component as any).onSave();

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'validation error',
      NotificationType.WARNING
    );
    expect(userServiceSpy.update).not.toHaveBeenCalled();
  });

  it('should generate password and align pwdVerification', () => {
    component.user = buildValidUser(undefined);
    component.user.password = undefined as any;

    (component as any).generatePassword();

    expect(component.user.password).toBeTruthy();
    expect((component as any).pwdVerification).toBe(component.user.password);
  });
});



