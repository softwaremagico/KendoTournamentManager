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
});

