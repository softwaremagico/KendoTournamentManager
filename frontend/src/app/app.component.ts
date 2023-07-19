import {Component, HostBinding, Renderer2} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
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

  constructor(public translate: TranslateService, public loginService: LoginService, public loggedInService: LoggedInService,
              private userSessionService: UserSessionService, private dialog: MatDialog, private router: Router,
              private overlay: OverlayContainer, private _renderer: Renderer2,
              private messageService: MessageService, rbacService: RbacService,
              private darkModeService: DarkModeService) {
    super(rbacService);
    translate.addLangs(['en', 'es', 'it', 'de', 'nl', 'ca']);
    translate.setDefaultLang('en');
    this.loggedInService.isUserLoggedIn.subscribe(value => this.loggedIn = value);
    if (userSessionService.getLanguage()) {
      this.translate.use(userSessionService.getLanguage());
      this.selectedLanguage = userSessionService.getLanguage();
    }
    this.nightModeEnabled = userSessionService.getNightMode();
    this.setDarkModeTheme();
  }

  toggleMenu(selectedRow: string): void {
    if (this.selectedRow === selectedRow) {
      this.selectedRow = '';
    } else {
      this.selectedRow = selectedRow;
    }
  }

  switchLanguage(lang: string): void {
    this.translate.use(lang);
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
}
