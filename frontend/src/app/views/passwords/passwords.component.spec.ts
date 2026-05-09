import {of} from 'rxjs';
import {confirmPasswordValidator, MyErrorStateMatcher, PasswordsComponent} from './passwords.component';
import {UserService} from '../../services/user.service';
import {MessageService} from '../../services/message.service';
import {RbacService} from '../../services/rbac/rbac.service';

describe('PasswordsComponent', () => {
  let component: PasswordsComponent;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  beforeEach(() => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['updatePassword']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new PasswordsComponent(userServiceSpy, messageServiceSpy, rbacServiceSpy);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should mark all form controls as touched on init', () => {
    component.ngOnInit();

    expect(component.passwordForm.get('oldPassword')?.touched).toBeTrue();
    expect(component.passwordForm.get('newPassword')?.touched).toBeTrue();
    expect(component.passwordForm.get('repeatPassword')?.touched).toBeTrue();
  });

  it('should call updatePassword and show info message on changePassword', () => {
    component.oldPassword = 'Old1234';
    component.newPassword = 'New1234';
    userServiceSpy.updatePassword.and.returnValue(of({} as any));

    component.changePassword();

    expect(userServiceSpy.updatePassword).toHaveBeenCalledOnceWith('Old1234', 'New1234');
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledOnceWith('infoAuthenticatedUserUpdated');
  });

  it('should return invalid controls when form is empty', () => {
    const invalid = component.findInvalidControls();

    expect(invalid).toEqual(['oldPassword', 'newPassword', 'repeatPassword']);
  });

  it('should return no invalid controls when form is valid', () => {
    component.passwordForm.patchValue({
      oldPassword: 'Old1234',
      newPassword: 'New1234',
      repeatPassword: 'New1234'
    });

    const invalid = component.findInvalidControls();

    expect(invalid).toEqual([]);
  });

  it('should return true from passwordMatch only when password is visible and both match', () => {
    component.hidePassword = false;
    component.newPassword = 'abc';
    component.repeatedPassword = 'abc';

    expect(component.passwordMatch()).toBeTrue();

    component.hidePassword = true;
    expect(component.passwordMatch()).toBeFalse();
  });
});

describe('confirmPasswordValidator', () => {
  it('should return null when newPassword and repeatPassword are equal', () => {
    const component = new PasswordsComponent(
      jasmine.createSpyObj('UserService', ['updatePassword']),
      jasmine.createSpyObj('MessageService', ['infoMessage']),
      jasmine.createSpyObj('RbacService', ['isAllowed'])
    );

    component.passwordForm.patchValue({
      newPassword: 'New1234',
      repeatPassword: 'New1234'
    });

    expect(confirmPasswordValidator(component.passwordForm)).toBeNull();
  });

  it('should return repeatPassword error when values are different', () => {
    const component = new PasswordsComponent(
      jasmine.createSpyObj('UserService', ['updatePassword']),
      jasmine.createSpyObj('MessageService', ['infoMessage']),
      jasmine.createSpyObj('RbacService', ['isAllowed'])
    );

    component.passwordForm.patchValue({
      newPassword: 'New1234',
      repeatPassword: 'Other1234'
    });

    expect(confirmPasswordValidator(component.passwordForm)).toEqual({ repeatPassword: true });
  });
});

describe('MyErrorStateMatcher', () => {
  it('should return true when control is invalid and parent is dirty', () => {
    const matcher = new MyErrorStateMatcher();
    const component = new PasswordsComponent(
      jasmine.createSpyObj('UserService', ['updatePassword']),
      jasmine.createSpyObj('MessageService', ['infoMessage']),
      jasmine.createSpyObj('RbacService', ['isAllowed'])
    );

    component.passwordForm.patchValue({
      oldPassword: '',
      newPassword: 'short',
      repeatPassword: ''
    });
    component.passwordForm.markAsDirty();

    const newPasswordControl = component.passwordForm.get('newPassword') as any;

    expect(matcher.isErrorState(newPasswordControl, null)).toBeTrue();
  });
});

