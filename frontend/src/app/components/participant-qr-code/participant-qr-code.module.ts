import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantQrCodeComponent} from './participant-qr-code.component';
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [ParticipantQrCodeComponent],
  exports: [ParticipantQrCodeComponent],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    RbacModule,
    MatCheckboxModule,
    MatButtonModule,
    MatSpinnerOverlayModule,
    HasPermissionPipe,
    BiitButtonModule
  ]
})
export class ParticipantQrCodeModule {
}
