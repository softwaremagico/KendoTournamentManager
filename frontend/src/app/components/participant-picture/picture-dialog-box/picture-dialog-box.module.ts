import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PictureDialogBoxComponent} from './picture-dialog-box.component';
import {MatIconModule} from "@angular/material/icon";
import {TranslateModule} from "@ngx-translate/core";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [
    PictureDialogBoxComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    TranslateModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class PictureDialogBoxModule { }
