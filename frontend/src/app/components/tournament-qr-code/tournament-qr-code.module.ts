import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentQrCodeComponent} from "./tournament-qr-code.component";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";


@NgModule({
  declarations: [TournamentQrCodeComponent],
  exports: [TournamentQrCodeComponent],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatTooltipModule,
    MatButtonModule,
    RbacModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSpinnerOverlayModule
  ]
})
export class TournamentQrCodeModule {
}
