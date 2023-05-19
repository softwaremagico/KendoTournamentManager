import {Component, Input, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {BarChartData} from "./bar-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";


type BarChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  fill: ApexFill;
  chart: ApexChart;
  labels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  theme: ApexTheme;
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
      chart: this.getChart('bar', this.width, this.shadow, this.showToolbar),
      series: this.data.getData(),
      labels: this.getLabels(this.showValuesLabels),
      fill: this.getFill(this.fill),
      plotOptions: this.getPlotOptions(),
      tooltip: this.getTooltip(),
      xaxis: this.getXAxis(this.data.getLabels(), this.xAxisOnTop ? 'top' : 'bottom', this.xAxisTitle),
      yaxis: this.getYAxis(this.showYAxis, this.yAxisTitle),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      theme: this.getTheme()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return {
      bar: {
        distributed: true, // this line is mandatory for using colors
        horizontal: this.horizontal,
        barHeight: this.barThicknessPercentage + '%',
        columnWidth: this.barThicknessPercentage + '%',
        borderRadius: this.borderRadius
      }
    }
  }

  update(data: BarChartData) {
    this.chart.updateSeries(data.getData());
  }
}
