import {Component, Input, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle,
  ApexXAxis,
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {BarChartData} from "./bar-chart-data";
import {CustomChartComponent} from "../CustomChartComponent";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";


type BarChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  fill: ApexFill;
  chart: ApexChart;
  labels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
};

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: BarChartOptions;

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
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public borderRadius: number = 0;
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"
  @Input()
  public shadow: boolean = true;

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      chart: {
        width: this.width,
        type: "bar",
        toolbar: {
          show: this.showToolbar,
        },
        dropShadow: {
          enabled: this.shadow,
          color: '#000',
          top: 18,
          left: 7,
          blur: 10,
          opacity: 0.2
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
          borderRadius: this.borderRadius
        }
      },
      xaxis: {
        categories: this.data.getLabels(),
        position: this.xAxisOnTop ? 'top' : 'bottom',
        title: {
          text: this.xAxisTitle
        },
        labels: {
          style: {
            fontFamily: 'Roboto',
            colors: this.axisTextColor
          },
        },
      },
      yaxis: {
        show: this.showYAxis,
        title: {
          text: this.yAxisTitle
        },
        labels: {
          style: {
            fontFamily: 'Roboto',
            colors: this.axisTextColor
          },
        },
      },
      title: {
        text: this.title,
        align: this.titleAlignment,
        style: {
          fontSize: '14px',
          fontWeight: 'bold',
          fontFamily: 'Roboto',
          color: this.titleTextColor
        },
      },
      legend: {
        position: this.legendPosition,
        labels: {
          colors: this.legendTextColor,
          useSeriesColors: false
        },
      },
    };
  }

  update(data: BarChartData) {
    this.chart.updateSeries(data.getData());
  }
}
