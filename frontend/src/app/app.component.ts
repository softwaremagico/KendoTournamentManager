import {Component, HostBinding, Renderer2} from '@angular/core';
import {LoginService} from "./services/login.service";
import {LoggedInService} from "./interceptors/logged-in.service";
import {UserSessionService} from "./services/user-session.service";
import {ConfirmationDialogComponent} from "./components/basic/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {MessageService} from "./services/message.service";
import {RbacService} from "./services/rbac/rbac.service";
import {RbacBasedComponent} from "./components/RbacBasedComponent";
import {OverlayContainer} from "@angular/cdk/overlay";
import {DarkModeService} from "./services/notifications/dark-mode.service";
import {ProjectModeChangedService} from "./services/notifications/project-mode-changed.service";
import {AvailableLangs, TranslocoService} from "@ngneat/transloco";
import {BiitIconService} from "@biit-solutions/wizardry-theme/icon";
import {completeIconSet} from "@biit-solutions/biit-icons-collection";

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
              protected userSessionService: UserSessionService, private dialog: MatDialog, private router: Router,
              private overlay: OverlayContainer, private _renderer: Renderer2,
              private messageService: MessageService, rbacService: RbacService, biitIconService: BiitIconService,
              private darkModeService: DarkModeService, private projectModeChangedService: ProjectModeChangedService) {
    super(rbacService);
    this.setLanguage();
    biitIconService.registerIcons(completeIconSet);
    this.loggedInService.isUserLoggedIn.subscribe((value: boolean) => this.loggedIn = value);
    this.nightModeEnabled = userSessionService.getNightMode();
    this.setDarkModeTheme();
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

  switchLanguage(lang: string): void {
    this.translocoService.setActiveLang(lang);
    this.selectedLanguage = lang;
    this.userSessionService.setLanguage(lang);
  }

  logout() {
    let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: false
    });
    dialogRef.componentInstance.messageTag = "logoutWarning"

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loginService.logout();
        this.rbacService.setRoles([]);
        this.loggedIn = false;
        this.messageService.infoMessage("userLoggedOutMessage");
        this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
      }
    });
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

  openWiki(): void {
    window.open("https://github.com/softwaremagico/KendoTournamentManager/wiki", "_blank");
  }
}
