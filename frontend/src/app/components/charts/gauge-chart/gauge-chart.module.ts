import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {GaugeChartComponent} from './gauge-chart.component';
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [
    GaugeChartComponent
  ],
  exports: [
    GaugeChartComponent
  ],
  imports: [
    CommonModule,
    NgApexchartsModule
  ]
})
export class GaugeChartModule { }
