import {Component} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'Kendo Tournament Generator v2';
  selectedLanguage = 'en';

  constructor(public translate: TranslateService) {
    translate.addLangs(['en', 'es', 'it', 'de', 'nl', 'ca']);
    translate.setDefaultLang('en');
  }

  switchLanguage(lang: string) {
    this.translate.use(lang);
    this.selectedLanguage = lang;
  }
}
