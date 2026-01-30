import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentFormPopupComponent} from './tournament-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoRootModule} from "@biit-solutions/wizardry-theme/i18n";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";


@NgModule({
  declarations: [
    TournamentFormPopupComponent
  ],
  exports: [
    TournamentFormPopupComponent
  ],
    imports: [
        CommonModule,
        BiitPopupModule,
        TournamentFormModule,
        TranslocoRootModule,
        BiitButtonModule,
        HasPermissionPipe
    ]
})
export class TournamentFormPopupModule {
}
