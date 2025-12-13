import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipantFormPopupComponent } from './participant-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoModule} from "@ngneat/transloco";
import {ParticipantFormModule} from "../../../forms/participant-form/participant-form.module";



@NgModule({
  declarations: [
    ParticipantFormPopupComponent
  ],
  exports: [
    ParticipantFormPopupComponent
  ],
  imports: [
    CommonModule,
    BiitPopupModule,
    TournamentFormModule,
    TranslocoModule,
    ParticipantFormModule
  ]
})
export class ParticipantFormPopupModule { }
