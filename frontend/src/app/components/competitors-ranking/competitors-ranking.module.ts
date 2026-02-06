import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CompetitorsRankingComponent} from "./competitors-ranking.component";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {MatButtonModule} from "@angular/material/button";


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
    MatButtonModule,
    FormsModule,
    MatInputModule,
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
