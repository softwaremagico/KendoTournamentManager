import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RadialChartComponent} from './radial-chart.component';
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [
    RadialChartComponent
  ],
  exports: [
    RadialChartComponent
  ],
  imports: [
    CommonModule,
    NgApexchartsModule
  ]
})
export class RadialChartModule {
}
