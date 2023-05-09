import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ScoreComponent} from "./score.component";
import {MatMenuModule} from "@angular/material/menu";
import {TranslateModule} from "@ngx-translate/core";
import {MatTooltipModule} from "@angular/material/tooltip";


@NgModule({
  declarations: [ScoreComponent],
  exports: [
    ScoreComponent
  ],
  imports: [
    CommonModule,
    MatMenuModule,
    TranslateModule,
    MatTooltipModule
  ]
})
export class ScoreModule {
}
