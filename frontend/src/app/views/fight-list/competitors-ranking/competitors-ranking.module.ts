import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CompetitorsRankingComponent} from "./competitors-ranking.component";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";



@NgModule({
  declarations: [CompetitorsRankingComponent],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslateModule,
    MatIconModule,
    RbacModule
  ]
})
export class CompetitorsRankingModule { }
