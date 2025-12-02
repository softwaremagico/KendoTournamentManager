import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentImageSelectorComponent} from "./tournament-image-selector.component";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSpinnerOverlayModule} from "../../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {FormsModule} from "@angular/forms";
import {MatLegacySliderModule} from "@angular/material/legacy-slider";


@NgModule({
  declarations: [TournamentImageSelectorComponent],
  exports: [
    TournamentImageSelectorComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    RbacModule,
    TranslocoModule,
    MatButtonModule,
    MatDialogModule,
    MatSpinnerOverlayModule,
    MatLegacySliderModule,
    FormsModule
  ]
})
export class TournamentImageSelectorModule {
}
