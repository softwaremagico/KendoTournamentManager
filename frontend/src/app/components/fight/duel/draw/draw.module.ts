import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DrawComponent} from "./draw.component";
import {DrawPipeModule} from "../../../../pipes/draw-pipe/draw-pipe.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [DrawComponent],
  exports: [
    DrawComponent
  ],
  imports: [
    CommonModule,
    DrawPipeModule,
    TranslocoModule
  ]
})
export class DrawModule {
}
