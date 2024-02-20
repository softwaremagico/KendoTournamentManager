import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightStatisticsPanelComponent} from "./fight-statistics-panel.component";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [FightStatisticsPanelComponent],
  exports: [
    FightStatisticsPanelComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatTooltipModule,
    TranslateModule,
  ]
})
export class FightStatisticsPanelModule { }
