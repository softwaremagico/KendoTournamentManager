import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LanguageSelectorComponent} from './language-selector.component';
import {TranslocoModule} from "@ngneat/transloco";
import {BiitRadioButtonModule} from "@biit-solutions/wizardry-theme/inputs";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";


@NgModule({
  declarations: [
    LanguageSelectorComponent
  ],
  exports: [
    LanguageSelectorComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    BiitRadioButtonModule,
    FormsModule,
    BiitButtonModule
  ]
})
export class LanguageSelectorModule {
}
