import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightStatisticsPanelComponent} from "./fight-statistics-panel.component";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {MatTooltipModule} from "@angular/material/tooltip";


@NgModule({
  declarations: [FightStatisticsPanelComponent],
  exports: [
    FightStatisticsPanelComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    TranslocoModule,
    MatTooltipModule,
  ]
})
export class FightStatisticsPanelModule { }
