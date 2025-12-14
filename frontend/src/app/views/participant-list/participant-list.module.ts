import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {BasicTableModule} from "../../components/basic/basic-table/basic-table.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatButtonModule} from "@angular/material/button";
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

@NgModule({
  declarations: [ParticipantListComponent],
  exports: [ParticipantListComponent],
    imports: [
        ParticipantRoutingModule,
        CommonModule,
        MatIconModule,
        TranslocoModule,
        BasicTableModule,
        MatTooltipModule,
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
        ParticipantQrCodeModule
    ]
})
export class ParticipantListModule {
}
