import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CompetitorsRankingComponent} from "./competitors-ranking.component";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [CompetitorsRankingComponent],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslateModule,
    MatIconModule,
    RbacModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class CompetitorsRankingModule { }
