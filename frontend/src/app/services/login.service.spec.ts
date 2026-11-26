import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {RxStompService} from '../websockets/rx-stomp.service';
import {ActivityService} from './rbac/activity.service';
import {LoginService} from './login.service';
import {UserRoles} from './rbac/user-roles';
import {UserSessionService} from './user-session.service';

describe('LoginService', () => {
  let http: jasmine.SpyObj<HttpClient>;
  let environment: jasmine.SpyObj<EnvironmentService>;
  let activities: jasmine.SpyObj<ActivityService>;
  let router: jasmine.SpyObj<Router>;
  let session: jasmine.SpyObj<UserSessionService>;
  let rxStomp: jasmine.SpyObj<RxStompService>;
  let service: LoginService;

  beforeEach(() => {
    http = jasmine.createSpyObj<HttpClient>('HttpClient', ['post', 'get']);
    environment = jasmine.createSpyObj<EnvironmentService>('EnvironmentService', ['getBackendUrl']);
    environment.getBackendUrl.and.returnValue('/backend');
    activities = jasmine.createSpyObj<ActivityService>('ActivityService', ['setRoles']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    session = jasmine.createSpyObj<UserSessionService>('UserSessionService', [
      'getAuthToken', 'getExpirationDate', 'setAuthToken', 'setExpirationDate', 'setLocalUser', 'clearToken'
    ]);
    session.getAuthToken.and.returnValue(null);
    session.getExpirationDate.and.returnValue(null);
    rxStomp = jasmine.createSpyObj<RxStompService>('RxStompService', ['activate', 'deactivate']);
    service = new LoginService(http, environment, activities, router, session, rxStomp);
  });

  it('reads token metadata from the session', () => {
    expect(service.getJwtValue()).toBeNull();
    expect(service.getJwtExpirationValue()).toBe(0);
    expect(service.getTenantId()).toBeNull();
    expect(service.getJwtRoles()).toEqual([]);
  });

  it('decodes tenant and roles from a JWT', () => {
    const payload = btoa(JSON.stringify({tenantId: 7, roles: ['ADMIN', 'VIEWER']}));
    session.getAuthToken.and.returnValue(`header.${payload}.signature`);
    expect(service.getTenantId()).toBe(7);
    expect(service.getJwtRoles()).toEqual(['ADMIN', 'VIEWER']);

    session.getAuthToken.and.returnValue('header.invalid.signature');
    expect(service.getTenantId()).toBeNull();
    expect(service.getJwtRoles()).toEqual([]);
  });

  it('posts credentials and reads authentication headers from the response', () => {
    const headers = new HttpHeaders({Authorization: 'token', Expires: '123', 'X-Session': 'session'});
    const response = new HttpResponse({body: {username: 'aki', roles: []} as any, headers});
    (http.post as jasmine.Spy).and.returnValue(of(response));

    service.login('aki', 'password').subscribe(user => {
      expect(user.username).toBe('aki');
      expect(user.jwt).toBe('token');
      expect(user.expires).toBe(123);
      expect(user.session).toBe('session');
    });

    expect(http.post).toHaveBeenCalledWith('/backend/auth/public/login', {username: 'aki', password: 'password'}, jasmine.any(Object));
  });

  it('clears the session and the websocket on logout', () => {
    service.logout();
    expect(session.clearToken).toHaveBeenCalled();
    expect(activities.setRoles).toHaveBeenCalledWith([]);
    expect(rxStomp.deactivate).toHaveBeenCalled();
    expect(localStorage.getItem('account')).toBeNull();
    expect(localStorage.getItem('tournamentId')).toBeNull();
  });

  it('establishes a guest session from the authentication headers', () => {
    (http.post as jasmine.Spy).and.returnValue(of(authResponse('guest-jwt', 1000)));
    const callback = jasmine.createSpy('callback');

    service.setGuestUserSession(7, 'ticket-123', callback);

    expect(http.post).toHaveBeenCalledWith('/backend/auth/public/login/guest',
      jasmine.objectContaining({tournamentId: 7, ticket: 'ticket-123'}), jasmine.any(Object));
    expect(session.setAuthToken).toHaveBeenCalledWith('guest-jwt');
    expect(session.setExpirationDate).toHaveBeenCalledWith(1000);
    expect(activities.setRoles).toHaveBeenCalledWith([UserRoles.ADMIN]);
    expect(session.setLocalUser).toHaveBeenCalled();
    expect(rxStomp.activate).toHaveBeenCalled();
    expect(localStorage.getItem('account')).toBe('guest');
    expect(localStorage.getItem('tournamentId')).toBe('7');
    expect(localStorage.getItem('username')).toBe('aki');
    expect(callback).toHaveBeenCalledWith('guest-jwt', 1000);
  });

  it('establishes a participant session and redirects when the login fails', () => {
    (http.post as jasmine.Spy).and.returnValue(of(authResponse('participant-jwt', 1000)));
    const callback = jasmine.createSpy('callback');

    service.setParticipantUserSession('temporal-token', callback);

    expect(http.post).toHaveBeenCalledWith('/backend/auth/public/participant/token',
      jasmine.objectContaining({content: 'temporal-token'}), jasmine.any(Object));
    expect(localStorage.getItem('account')).toBe('participant');
    expect(callback).toHaveBeenCalledWith('participant-jwt', 1000);

    (http.post as jasmine.Spy).and.returnValue(throwError(() => new Error('invalid token')));
    service.setParticipantUserSession('invalid', (): void => {});
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('selects a tenant and refreshes roles from the token', () => {
    (http.post as jasmine.Spy).and.returnValue(of(authResponse('tenant-jwt', 1000)));

    let selectedTenant: number | undefined;
    service.selectTenant(9).subscribe(user => selectedTenant = user.tenantId);
    expect(http.post).toHaveBeenCalledWith('/backend/auth/tenants/9/select', {}, jasmine.any(Object));
    expect(selectedTenant).toBe(9);

    const payload = btoa(JSON.stringify({tenantId: 9, roles: ['EDITOR']}));
    session.getAuthToken.and.returnValue(`header.${payload}.signature`);
    (http.get as jasmine.Spy).and.returnValue(of(['EDITOR']));
    service.refreshDataFormJwt();
    expect(activities.setRoles).toHaveBeenCalledWith([UserRoles.EDITOR]);
  });

  it('applies renew responses and stops when the server returns invalid data', () => {
    spyOn(console, 'error');
    try {
      const now: number = Date.now();
      const callback = jasmine.createSpy('callback');
      const renewed = new HttpResponse({
        headers: new HttpHeaders({Authorization: 'renewed-jwt', Expires: String(now + 120000)})
      });

      (service as any).handleRenewResponse('old-jwt', renewed, callback);

      expect(callback).toHaveBeenCalledWith('renewed-jwt', now + 120000);
      expect(session.setAuthToken).toHaveBeenCalledWith('renewed-jwt');
      expect(session.setExpirationDate).toHaveBeenCalledWith(now + 120000);

      (service as any).handleRenewResponse('old-jwt', new HttpResponse({headers: new HttpHeaders({})}), callback);
      expect(console.error).toHaveBeenCalledWith('Server returned invalid renew response');

      service.autoRenewToken(null, 0, callback);
      expect((service as any).interval).toBeNull();
    } finally {
      service.autoRenewToken(null, 0, (): void => {});
    }
  });

  function authResponse(jwt: string, expires: number): HttpResponse<any> {
    return new HttpResponse({
      body: {username: 'aki', roles: ['ADMIN'], tenantId: 9},
      headers: new HttpHeaders({Authorization: jwt, Expires: String(expires), 'X-Session': 'session-id'})
    });
  }
});
