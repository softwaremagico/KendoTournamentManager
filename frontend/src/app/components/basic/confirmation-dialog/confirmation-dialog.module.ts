import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ConfirmationDialogComponent} from "./confirmation-dialog.component";
import {MatIconModule} from "@angular/material/icon";
import {TranslateModule} from "@ngx-translate/core";



@NgModule({
  declarations: [ConfirmationDialogComponent],
  imports: [
    CommonModule,
    MatIconModule,
    TranslateModule
  ]
})
export class ConfirmationDialogModule { }
