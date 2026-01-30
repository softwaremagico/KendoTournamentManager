import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UntieTeamsComponent} from "./untie-teams.component";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {MatTabsModule} from "@angular/material/tabs";
import {MemberSelectorModule} from "../basic/member-selector/member-selector.module";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitTabGroupModule} from "@biit-solutions/wizardry-theme/navigation";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [UntieTeamsComponent],
  exports: [
    UntieTeamsComponent
  ],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    MatTabsModule,
    MemberSelectorModule,
    TranslocoModule,
    RbacModule,
    MatButtonModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitTabGroupModule
  ]
})
export class UndrawTeamsModule {
}
