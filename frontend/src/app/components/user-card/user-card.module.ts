import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserCardComponent} from "./user-card.component";
import {MatCardModule} from "@angular/material/card";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {ParticipantPictureModule} from "../participant-picture/participant-picture.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitIconModule} from "@biit-solutions/wizardry-theme/icon";


@NgModule({
  declarations: [
    UserCardComponent
  ],
  exports: [
    UserCardComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    DragDropModule,
    RbacModule,
    TranslocoModule,
    MatIconModule,
    ParticipantPictureModule,
    HasPermissionPipe,
    BiitIconModule,
  ]
})
export class UserCardModule {
}
