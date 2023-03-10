import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ScoreChartComponent} from "./score-chart.component";


@NgModule({
  declarations: [ScoreChartComponent],
  exports: [ScoreChartComponent],
  imports: [
    CommonModule
  ]
})
export class ScoreChartModule {
}
