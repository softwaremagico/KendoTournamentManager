import {Component} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {AuthenticatedUserService} from "./services/authenticated-user.service";
import {LoggedInService} from "./guards/logged-in.service";
import {UserSessionService} from "./services/user-session.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  selectedLanguage = 'en';
  loggedIn = false;
  selectedRow: string = '';

  constructor(public translate: TranslateService, public authenticatedUserService: AuthenticatedUserService, public loggedInService: LoggedInService,
              private userSessionService: UserSessionService) {
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
}
