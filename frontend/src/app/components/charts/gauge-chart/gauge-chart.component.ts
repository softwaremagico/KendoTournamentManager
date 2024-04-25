import {Component, Input, ViewChild} from '@angular/core';
import {ApexChart, ApexFill, ApexPlotOptions, ApexTitleSubtitle, ApexTooltip, ChartComponent} from "ng-apexcharts";
import {Colors} from "../colors";
import {GaugeChartData} from "./gauge-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";

type GaugeChartOptions = {
  series: number[];
  colors: string [];
  labels: string[];
  fill: ApexFill;
  chart: ApexChart;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
  title: ApexTitleSubtitle;
  theme: ApexTheme;
};

@Component({
  selector: 'app-gauge-chart',
  templateUrl: './gauge-chart.component.html',
  styleUrls: ['./gauge-chart.component.scss']
})
export class GaugeChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: GaugeChartOptions;

  @Input()
  public data: GaugeChartData;
  @Input()
  public width: number = 500;
  @Input()
  public height: number | undefined = undefined;
  @Input()
  public showToolbar: boolean = true;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public opacity: number = 1;
  @Input()
  public shadow: boolean = true;
  @Input()
  public innerCirclePercentage: number = 50;
  @Input()
  public trackBackgroundColor: string = "#e7e7e7";
  @Input()
  public trackBackgroundThicknessPercentage: number = 97;

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      chart: this.getChart('radialBar', this.width, this.height, this.shadow, this.showToolbar),
      series: this.data?.getValues(),
      labels: this.data?.getLabels(),
      fill: this.getFill(this.fill, this.opacity),
      plotOptions: this.getPlotOptions(),
      tooltip: this.getTooltip(),
      title: this.getTitle(this.title, this.titleAlignment),
      theme:this.getTheme()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return {
      radialBar: {
        startAngle: -90,
        endAngle: 90,
        track: {
          background: this.trackBackgroundColor,
          strokeWidth: this.trackBackgroundThicknessPercentage + "%",
          dropShadow: {
            enabled: true,
            top: 2,
            left: 0,
            opacity: 0.31,
            blur: 2
          }
        },
        offsetY: -20,
        dataLabels: {
          name: {
            show: true,
            offsetY: -15,
            fontSize: "16px"
          },
          value: {
            show: true,
            offsetY: -15,
            fontSize: "12px"
          }
        },
        hollow: {
          size: this.innerCirclePercentage + "%"
        }
      }
    }
  }

  update(data: GaugeChartData) {
    this.chart.updateSeries(data.getData());
  }
}
