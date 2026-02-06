import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TimerComponent} from "./timer.component";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TranslocoModule} from "@ngneat/transloco";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";


@NgModule({
  declarations: [TimerComponent],
  exports: [
    TimerComponent
  ],
  imports: [
    CommonModule,
    RbacModule,
    MatIconModule,
    DragDropModule,
    TranslocoModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitPopupModule
  ]
})
export class TimerModule {
}
