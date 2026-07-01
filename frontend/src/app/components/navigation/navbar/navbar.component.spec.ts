import {of, Subject} from 'rxjs';
import {ElementRef, Renderer2} from '@angular/core';
import {ContextMenuService} from '@perfectmemory/ngx-contextmenu';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {OverlayContainer} from '@angular/cdk/overlay';
import {ActivityService} from '../../../services/rbac/activity.service';
import {UserSessionService} from '../../../services/user-session.service';
import {DarkModeService} from '../../../services/notifications/dark-mode.service';
import {RbacService} from '../../../services/rbac/rbac.service';
import {NavbarComponent} from './navbar.component';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let contextMenuServiceSpy: jasmine.SpyObj<ContextMenuService<void>>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let activityServiceSpy: jasmine.SpyObj<ActivityService>;
  let overlayContainerSpy: jasmine.SpyObj<OverlayContainer>;
  let rendererSpy: jasmine.SpyObj<Renderer2>;
  let darkModeServiceMock: DarkModeService;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    contextMenuServiceSpy = jasmine.createSpyObj('ContextMenuService', ['show']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getNightMode', 'setNightMode', 'getUser']);
    activityServiceSpy = jasmine.createSpyObj('ActivityService', ['isAllowed']);
    overlayContainerSpy = jasmine.createSpyObj('OverlayContainer', ['getContainerElement']);
    rendererSpy = jasmine.createSpyObj('Renderer2', ['addClass', 'removeClass']);
    darkModeServiceMock = {
      darkModeSwitched: new Subject<boolean>()
    } as DarkModeService;
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    userSessionServiceSpy.getNightMode.and.returnValue(false);
    userSessionServiceSpy.getUser.and.returnValue({email: 'john@doe.com'} as any);
    activityServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated-title'));

    const classListMock = {
      add: jasmine.createSpy('add'),
      remove: jasmine.createSpy('remove')
    };
    overlayContainerSpy.getContainerElement.and.returnValue({classList: classListMock} as any);

    component = new NavbarComponent(
      routerSpy,
      contextMenuServiceSpy,
      translocoServiceSpy,
      userSessionServiceSpy,
      activityServiceSpy,
      userSessionServiceSpy,
      overlayContainerSpy,
      rendererSpy,
      darkModeServiceMock,
      rbacServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize nightModeEnabled from user session in constructor', () => {
    expect(userSessionServiceSpy.getNightMode).toHaveBeenCalled();
    expect(component.nightModeEnabled).toBeFalse();
  });

  it('should initialize user and routes on ngOnInit', () => {
    component.ngOnInit();

    expect(component.user).toEqual({email: 'john@doe.com'} as any);
    expect(component.routes.length).toBe(4);
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalled();
  });

  it('should set dark mode class when night mode is disabled on init', () => {
    component.nightModeEnabled = false;

    component.ngOnInit();

    const classList = overlayContainerSpy.getContainerElement.calls.mostRecent().returnValue.classList;
    expect(component.className).toBe('');
    expect(classList.remove).toHaveBeenCalledWith('dark-mode');
    expect(rendererSpy.removeClass).toHaveBeenCalledWith(document.body, 'dark-mode');
  });

  it('should show context menu and prevent event propagation on onContextMenu', () => {
    component.contextMenu = {} as any;
    component.navUser = {
      nativeElement: {
        offsetLeft: 100,
        offsetWidth: 20,
        offsetHeight: 40
      }
    } as ElementRef;
    const event = {
      preventDefault: jasmine.createSpy('preventDefault'),
      stopPropagation: jasmine.createSpy('stopPropagation')
    } as unknown as Event;

    (component as any).onContextMenu(event);

    expect(contextMenuServiceSpy.show).toHaveBeenCalled();
    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('should open wiki page in new tab', () => {
    spyOn(window, 'open');

    component.openWiki();

    expect(window.open).toHaveBeenCalledOnceWith('https://github.com/softwaremagico/KendoTournamentManager/wiki', '_blank', 'noopener');
  });

  it('should open about page in new tab', () => {
    spyOn(window, 'open');

    component.openAbout();

    expect(window.open).toHaveBeenCalledOnceWith('https://github.com/softwaremagico/KendoTournamentManager', '_blank', 'noopener');
  });

  it('should switch dark mode on and notify services', () => {
    const darkModeNextSpy = spyOn(darkModeServiceMock.darkModeSwitched, 'next');
    component.nightModeEnabled = false;

    component.switchDarkMode();

    const classList = overlayContainerSpy.getContainerElement.calls.mostRecent().returnValue.classList;
    expect(component.nightModeEnabled).toBeTrue();
    expect(userSessionServiceSpy.setNightMode).toHaveBeenCalledOnceWith(true);
    expect(darkModeNextSpy).toHaveBeenCalledOnceWith(true);
    expect(component.className).toBe('dark-mode');
    expect(classList.add).toHaveBeenCalledWith('dark-mode');
    expect(rendererSpy.addClass).toHaveBeenCalledWith(document.body, 'dark-mode');
  });

  it('should switch dark mode off and notify services', () => {
    const darkModeNextSpy = spyOn(darkModeServiceMock.darkModeSwitched, 'next');
    component.nightModeEnabled = true;

    component.switchDarkMode();

    const classList = overlayContainerSpy.getContainerElement.calls.mostRecent().returnValue.classList;
    expect(component.nightModeEnabled).toBeFalse();
    expect(userSessionServiceSpy.setNightMode).toHaveBeenCalledOnceWith(false);
    expect(darkModeNextSpy).toHaveBeenCalledOnceWith(false);
    expect(component.className).toBe('');
    expect(classList.remove).toHaveBeenCalledWith('dark-mode');
    expect(rendererSpy.removeClass).toHaveBeenCalledWith(document.body, 'dark-mode');
  });
});

