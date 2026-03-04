import {Component} from '@angular/core';
import {LoginService} from "./services/login.service";
import {LoggedInService} from "./interceptors/logged-in.service";
import {UserSessionService} from "./services/user-session.service";
import {RbacService} from "./services/rbac/rbac.service";
import {RbacBasedComponent} from "./components/RbacBasedComponent";
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
  hideMenu = false;

  constructor(public translocoService: TranslocoService, public loginService: LoginService, public loggedInService: LoggedInService,
              protected userSessionService: UserSessionService, rbacService: RbacService, biitIconService: BiitIconService,
              projectModeChangedService: ProjectModeChangedService,
              protected sessionService: UserSessionService, private activityService: ActivityService) {
    super(rbacService);
    this.setLanguage();
    biitIconService.registerIcons(completeIconSet);
    this.loggedInService.isUserLoggedIn.subscribe((value: boolean) => this.loggedIn = value);
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

  private setPermissions(): void {
    const user: AuthenticatedUser | undefined = this.sessionService.getUser();
    if (!user) {
      return;
    }
    this.activityService.setRoles(user.roles);
  }
}
