import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DrawPipe} from "./draw.pipe";


@NgModule({
  declarations: [DrawPipe],
  exports: [
    DrawPipe
  ],
  imports: [
    CommonModule
  ]
})
export class DrawPipeModule { }
