import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentExtraPropertiesComponent} from './tournament-extra-properties.component';
import {MatSpinnerOverlayModule} from "../../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [
    TournamentExtraPropertiesComponent
  ],
  exports: [
    TournamentExtraPropertiesComponent
  ],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    MatDialogModule,
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTooltipModule,
    TranslateModule,
  ]
})
export class TournamentExtraPropertiesModule {
}
