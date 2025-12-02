import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PictureDialogBoxComponent} from './picture-dialog-box.component';
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [
    PictureDialogBoxComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class PictureDialogBoxModule { }
