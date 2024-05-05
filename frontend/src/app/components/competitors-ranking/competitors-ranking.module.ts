import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CompetitorsRankingComponent} from "./competitors-ranking.component";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";


@NgModule({
  declarations: [CompetitorsRankingComponent],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslateModule,
    MatIconModule,
    RbacModule,
    MatDialogModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    MatTooltipModule,
    ReactiveFormsModule,
    MatSlideToggleModule
  ]
})
export class CompetitorsRankingModule {
}
