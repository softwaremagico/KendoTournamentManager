import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightComponent} from "./fight.component";
import {DuelModule} from "./duel/duel.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TranslocoModule} from "@ngneat/transloco";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";


@NgModule({
  declarations: [FightComponent],
  exports: [
    FightComponent
  ],
    imports: [
        CommonModule,
        DuelModule,
        MatIconModule,
        RbacModule,
        DragDropModule,
        TranslocoModule,
        HasPermissionPipe
    ]
})
export class FightModule {
}
