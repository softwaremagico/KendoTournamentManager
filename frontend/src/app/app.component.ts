import {Component, HostBinding, Renderer2} from '@angular/core';
import {LoginService} from "./services/login.service";
import {LoggedInService} from "./interceptors/logged-in.service";
import {UserSessionService} from "./services/user-session.service";
import {RbacService} from "./services/rbac/rbac.service";
import {RbacBasedComponent} from "./components/RbacBasedComponent";
import {OverlayContainer} from "@angular/cdk/overlay";
import {DarkModeService} from "./services/notifications/dark-mode.service";
import {ProjectModeChangedService} from "./services/notifications/project-mode-changed.service";
import {AvailableLangs, TranslocoService} from "@ngneat/transloco";
import {BiitIconService} from "@biit-solutions/wizardry-theme/icon";
import {completeIconSet} from "@biit-solutions/biit-icons-collection";
import {AuthenticatedUser} from "./models/authenticated-user";
import {ActivityService} from "./services/rbac/activity.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent extends RbacBasedComponent {
  selectedLanguage = 'en';
  loggedIn = false;
  selectedRow: string = '';
  nightModeEnabled: boolean = false;
  @HostBinding('class') className = '';
  hideMenu = false;

  constructor(public translocoService: TranslocoService, public loginService: LoginService, public loggedInService: LoggedInService,
              protected userSessionService: UserSessionService, private overlay: OverlayContainer, private _renderer: Renderer2,
              rbacService: RbacService, biitIconService: BiitIconService,
              private darkModeService: DarkModeService, projectModeChangedService: ProjectModeChangedService,
              protected sessionService: UserSessionService, private activityService: ActivityService) {
    super(rbacService);
    this.setLanguage();
    biitIconService.registerIcons(completeIconSet);
    this.loggedInService.isUserLoggedIn.subscribe((value: boolean) => this.loggedIn = value);
    this.nightModeEnabled = userSessionService.getNightMode();
    this.setDarkModeTheme();
    this.setPermissions();
    projectModeChangedService.isProjectMode.subscribe((_mode: boolean): void => {
      this.hideMenu = _mode;
    });
  }

  private setLanguage(): void {
    const clientLanguages: ReadonlyArray<string> = navigator.languages;
    const languages: AvailableLangs = this.translocoService.getAvailableLangs();
    if (this.userSessionService.getLanguage()) {
      this.translocoService.setActiveLang(this.userSessionService.getLanguage());
      this.selectedLanguage = this.userSessionService.getLanguage();
    } else {
      const language: string | undefined = clientLanguages.find(lang => languages.map(lang => lang.toString()).includes(lang));
      if (language) {
        this.translocoService.setActiveLang(language);
        this.selectedLanguage = language;
      }
    }
  }

  toggleMenu(selectedRow: string): void {
    if (this.selectedRow === selectedRow) {
      this.selectedRow = '';
    } else {
      this.selectedRow = selectedRow;
    }
  }

  switchDarkMode(): void {
    this.nightModeEnabled = !this.nightModeEnabled;
    this.userSessionService.setNightMode(this.nightModeEnabled);
    this.darkModeService.darkModeSwitched.next(this.nightModeEnabled);
    this.setDarkModeTheme();
  }

  private setDarkModeTheme(): void {
    this.className = this.nightModeEnabled ? 'dark-mode' : '';
    if (this.nightModeEnabled) {
      this.overlay.getContainerElement().classList.add('dark-mode');
      //For drag and drop preview.
      this._renderer.addClass(document.body, 'dark-mode');
    } else {
      this.overlay.getContainerElement().classList.remove('dark-mode');
      //For drag and drop preview.
      this._renderer.removeClass(document.body, 'dark-mode');
    }
  }

  private setPermissions(): void {
    const user: AuthenticatedUser | undefined = this.sessionService.getUser();
    if (!user) {
      return;
    }
    this.activityService.setRoles(user.roles);
  }
}
