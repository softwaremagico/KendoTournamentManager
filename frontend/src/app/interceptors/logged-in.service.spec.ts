import {ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';

import {LoggedInService} from './logged-in.service';

describe('LoggedInService', () => {
  let routerSpy: jasmine.SpyObj<any>;
  let loginServiceSpy: jasmine.SpyObj<any>;
  let service: LoggedInService;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['getJwtValue', 'refreshDataFormJwt']);
    service = new LoggedInService(routerSpy, loginServiceSpy, {} as any);
  });

  it('should block access and redirect to the login page when there is no JWT and the route is not whitelisted', () => {
    loginServiceSpy.getJwtValue.and.returnValue(null);

    const state = {url: '/admin/secure?page=2'} as RouterStateSnapshot;

    const canActivate = service.canActivate({} as ActivatedRouteSnapshot, state);

    expect(canActivate).toBeFalse();
    expect(service.isUserLoggedIn.value).toBeFalse();
    expect(loginServiceSpy.refreshDataFormJwt).not.toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledOnceWith(
      ['/login'],
      {queryParams: {returnUrl: '/admin/secure?page=2'}}
    );
  });

  it('allows an authenticated user and refreshes roles from the JWT', () => {
    loginServiceSpy.getJwtValue.and.returnValue('jwt');
    localStorage.removeItem('account');

    const allowed: boolean = service.canActivate({} as ActivatedRouteSnapshot, {url: '/admin/secure'} as RouterStateSnapshot);

    expect(allowed).toBeTrue();
    expect(loginServiceSpy.refreshDataFormJwt).toHaveBeenCalled();
    expect(service.isUserLoggedIn.value).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('allows whitelisted pages without a session', () => {
    loginServiceSpy.getJwtValue.and.returnValue(null);

    const allowed: boolean = service.canActivate({} as ActivatedRouteSnapshot,
      {url: '/tournaments/fights?id=7'} as RouterStateSnapshot);

    expect(allowed).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('allows the public registration page without a session and without role redirects', () => {
    loginServiceSpy.getJwtValue.and.returnValue(null);

    expect(service.canActivate({} as ActivatedRouteSnapshot,
      {url: '/register?invitation=jwt'} as RouterStateSnapshot)).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();

    loginServiceSpy.getJwtValue.and.returnValue('jwt');
    localStorage.setItem('account', 'participant');
    expect(service.userLoginPageDependingOnRoles('/register', '')).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
    localStorage.removeItem('account');
  });

  it('redirects guest and participant sessions to their permitted pages', () => {
    loginServiceSpy.getJwtValue.and.returnValue('jwt');

    localStorage.setItem('account', 'participant');
    expect(service.userLoginPageDependingOnRoles('/admin', '')).toBeTrue();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/participants/statistics']);
    expect(service.userLoginPageDependingOnRoles('/participants/fights', '')).toBeTrue();

    routerSpy.navigate.calls.reset();
    localStorage.setItem('account', 'guest');
    expect(service.userLoginPageDependingOnRoles('/admin', '')).toBeTrue();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments/fights']);
    localStorage.removeItem('account');
  });
});

