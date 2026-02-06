import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FaultComponent} from "./fault.component";
import {MatMenuModule} from "@angular/material/menu";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../../../pipes/rbac-pipe/rbac.module";
import {HasPermissionPipe} from "../../../../../pipes/has-permission.pipe";


@NgModule({
  declarations: [FaultComponent],
  exports: [
    FaultComponent
  ],
    imports: [
        CommonModule,
        MatMenuModule,
        TranslocoModule,
        MatIconModule,
        RbacModule,
        HasPermissionPipe
    ]
})
export class FaultModule { }
