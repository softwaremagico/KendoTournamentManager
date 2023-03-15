import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';

import {ApexChart, ApexNonAxisChartSeries, ApexResponsive, ChartComponent} from "ng-apexcharts";
import {PieChartData} from "./pie-chart-data";
import {Colors} from "../colors";


export type ChartOptions = {
  series: ApexNonAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  responsive: ApexResponsive[];
  labels: string[];
};

@Component({
  selector: 'app-pie-chart',
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent implements AfterViewInit {

  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: ChartOptions;

  @Input()
  public data: PieChartData;
  @Input()
  public width: number = 500;
  @Input()
  public colors: string[] = Colors.defaultPalette;

  constructor() {
    this.chartOptions = {
      series: [],
      colors: [],
      labels: [],
      chart: {
        width: this.width,
        type: "pie"
      },
      responsive: []
    };
  }


  ngAfterViewInit() {
    this.chartOptions = {
      colors: this.colors,
      series: this.data.getValues(),
      chart: {
        width: this.width,
        type: "pie"
      },

      labels: this.data.getLabels(),
      responsive: [
        {
          breakpoint: 480,
          options: {
            chart: {
              width: 200
            },
            legend: {
              position: "bottom"
            }
          }
        }
      ]
    };
  }
}
