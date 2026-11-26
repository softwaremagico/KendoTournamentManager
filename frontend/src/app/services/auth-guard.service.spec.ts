import {Router} from '@angular/router';
import {AuthGuardService} from './auth-guard.service';
import {UserSessionService} from './user-session.service';

describe('AuthGuardService', () => {
  it('allows a valid session', () => {
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    const session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['isTokenExpired']);
    session.isTokenExpired.and.returnValue(false);
    expect(new AuthGuardService(router, session).canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects an expired session to login', () => {
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    const session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['isTokenExpired']);
    session.isTokenExpired.and.returnValue(true);
    expect(new AuthGuardService(router, session).canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {queryParams: {expired: ''}});
  });
});
