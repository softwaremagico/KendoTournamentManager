import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticatedUserFormPopupComponent } from './authenticated-user-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoModule} from "@ngneat/transloco";
import {
  TournamentFormPopupComponent
} from "../../tournament-list/tournament-form-popup/tournament-form-popup.component";
import {AuthenticatedUserFormModule} from "../../../forms/authenticated-user-form/authenticated-user-form.module";



@NgModule({
  declarations: [
    AuthenticatedUserFormPopupComponent
  ],
  exports: [
    AuthenticatedUserFormPopupComponent
  ],
  imports: [
    CommonModule,
    BiitPopupModule,
    TournamentFormModule,
    TranslocoModule,
    AuthenticatedUserFormModule
  ]
})
export class AuthenticatedUserFormPopupModule { }
