import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightStatisticsPanelComponent} from "./fight-statistics-panel.component";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [FightStatisticsPanelComponent],
  exports: [
    FightStatisticsPanelComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatTooltipModule,
    TranslocoModule,
  ]
})
export class FightStatisticsPanelModule { }
