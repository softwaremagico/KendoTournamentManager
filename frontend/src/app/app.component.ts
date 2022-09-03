import {Component} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {LoginService} from "./services/login.service";
import {LoggedInService} from "./guards/logged-in.service";
import {UserSessionService} from "./services/user-session.service";
import {ConfirmationDialogComponent} from "./components/basic/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {MessageService} from "./services/message.service";
import {RbacService} from "./services/rbac/rbac.service";
import {RbacBasedComponent} from "./components/RbacBasedComponent";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent extends RbacBasedComponent{
  selectedLanguage = 'en';
  loggedIn = false;
  selectedRow: string = '';

  constructor(public translate: TranslateService, public loginService: LoginService, public loggedInService: LoggedInService,
              private userSessionService: UserSessionService, private dialog: MatDialog, private router: Router,
              private messageService: MessageService, rbacService: RbacService) {
    super(rbacService);
    translate.addLangs(['en', 'es', 'it', 'de', 'nl', 'ca']);
    translate.setDefaultLang('en');
    this.loggedInService.isUserLoggedIn.subscribe(value => this.loggedIn = value);
    if (userSessionService.getLanguage()) {
      this.translate.use(userSessionService.getLanguage());
      this.selectedLanguage = userSessionService.getLanguage();
    }
  }

  toggleMenu(selectedRow: string) {
    if (this.selectedRow === selectedRow) {
      this.selectedRow = '';
    } else {
      this.selectedRow = selectedRow;
    }
  }

  switchLanguage(lang: string) {
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
        this.messageService.infoMessage("userloggedOutMessage");
        this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
      }
    });
  }
}
