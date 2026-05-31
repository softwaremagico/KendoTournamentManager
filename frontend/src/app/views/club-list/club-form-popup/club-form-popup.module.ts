import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ClubFormPopupComponent} from './club-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoModule} from "@ngneat/transloco";
import {ClubFormModule} from "../../../forms/club-form/club-form.module";
import {BiitActionButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";


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
    ClubFormModule,
    BiitActionButtonModule,
    BiitIconButtonModule,
    HasPermissionPipe,
    MatButtonModule,
    MatIconModule
  ]
})
export class ClubFormPopupModule {
}
