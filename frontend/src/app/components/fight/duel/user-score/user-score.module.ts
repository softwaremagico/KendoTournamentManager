import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserScoreComponent} from "./user-score.component";
import {ScoreModule} from "./score/score.module";
import {FaultModule} from "./fault/fault.module";
import {UserNameModule} from "./user-name/user-name.module";
import {TranslocoModule} from "@ngneat/transloco";
import {ParticipantPictureModule} from "../../../participant-picture/participant-picture.module";


@NgModule({
  declarations: [UserScoreComponent],
  exports: [
    UserScoreComponent
  ],
  imports: [
    CommonModule,
    ScoreModule,
    FaultModule,
    UserNameModule,
    TranslocoModule,
    ParticipantPictureModule
  ]
})
export class UserScoreModule {
}
