import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatSpinnerOverlayComponent} from "./mat-spinner-overlay.component";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {TranslocoModule} from '@jsverse/transloco';


@NgModule({
  declarations: [MatSpinnerOverlayComponent],
  exports: [
    MatSpinnerOverlayComponent
  ],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    TranslocoModule
  ]
})
export class MatSpinnerOverlayModule { }
