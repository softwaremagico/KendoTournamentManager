import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PieChartComponent} from "./pie-chart.component";
import {NgApexchartsModule} from "ng-apexcharts";


@NgModule({
  declarations: [PieChartComponent],
  exports: [PieChartComponent],
    imports: [
        CommonModule,
        NgApexchartsModule
    ]
})
export class PieChartModule {
}
