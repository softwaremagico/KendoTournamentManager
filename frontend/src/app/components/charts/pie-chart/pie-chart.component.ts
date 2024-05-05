import {Component, Input, ViewChild} from '@angular/core';

import {
  ApexChart,
  ApexFill, ApexGrid,
  ApexLegend,
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexTitleSubtitle,
  ApexTooltip,
  ChartComponent
} from "ng-apexcharts";
import {PieChartData} from "./pie-chart-data";
import {Colors} from "../colors";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {CustomChartComponent} from "../custom-chart-component";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";


type PieChartOptions = {
  series: ApexNonAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  fill: ApexFill;
  tooltip: ApexTooltip;
  responsive: ApexResponsive[];
  labels: string[];
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  theme: ApexTheme;
};

@Component({
  selector: 'app-pie-chart',
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  private chart!: ChartComponent;

  public chartOptions: PieChartOptions;

  @Input()
  public data: PieChartData;
  @Input()
  public width: number = 500;
  @Input()
  public height: number | undefined = undefined;
  @Input()
  public showToolbar: boolean = false;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public isDonut: boolean = false;
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public shadow: boolean = true;
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      chart: this.getChart(this.isDonut ? "donut" : "pie", this.width, this.height, this.shadow, this.showToolbar),
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      fill: this.getFill(this.fill),
      tooltip: this.getTooltip(),
      responsive: this.getResponsive(this.legendPosition),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      theme: this.getTheme()
    };
  }

  protected getPlotOptions(): undefined {
    return undefined;
  }

  update(data: PieChartData) {
    this.chart.updateSeries(data.getData());
  }

}
