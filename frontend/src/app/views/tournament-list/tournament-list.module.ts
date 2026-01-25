import {NgModule} from '@angular/core';
import {TournamentListComponent} from "./tournament-list.component";
import {CommonModule, DatePipe} from "@angular/common";
import {TournamentRoutingModule} from "./tournament-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {BiitDatatableModule} from "@biit-solutions/wizardry-theme/table";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatDividerModule} from "@angular/material/divider";
import {TournamentFormPopupModule} from "./tournament-form-popup/tournament-form-popup.module";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentQrCodeModule} from "../../components/tournament-qr-code/tournament-qr-code.module";
import {TournamentRolesModule} from "./tournament-roles/tournament-roles.module";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {TournamentTeamsModule} from "./tournament-teams/tournament-teams.module";
import {LeagueGeneratorModule} from "../fight-list/league-generator/league-generator.module";
import {RoleSelectorModule} from "../../components/role-selector/role-selector.module";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [TournamentListComponent],
  exports: [TournamentListComponent],
  imports: [
    TournamentRoutingModule,
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatButtonModule,
    RbacModule,
    MatSpinnerOverlayModule,
    BiitDatatableModule,
    HasPermissionPipe,
    BiitIconButtonModule,
    BiitButtonModule,
    MatDividerModule,
    TournamentFormPopupModule,
    BiitPopupModule,
    TournamentQrCodeModule,
    TournamentRolesModule,
    BiitProgressBarModule,
    TournamentTeamsModule,
    LeagueGeneratorModule,
    RoleSelectorModule
  ],
  providers: [
    DatePipe
  ]
})
export class TournamentListModule {
}
