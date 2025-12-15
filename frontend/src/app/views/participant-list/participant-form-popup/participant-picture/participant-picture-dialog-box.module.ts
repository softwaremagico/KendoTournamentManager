import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantPictureDialogBoxComponent} from "./participant-picture-dialog-box.component";
import {WebcamModule} from "ngx-webcam";
import {RbacModule} from "../../../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {HasPermissionPipe} from "../../../../pipes/has-permission.pipe";
import {BiitActionButtonModule, BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {FloatingDeleteButtonModule} from "../../../../components/floating-delete-button/floating-delete-button.module";


@NgModule({
  declarations: [ParticipantPictureDialogBoxComponent],
  exports: [
    ParticipantPictureDialogBoxComponent
  ],
  imports: [
    CommonModule,
    WebcamModule,
    RbacModule,
    MatIconModule,
    TranslocoModule,
    MatDialogModule,
    MatButtonModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitActionButtonModule,
    BiitIconButtonModule,
    FloatingDeleteButtonModule
  ]
})
export class ParticipantPictureDialogModule { }
