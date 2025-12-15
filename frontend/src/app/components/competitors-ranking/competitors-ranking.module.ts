import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CompetitorsRankingComponent} from "./competitors-ranking.component";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";


@NgModule({
    declarations: [CompetitorsRankingComponent],
    exports: [
        CompetitorsRankingComponent
    ],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslocoModule,
    MatIconModule,
    RbacModule,
    MatDialogModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    MatTooltipModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitIconButtonModule,
    BiitInputTextModule,
    MapGetPipeModule
  ]
})
export class CompetitorsRankingModule {
}
