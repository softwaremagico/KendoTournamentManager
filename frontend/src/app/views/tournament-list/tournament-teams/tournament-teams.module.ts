import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentTeamsComponent} from "./tournament-teams.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserListModule} from "../../../components/basic/user-list/user-list.module";
import {MatCardModule} from "@angular/material/card";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule} from "@angular/forms";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FightStatisticsPanelModule} from "../../../components/fight-statistics-panel/fight-statistics-panel.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {BiitActionButtonModule, BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";


@NgModule({
  declarations: [TournamentTeamsComponent],
  exports: [
    TournamentTeamsComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    UserListModule,
    MatCardModule,
    TranslocoModule,
    MatIconModule,
    FormsModule,
    RbacModule,
    MatSpinnerOverlayModule,
    MatDialogModule,
    MatButtonModule,
    FightStatisticsPanelModule,
    MatTooltipModule,
    HasPermissionPipe,
    BiitProgressBarModule,
    BiitButtonModule,
    BiitIconButtonModule,
    BiitActionButtonModule,
  ]
})
export class TournamentTeamsModule {
}
