import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexPlotOptions,
  ApexXAxis,
  ApexYAxis,
  ApexStroke,
  ChartComponent, ApexTitleSubtitle
} from "ng-apexcharts";
import {Colors} from "../colors";
import {LineChartData} from "./line-chart-data";


export type ChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  labels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  stroke: ApexStroke;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
};

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent implements AfterViewInit {

  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: ChartOptions;

  @Input()
  public data: LineChartData;
  @Input()
  public width: number = 500;
  @Input()
  public showToolbar: boolean = true;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public horizontal: boolean = false;
  @Input()
  public barThicknessPercentage: number = 75;
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
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right";

  constructor() {
    this.chartOptions = {
      series: [],
      colors: [],
      chart: {
        width: this.width,
        type: "line"
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
      },
      stroke: {},
      title: {
        text: "Product Trends by Month",
        align: "left"
      },
    };
  }


  ngAfterViewInit() {
    this.chartOptions = {
      colors: this.colors,
      series: this.data.getData(),
      chart: {
        width: this.width,
        type: "line",
        toolbar: {
          show: this.showToolbar,
        },
      },
      labels: {
        enabled: this.showValuesLabels
      },
      plotOptions: {
        bar: {
          distributed: true, // this line is mandatory for using colors
          horizontal: this.horizontal,
          barHeight: this.barThicknessPercentage + '%',
          columnWidth: this.barThicknessPercentage + '%',
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
      },
      stroke: {
        curve: "straight"
      },
      title: {
        text: this.title,
        align: this.titleAlignment
      },
    };
  }
}
