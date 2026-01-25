import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PictureDialogBoxComponent} from './picture-dialog-box.component';
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [
    PictureDialogBoxComponent
  ],
  exports: [
    PictureDialogBoxComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatButtonModule,
    BiitButtonModule
  ]
})
export class PictureDialogBoxModule {
}
