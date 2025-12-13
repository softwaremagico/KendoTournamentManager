import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClubFormPopupComponent } from './club-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoModule} from "@ngneat/transloco";
import {ClubFormModule} from "../../../forms/club-form/club-form.module";



@NgModule({
  declarations: [
    ClubFormPopupComponent
  ],
  exports: [
    ClubFormPopupComponent
  ],
  imports: [
    CommonModule,
    BiitPopupModule,
    TournamentFormModule,
    TranslocoModule,
    ClubFormModule
  ]
})
export class ClubFormPopupModule { }
