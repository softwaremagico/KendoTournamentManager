import {HttpHandler, HttpRequest} from '@angular/common/http';
import {of} from 'rxjs';
import {HeaderInterceptor} from './header-interceptor';
import {LoginService} from '../services/login.service';

describe('HeaderInterceptor', () => {
  it('should omit Authorization when no JWT exists', () => {
    const login = jasmine.createSpyObj<LoginService>('LoginService', ['getJwtValue']);
    login.getJwtValue.and.returnValue(null as any);
    const handler = jasmine.createSpyObj<HttpHandler>('HttpHandler', ['handle']);
    handler.handle.and.returnValue(of());

    new HeaderInterceptor(login).intercept(new HttpRequest('GET', '/login'), handler);

    const request = handler.handle.calls.mostRecent().args[0];
    expect(request.headers.has('Authorization')).toBeFalse();
    expect(request.headers.has('X-Session')).toBeTrue();
  });

  it('should replace stale Authorization with the current JWT', () => {
    const login = jasmine.createSpyObj<LoginService>('LoginService', ['getJwtValue']);
    login.getJwtValue.and.returnValue('current-token');
    const handler = jasmine.createSpyObj<HttpHandler>('HttpHandler', ['handle']);
    handler.handle.and.returnValue(of());
    const request = new HttpRequest('GET', '/tournaments', {setHeaders: {Authorization: 'Bearer old-token'}});

    new HeaderInterceptor(login).intercept(request, handler);

    expect(handler.handle.calls.mostRecent().args[0].headers.getAll('Authorization')).toEqual(['Bearer current-token']);
  });
});
