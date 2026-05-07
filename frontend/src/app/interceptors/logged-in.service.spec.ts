import {ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';

import {LoggedInService} from './logged-in.service';

describe('LoggedInService', () => {
  it('debe bloquear y redirigir al login cuando no hay JWT y la ruta no esta en lista blanca', () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const loginServiceSpy = jasmine.createSpyObj('LoginService', ['getJwtValue', 'refreshDataFormJwt']);

    loginServiceSpy.getJwtValue.and.returnValue(null);

    const service = new LoggedInService(routerSpy, loginServiceSpy, {} as any);
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

