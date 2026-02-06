import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FloatingDeleteButtonComponent } from './floating-delete-button.component';
import {BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";



@NgModule({
  declarations: [
    FloatingDeleteButtonComponent
  ],
  exports: [
    FloatingDeleteButtonComponent
  ],
  imports: [
    CommonModule,
    BiitIconButtonModule
  ]
})
export class FloatingDeleteButtonModule { }
