import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipantFormPopupComponent } from './participant-form-popup.component';
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormModule} from "../../../forms/tournament-form/tournament-form.module";
import {TranslocoModule} from "@ngneat/transloco";
import {ParticipantFormModule} from "../../../forms/participant-form/participant-form.module";
import {BiitActionButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FloatingDeleteButtonModule} from "../../../components/floating-delete-button/floating-delete-button.module";



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
        ParticipantFormModule,
        BiitIconButtonModule,
        HasPermissionPipe,
        BiitActionButtonModule,
        MatButtonModule,
        MatIconModule,
        FloatingDeleteButtonModule
    ]
})
export class ParticipantFormPopupModule { }
