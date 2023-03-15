import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexPlotOptions,
  ApexTitleSubtitle,
  ApexXAxis,
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {BarChartData} from "./bar-chart-data";


export type ChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  fill: ApexFill;
  chart: ApexChart;
  labels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
};

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent implements OnInit {

  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: ChartOptions;

  @Input()
  public data: BarChartData;
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
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public fill:  "gradient" | "solid" | "pattern" | "image" = "solid";


  ngOnInit() {
    this.chartOptions = {
      colors: this.colors,
      chart: {
        width: this.width,
        type: "bar",
        toolbar: {
          show: this.showToolbar,
        },
      },
      series: this.data.getData(),
      labels: {
        enabled: this.showValuesLabels
      },
      fill: {
        type: this.fill,
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
        },
      },
      title: {
        text: this.title,
        align: this.titleAlignment
      },
    };
  }
}
