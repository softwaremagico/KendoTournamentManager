import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatSpinnerOverlayComponent} from "./mat-spinner-overlay.component";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [MatSpinnerOverlayComponent],
  exports: [
    MatSpinnerOverlayComponent
  ],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    TranslateModule
  ]
})
export class MatSpinnerOverlayModule { }
