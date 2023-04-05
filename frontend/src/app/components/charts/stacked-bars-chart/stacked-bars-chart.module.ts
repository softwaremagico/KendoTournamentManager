import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StackedBarsChartComponent} from "./stacked-bars-chart.component";
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [StackedBarsChartComponent],
  exports: [StackedBarsChartComponent],
  imports: [
    CommonModule,
    NgApexchartsModule
  ]
})
export class StackedBarsChartModule {
}
