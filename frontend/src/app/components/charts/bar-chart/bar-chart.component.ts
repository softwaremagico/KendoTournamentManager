import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexGrid,
  ApexPlotOptions,
  ApexXAxis,
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {BarChartData} from "./bar-chart-data";


export type ChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  labels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
};

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent implements AfterViewInit {

  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: ChartOptions;

  @Input()
  public data: BarChartData;
  @Input()
  public width: number = 500;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public horizontal: boolean = false;
  @Input()
  public showValuesLabels: boolean = true;
  @Input()
  public xAxisOnTop: boolean = false;
  @Input()
  public xAxisTitle: string | undefined = undefined;
  @Input()
  public yAxisTitle: string | undefined = undefined;
  @Input()
  public showYAxis: boolean = true;

  constructor() {
    this.chartOptions = {
      series: [],
      colors: [],
      chart: {
        width: this.width,
        type: "bar"
      },
      labels: {
        enabled: false
      },
      plotOptions: {
        bar: {
          horizontal: this.horizontal
        }
      },
      xaxis: {
        categories: []
      },
      yaxis: {
        show: this.showYAxis,
      }
    };
  }


  ngAfterViewInit() {
    this.chartOptions = {
      colors: this.colors,
      series: this.data.getData(),
      chart: {
        width: this.width,
        type: "bar"
      },
      labels: {
        enabled: this.showValuesLabels
      },
      plotOptions: {
        bar: {
          distributed: true, // this line is mandatory for using colors
          horizontal: this.horizontal
        }
      },
      xaxis: {
        categories: this.data.getLabels(),
        position: this.xAxisOnTop ? 'top' : 'bottom',
        title: {
          text: this.xAxisTitle
        }
      },
      yaxis: {
        show: this.showYAxis,
        title: {
          text: this.yAxisTitle
        }
      }
    };
  }
}
