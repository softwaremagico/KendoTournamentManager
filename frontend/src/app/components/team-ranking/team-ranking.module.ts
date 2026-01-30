import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TeamRankingComponent} from "./team-ranking.component";
import {TranslocoModule} from "@ngneat/transloco";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {UndrawTeamsModule} from "../undraw-teams/undraw-teams.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [TeamRankingComponent],
  exports: [
    TeamRankingComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    MatSpinnerOverlayModule,
    RbacModule,
    MatIconModule,
    MatButtonModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitPopupModule,
    UndrawTeamsModule,
    MatTooltipModule
  ]
})
export class TeamRankingModule {
}
