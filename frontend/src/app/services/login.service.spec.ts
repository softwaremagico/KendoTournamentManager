import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {of} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {RxStompService} from '../websockets/rx-stomp.service';
import {ActivityService} from './rbac/activity.service';
import {LoginService} from './login.service';
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
  });
});
