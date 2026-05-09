import {BehaviorSubject} from 'rxjs';
import {AppComponent} from './app.component';
import {TranslocoService} from '@ngneat/transloco';
import {LoginService} from './services/login.service';
import {LoggedInService} from './interceptors/logged-in.service';
import {UserSessionService} from './services/user-session.service';
import {RbacService} from './services/rbac/rbac.service';
import {BiitIconService} from '@biit-solutions/wizardry-theme/icon';
import {ProjectModeChangedService} from './services/notifications/project-mode-changed.service';
import {ActivityService} from './services/rbac/activity.service';
import {AuthenticatedUser} from './models/authenticated-user';

describe('AppComponent', () => {
  let component: AppComponent;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let loggedInServiceMock: LoggedInService;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let biitIconServiceSpy: jasmine.SpyObj<BiitIconService>;
  let projectModeChangedServiceMock: ProjectModeChangedService;
  let activityServiceSpy: jasmine.SpyObj<ActivityService>;

  const loggedUser = {
    id: 1,
    username: 'admin',
    roles: ['ADMIN']
  } as unknown as AuthenticatedUser;

  beforeEach(() => {
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', [
      'getAvailableLangs',
      'setActiveLang'
    ]);
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['getJwtValue']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getLanguage', 'getUser']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    biitIconServiceSpy = jasmine.createSpyObj('BiitIconService', ['registerIcons']);
    activityServiceSpy = jasmine.createSpyObj('ActivityService', ['setRoles']);

    loggedInServiceMock = {
      isUserLoggedIn: new BehaviorSubject<boolean>(false)
    } as LoggedInService;

    projectModeChangedServiceMock = {
      isProjectMode: new BehaviorSubject<boolean>(false)
    } as ProjectModeChangedService;

    translocoServiceSpy.getAvailableLangs.and.returnValue(['en', 'es'] as any);
    userSessionServiceSpy.getLanguage.and.returnValue('es');
    userSessionServiceSpy.getUser.and.returnValue(loggedUser);
    rbacServiceSpy.isAllowed.and.returnValue(true);

    component = new AppComponent(
      translocoServiceSpy,
      loginServiceSpy,
      loggedInServiceMock,
      userSessionServiceSpy,
      rbacServiceSpy,
      biitIconServiceSpy,
      projectModeChangedServiceMock,
      activityServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set language from session if available', () => {
    expect(translocoServiceSpy.setActiveLang).toHaveBeenCalledWith('es');
    expect(component.selectedLanguage).toBe('es');
  });

  it('should register icon set on constructor', () => {
    expect(biitIconServiceSpy.registerIcons).toHaveBeenCalled();
  });

  it('should set permissions when user is available', () => {
    expect(activityServiceSpy.setRoles).toHaveBeenCalledWith(loggedUser.roles);
  });

  it('should not set permissions when user is undefined', () => {
    userSessionServiceSpy.getUser.and.returnValue(undefined);

    component = new AppComponent(
      translocoServiceSpy,
      loginServiceSpy,
      loggedInServiceMock,
      userSessionServiceSpy,
      rbacServiceSpy,
      biitIconServiceSpy,
      projectModeChangedServiceMock,
      activityServiceSpy
    );

    expect(activityServiceSpy.setRoles).not.toHaveBeenCalledWith(undefined as any);
  });

  it('should update loggedIn value when loggedInService emits', () => {
    loggedInServiceMock.isUserLoggedIn.next(true);
    expect(component.loggedIn).toBeTrue();

    loggedInServiceMock.isUserLoggedIn.next(false);
    expect(component.loggedIn).toBeFalse();
  });

  it('should update hideMenu when project mode changes', () => {
    projectModeChangedServiceMock.isProjectMode.next(true);
    expect(component.hideMenu).toBeTrue();

    projectModeChangedServiceMock.isProjectMode.next(false);
    expect(component.hideMenu).toBeFalse();
  });

  it('should toggle selectedRow off when same row is clicked', () => {
    component.selectedRow = 'menuA';

    component.toggleMenu('menuA');

    expect(component.selectedRow).toBe('');
  });

  it('should toggle selectedRow on when a different row is clicked', () => {
    component.selectedRow = '';

    component.toggleMenu('menuB');

    expect(component.selectedRow).toBe('menuB');
  });

  it('should choose browser language when session language is empty and language exists', () => {
    userSessionServiceSpy.getLanguage.and.returnValue('' as any);
    spyOnProperty(navigator, 'languages', 'get').and.returnValue(['en', 'fr']);

    component = new AppComponent(
      translocoServiceSpy,
      loginServiceSpy,
      loggedInServiceMock,
      userSessionServiceSpy,
      rbacServiceSpy,
      biitIconServiceSpy,
      projectModeChangedServiceMock,
      activityServiceSpy
    );

    expect(translocoServiceSpy.setActiveLang).toHaveBeenCalledWith('en');
    expect(component.selectedLanguage).toBe('en');
  });
});

