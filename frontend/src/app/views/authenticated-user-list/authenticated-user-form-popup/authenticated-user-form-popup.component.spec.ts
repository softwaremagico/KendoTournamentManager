import {EventEmitter} from '@angular/core';
import {AuthenticatedUserFormPopupComponent} from './authenticated-user-form-popup.component';
import {TranslocoService} from '@ngneat/transloco';
import {AuthenticatedUser} from '../../../models/authenticated-user';

describe('AuthenticatedUserFormPopupComponent', () => {
  let component: AuthenticatedUserFormPopupComponent;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;

  beforeEach(() => {
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    component = new AuthenticatedUserFormPopupComponent(translocoServiceSpy);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize output event emitters', () => {
    expect(component.closed instanceof EventEmitter).toBeTrue();
    expect(component.saved instanceof EventEmitter).toBeTrue();
    expect(component.errorEvent instanceof EventEmitter).toBeTrue();
  });

  it('should initialize errors map as empty', () => {
    expect((component as any).errors.size).toBe(0);
  });

  it('should keep assigned user input', () => {
    const user = {
      id: 1,
      username: 'john',
      roles: []
    } as unknown as AuthenticatedUser;

    component.user = user;

    expect(component.user).toBe(user);
  });
});

