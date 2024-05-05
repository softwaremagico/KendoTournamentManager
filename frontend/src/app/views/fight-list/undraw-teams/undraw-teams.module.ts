import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UndrawTeamsComponent} from "./undraw-teams.component";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatTabsModule} from "@angular/material/tabs";
import {MemberSelectorModule} from "../../../components/basic/member-selector/member-selector.module";
import {TranslateModule} from "@ngx-translate/core";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [UndrawTeamsComponent],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    MatTabsModule,
    MemberSelectorModule,
    TranslateModule,
    RbacModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class UndrawTeamsModule { }
