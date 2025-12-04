import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TimerComponent} from "./timer.component";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TranslocoModule} from "@ngneat/transloco";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";


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
        HasPermissionPipe
    ]
})
export class TimerModule {
}
