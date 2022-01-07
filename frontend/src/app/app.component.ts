import {Component} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {AuthenticatedUserService} from "./services/authenticated-user.service";
import {LoggedInService} from "./guards/logged-in.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'Kendo Tournament Manager v2';
  selectedLanguage = 'en';
  loggedIn = false;

  constructor(public translate: TranslateService, public authenticatedUserService: AuthenticatedUserService, public loggedInService: LoggedInService) {
    translate.addLangs(['en', 'es', 'it', 'de', 'nl', 'ca']);
    translate.setDefaultLang('en');
    this.loggedInService.isUserLoggedIn.subscribe(value => this.loggedIn = value);
  }

  switchLanguage(lang: string) {
    this.translate.use(lang);
    this.selectedLanguage = lang;
  }
}
