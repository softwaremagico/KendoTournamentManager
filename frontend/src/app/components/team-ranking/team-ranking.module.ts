import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TeamRankingComponent} from "./team-ranking.component";
import {TranslocoModule} from "@ngneat/transloco";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatTooltipModule} from "@angular/material/tooltip";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";


@NgModule({
  declarations: [TeamRankingComponent],
    imports: [
        CommonModule,
        TranslocoModule,
        MatSpinnerOverlayModule,
        RbacModule,
        MatIconModule,
        MatDialogModule,
        MatButtonModule,
        MatTooltipModule,
        HasPermissionPipe
    ]
})
export class TeamRankingModule {
}
