import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentRolesComponent} from "./tournament-roles.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserListModule} from "../../../components/basic/user-list/user-list.module";
import {TranslocoModule} from "@ngneat/transloco";
import {UserCardModule} from "../../../components/user-card/user-card.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FightStatisticsPanelModule} from "../../../components/fight-statistics-panel/fight-statistics-panel.module";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";


@NgModule({
    declarations: [TournamentRolesComponent],
    exports: [
        TournamentRolesComponent
    ],
  imports: [
    CommonModule,
    DragDropModule,
    UserListModule,
    TranslocoModule,
    UserCardModule,
    MatIconModule,
    RbacModule,
    MatSpinnerOverlayModule,
    MatDialogModule,
    MatButtonModule,
    FightStatisticsPanelModule,
    HasPermissionPipe,
    BiitButtonModule
  ]
})
export class TournamentRolesModule {
}
