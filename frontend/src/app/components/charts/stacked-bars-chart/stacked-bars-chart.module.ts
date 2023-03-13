import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StackedBarsChartComponent} from "./stacked-bars-chart.component";


@NgModule({
  declarations: [StackedBarsChartComponent],
  exports: [StackedBarsChartComponent],
  imports: [
    CommonModule
  ]
})
export class StackedBarsChartModule {
}
