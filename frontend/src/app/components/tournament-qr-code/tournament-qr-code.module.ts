import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentQrCodeComponent} from "./tournament-qr-code.component";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitActionButtonModule, BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [TournamentQrCodeComponent],
  exports: [TournamentQrCodeComponent],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatButtonModule,
    RbacModule,
    MatCheckboxModule,
    MatSpinnerOverlayModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitActionButtonModule,
    BiitIconButtonModule
  ]
})
export class TournamentQrCodeModule {
}
