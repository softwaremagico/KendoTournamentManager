import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantPictureComponent} from "./participant-picture.component";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {
    TournamentExtraPropertiesFormModule
} from "../../forms/tournament-extra-properties-form/tournament-extra-properties-form.module";
import {PictureDialogBoxModule} from "./picture-dialog-box/picture-dialog-box.module";


@NgModule({
  declarations: [ParticipantPictureComponent],
  exports: [
    ParticipantPictureComponent
  ],
  imports: [
    CommonModule,
    BiitPopupModule,
    TournamentExtraPropertiesFormModule,
    PictureDialogBoxModule
  ]
})
export class ParticipantPictureModule {

}
