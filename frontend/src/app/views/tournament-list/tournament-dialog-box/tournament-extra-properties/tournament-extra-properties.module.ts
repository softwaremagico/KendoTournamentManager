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
import {TranslocoModule} from "@ngneat/transloco";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";


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
    MatButtonModule,
    MatSlideToggleModule,
    MatTooltipModule,
    TranslocoModule,
    ReactiveFormsModule,
    FormsModule,
  ]
})
export class TournamentExtraPropertiesModule {
}
