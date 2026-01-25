import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {ParticipantListComponent} from "./participant-list.component";
import {ParticipantRoutingModule} from "./participant-routing.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitDatatableModule} from "@biit-solutions/wizardry-theme/table";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatDividerModule} from "@angular/material/divider";
import {TournamentFormPopupModule} from "../tournament-list/tournament-form-popup/tournament-form-popup.module";
import {ParticipantFormPopupModule} from "./participant-form-popup/participant-form-popup.module";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {ParticipantQrCodeModule} from "../../components/participant-qr-code/participant-qr-code.module";
import {CompetitorsRankingModule} from "../../components/competitors-ranking/competitors-ranking.module";
import {
  ParticipantPictureDialogModule
} from "./participant-form-popup/participant-picture/participant-picture-dialog-box.module";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [ParticipantListComponent],
  exports: [ParticipantListComponent],
  imports: [
    ParticipantRoutingModule,
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatButtonModule,
    RbacModule,
    MatSpinnerOverlayModule,
    HasPermissionPipe,
    BiitDatatableModule,
    BiitIconButtonModule,
    MatDividerModule,
    TournamentFormPopupModule,
    ParticipantFormPopupModule,
    BiitButtonModule,
    BiitPopupModule,
    ParticipantQrCodeModule,
    CompetitorsRankingModule,
    ParticipantPictureDialogModule
  ]
})
export class ParticipantListModule {
}
