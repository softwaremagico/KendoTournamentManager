import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentExtraSettingsFormComponent } from './tournament-extra-settings-form.component';
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@ngneat/transloco";
import {BiitToggleModule} from "@biit-solutions/wizardry-theme/inputs";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";



@NgModule({
  declarations: [
    TournamentExtraSettingsFormComponent
  ],
  exports: [
    TournamentExtraSettingsFormComponent
  ],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslocoModule,
    BiitToggleModule,
    FormsModule,
    BiitButtonModule,
    HasPermissionPipe
  ]
})
export class TournamentExtraSetttingsFormModule { }
