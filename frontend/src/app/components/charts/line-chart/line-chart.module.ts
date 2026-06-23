import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LineChartComponent} from "./line-chart.component";
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [LineChartComponent],
  exports: [LineChartComponent],
  imports: [
    CommonModule,
    NgApexchartsModule
  ]
})
export class LineChartModule {
}
