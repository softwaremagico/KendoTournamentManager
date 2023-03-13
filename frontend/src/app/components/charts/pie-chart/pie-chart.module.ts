import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PieChartComponent} from "./pie-chart.component";


@NgModule({
  declarations: [PieChartComponent],
  exports: [PieChartComponent],
  imports: [
    CommonModule
  ]
})
export class PieChartModule {
}
