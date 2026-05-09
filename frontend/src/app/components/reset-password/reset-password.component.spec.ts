import { of } from 'rxjs';
import { AuthenticatedUser } from '../../models/authenticated-user';
import { MessageService } from '../../services/message.service';
import { RbacActivity } from '../../services/rbac/rbac.activity';
import { UserService } from '../../services/user.service';
import { UserSessionService } from '../../services/user-session.service';
import { PasswordFormValidationFields } from './password-form-validation-fields';
import { ResetPasswordComponent } from './reset-password.component';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let sessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let translocoSpy: jasmine.SpyObj<any>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<any>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  const createUser = (id?: number): AuthenticatedUser => ({
    id,
    username: 'admin'
  } as AuthenticatedUser);

  beforeEach(() => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['updatePassword']);
    sessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getUser']);
    translocoSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['open']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);

    translocoSpy.translate.and.returnValue('error message');

    component = new ResetPasswordComponent(
      userServiceSpy,
      sessionServiceSpy,
      translocoSpy,
      biitSnackbarServiceSpy,
      messageServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load loggedUser from session on ngOnInit', () => {
    const user = createUser(1);
    sessionServiceSpy.getUser.and.returnValue(user);
    component.user = createUser(1);

    component.ngOnInit();

    expect((component as any).loggedUser).toBe(user);
  });

  it('should generate password on ngOnInit when user has no id', () => {
    sessionServiceSpy.getUser.and.returnValue(undefined);
    component.user = createUser(undefined);
    spyOn(component as any, 'generatePassword').and.callThrough();

    component.ngOnInit();

    expect((component as any).generatePassword).toHaveBeenCalled();
  });

  it('should NOT generate password on ngOnInit when user already has id', () => {
    sessionServiceSpy.getUser.and.returnValue(undefined);
    component.user = createUser(5);
    spyOn(component as any, 'generatePassword');

    component.ngOnInit();

    expect((component as any).generatePassword).not.toHaveBeenCalled();
  });

  it('should generate password and set pwdVerification when generatePassword is called', () => {
    component.user = createUser(1);

    (component as any).generatePassword();

    expect((component as any).newPassword).toBeTruthy();
    expect((component as any).pwdVerification).toBe((component as any).newPassword);
  });

  it('should emit onClosed when close is called', () => {
    spyOn(component.closed, 'emit');

    component.close();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });

  it('should add OLD_PASSWORD_MANDATORY error when oldPassword is empty on validate', () => {
    (component as any).oldPassword = '';
    (component as any).newPassword = 'abc123';
    (component as any).pwdVerification = 'abc123';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(PasswordFormValidationFields.OLD_PASSWORD_MANDATORY)).toBeTrue();
  });

  it('should add PASSWORD_MANDATORY error when newPassword is empty on validate', () => {
    (component as any).oldPassword = 'old';
    (component as any).newPassword = '';
    (component as any).pwdVerification = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(PasswordFormValidationFields.PASSWORD_MANDATORY)).toBeTrue();
  });

  it('should add PASSWORD_MISMATCH error when passwords do not match on validate', () => {
    (component as any).oldPassword = 'old';
    (component as any).newPassword = 'newPass';
    (component as any).pwdVerification = 'differentPass';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(PasswordFormValidationFields.PASSWORD_MISMATCH)).toBeTrue();
  });

  it('should return true from validate when all fields are correct', () => {
    (component as any).oldPassword = 'oldPass';
    (component as any).newPassword = 'newPass1!';
    (component as any).pwdVerification = 'newPass1!';

    const result = (component as any).validate();

    expect(result).toBeTrue();
  });

  it('should call userService and close on changePassword when validation passes', () => {
    (component as any).oldPassword = 'oldPass';
    (component as any).newPassword = 'newPass1!';
    (component as any).pwdVerification = 'newPass1!';
    spyOn(component, 'close');
    userServiceSpy.updatePassword.and.returnValue(of(undefined));

    component.changePassword();

    expect(userServiceSpy.updatePassword).toHaveBeenCalledOnceWith('oldPass', 'newPass1!');
    expect(component.close).toHaveBeenCalled();
  });

  it('should NOT call userService when validation fails on changePassword', () => {
    (component as any).oldPassword = '';
    (component as any).newPassword = '';
    (component as any).pwdVerification = '';

    component.changePassword();

    expect(userServiceSpy.updatePassword).not.toHaveBeenCalled();
  });
});

