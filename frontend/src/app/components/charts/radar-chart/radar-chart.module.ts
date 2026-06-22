import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RadarChartComponent} from './radar-chart.component';
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [
    RadarChartComponent
  ],
  exports: [
    RadarChartComponent
  ],
  imports: [
    CommonModule,
    NgApexchartsModule
  ]
})
export class RadarChartModule {
}
