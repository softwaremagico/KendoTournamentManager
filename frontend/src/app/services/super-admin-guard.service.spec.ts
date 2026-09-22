import {SuperAdminGuardService} from './super-admin-guard.service';
import {UserRoles} from './rbac/user-roles';

describe('SuperAdminGuardService', () => {
  let router: jasmine.SpyObj<any>;
  let userSessionService: jasmine.SpyObj<any>;
  let guard: SuperAdminGuardService;

  beforeEach(() => {
    router = jasmine.createSpyObj('Router', ['navigate']);
    userSessionService = jasmine.createSpyObj('UserSessionService', ['getUser']);
    guard = new SuperAdminGuardService(router, userSessionService);
  });

  it('allows a super administrator', () => {
    userSessionService.getUser.and.returnValue({roles: [UserRoles.SUPER_ADMIN]});

    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects a tenant administrator', () => {
    userSessionService.getUser.and.returnValue({roles: [UserRoles.ADMIN]});

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['tournaments']);
  });
});
