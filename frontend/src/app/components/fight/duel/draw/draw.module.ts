import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DrawComponent} from "./draw.component";
import {DrawPipeModule} from "../../../../pipes/draw-pipe/draw-pipe.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [DrawComponent],
  exports: [
    DrawComponent
  ],
  imports: [
    CommonModule,
    DrawPipeModule,
    TranslateModule
  ]
})
export class DrawModule {
}
