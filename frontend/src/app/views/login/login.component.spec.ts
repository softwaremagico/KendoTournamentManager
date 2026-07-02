import {of, Subject, throwError} from 'rxjs';
import {UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {LoginComponent} from './login.component';
import {LoginService} from '../../services/login.service';
import {MessageService} from '../../services/message.service';
import {LoggerService} from '../../services/logger.service';
import {InfoService} from '../../services/info.service';
import {TranslocoService} from '@jsverse/transloco';
import {EnvironmentService} from '../../environment.service';
import {UserSessionService} from '../../services/user-session.service';
import {ActivityService} from '../../services/rbac/activity.service';
import {Constants} from '../../constants';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let loggerServiceSpy: jasmine.SpyObj<LoggerService>;
  let infoServiceSpy: jasmine.SpyObj<InfoService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let environmentServiceSpy: jasmine.SpyObj<EnvironmentService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let activityServiceSpy: jasmine.SpyObj<ActivityService>;
  let activatedRouteMock: ActivatedRoute;
  let queryParams$: Subject<any>;

  const createComponent = (): void => {
    component = new LoginComponent(
      routerSpy,
      activatedRouteMock,
      loginServiceSpy,
      new UntypedFormBuilder(),
      messageServiceSpy,
      loggerServiceSpy,
      infoServiceSpy,
      translocoServiceSpy,
      environmentServiceSpy,
      userSessionServiceSpy,
      activityServiceSpy,
      translocoServiceSpy
    );
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['login', 'setAuthenticatedUser', 'logout']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'warningMessage', 'errorMessage']);
    loggerServiceSpy = jasmine.createSpyObj('LoggerService', ['info']);
    infoServiceSpy = jasmine.createSpyObj('InfoService', ['getLatestVersion']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate', 'selectTranslate']);
    environmentServiceSpy = jasmine.createSpyObj('EnvironmentService', ['isCheckForNewVersion']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', [
      'isTokenExpired',
      'clearToken',
      'setUser'
    ]);
    activityServiceSpy = jasmine.createSpyObj('ActivityService', ['clear', 'setRoles']);

    queryParams$ = new Subject<any>();
    activatedRouteMock = {
      queryParams: queryParams$,
      snapshot: {
        queryParams: {}
      }
    } as unknown as ActivatedRoute;

    environmentServiceSpy.isCheckForNewVersion.and.returnValue(false);
    infoServiceSpy.getLatestVersion.and.returnValue(of('1.0.0'));
    translocoServiceSpy.selectTranslate.and.returnValue(of('logout message'));
    translocoServiceSpy.translate.and.returnValue('translated warning');
    userSessionServiceSpy.isTokenExpired.and.returnValue(true);

    createComponent();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize login form with username and password controls', () => {
    expect(component.loginForm.contains('username')).toBeTrue();
    expect(component.loginForm.contains('password')).toBeTrue();
  });

  it('should navigate to tournaments when token is not expired', () => {
    userSessionServiceSpy.isTokenExpired.and.returnValue(false);
    createComponent();

    component.ngOnInit();

    expect(routerSpy.navigate).toHaveBeenCalledWith([Constants.PATHS.TOURNAMENTS.ROOT]);
    expect(userSessionServiceSpy.clearToken).not.toHaveBeenCalled();
  });

  it('should clear token and stop waiting when token is expired', () => {
    userSessionServiceSpy.isTokenExpired.and.returnValue(true);

    component.ngOnInit();

    expect(userSessionServiceSpy.clearToken).toHaveBeenCalled();
    expect((component as any).waiting).toBeFalse();
  });

  it('should show warning when a newer version is available', () => {
    environmentServiceSpy.isCheckForNewVersion.and.returnValue(true);
    infoServiceSpy.getLatestVersion.and.returnValue(of('9.9.9'));
    createComponent();

    component.isLastVersion();

    expect(infoServiceSpy.getLatestVersion).toHaveBeenCalled();
    expect(translocoServiceSpy.translate).toHaveBeenCalledWith(
      'newVersionAvailable',
      jasmine.objectContaining({ currentVersion: component.appVersion, newVersion: '9.9.9' })
    );
    expect(messageServiceSpy.warningMessage).toHaveBeenCalledWith('translated warning');
  });

  it('should logout and clear logout query param when logout query exists', () => {
    component.ngOnInit();

    queryParams$.next({ [Constants.PATHS.QUERY.LOGOUT]: 'true' });

    expect(loginServiceSpy.logout).toHaveBeenCalled();
    expect(activityServiceSpy.clear).toHaveBeenCalled();
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('logout message');
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      queryParams: { [Constants.PATHS.QUERY.LOGOUT]: null },
      queryParamsHandling: 'merge'
    });
  });

  it('should login and navigate to returnUrl when provided', () => {
    (activatedRouteMock.snapshot as any).queryParams = { returnUrl: '/secure' };
    const authenticatedUser = { roles: ['admin'], username: 'john' } as any;
    loginServiceSpy.login.and.returnValue(of(authenticatedUser));
    spyOn(localStorage, 'setItem');

    component.loginForm.controls['username'].setValue('john@doe.com');
    component.login({ username: 'john@doe.com', password: 'Pass1234' } as any);

    expect(loginServiceSpy.login).toHaveBeenCalledOnceWith('john@doe.com', 'Pass1234');
    expect(loginServiceSpy.setAuthenticatedUser).toHaveBeenCalled();
    expect(activityServiceSpy.setRoles).toHaveBeenCalledWith(authenticatedUser.roles);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/secure']);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('userLoggedInMessage');
    expect(userSessionServiceSpy.setUser).toHaveBeenCalled();
    expect(localStorage.setItem).toHaveBeenCalledWith('username', 'john@doe.com');
    expect((component as any).waiting).toBeFalse();
  });

  it('should login and navigate to tournaments root when returnUrl is missing', () => {
    (activatedRouteMock.snapshot as any).queryParams = {};
    const authenticatedUser = { roles: ['viewer'], username: 'john' } as any;
    loginServiceSpy.login.and.returnValue(of(authenticatedUser));

    component.login({ username: 'john@doe.com', password: 'Pass1234' } as any);

    expect(routerSpy.navigate).toHaveBeenCalledWith([Constants.PATHS.TOURNAMENTS.ROOT]);
  });

  it('should show denied user error on 401', () => {
    loginServiceSpy.login.and.returnValue(throwError(() => ({ status: 401 })));

    component.login({ username: 'john@doe.com', password: 'Pass1234' } as any);

    expect(loggerServiceSpy.info).toHaveBeenCalled();
    expect(messageServiceSpy.errorMessage).toHaveBeenCalledWith('deniedUserError');
    expect((component as any).waiting).toBeFalse();
  });

  it('should show blocked user warning on 423', () => {
    loginServiceSpy.login.and.returnValue(throwError(() => ({ status: 423 })));

    component.login({ username: 'john@doe.com', password: 'Pass1234' } as any);

    expect(loggerServiceSpy.info).toHaveBeenCalled();
    expect(messageServiceSpy.warningMessage).toHaveBeenCalledWith('blockedUserError');
  });

  it('should show backend error on unknown status', () => {
    loginServiceSpy.login.and.returnValue(throwError(() => ({ status: 500 })));

    component.login({ username: 'john@doe.com', password: 'Pass1234' } as any);

    expect(messageServiceSpy.errorMessage).toHaveBeenCalledWith('backendError');
  });
});

