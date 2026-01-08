import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LeagueGeneratorComponent} from "./league-generator.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TeamListModule} from "../../../components/basic/team-list/team-list.module";
import {TranslocoModule} from "@ngneat/transloco";
import {TeamCardModule} from "../../../components/team-card/team-card.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTooltipModule} from "@angular/material/tooltip";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {
  TournamentExtraPropertiesFormModule
} from "../../../forms/tournament-extra-properties-form/tournament-extra-properties-form.module";


@NgModule({
  declarations: [LeagueGeneratorComponent],
  exports: [
    LeagueGeneratorComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    TeamListModule,
    TranslocoModule,
    TeamCardModule,
    MatIconModule,
    RbacModule,
    MatSpinnerOverlayModule,
    MatDialogModule,
    MatButtonModule,
    MatSlideToggleModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitProgressBarModule,
    BiitPopupModule,
    TournamentExtraPropertiesFormModule,
    BiitIconButtonModule
  ]
})
export class LeagueGeneratorModule { }
