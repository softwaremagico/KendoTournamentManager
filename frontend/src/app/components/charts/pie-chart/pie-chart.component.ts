import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';

import {
  ApexChart,
  ApexFill,
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
import {ApexTheme, ChartFill, LegendPosition, TitleAlignment} from "../apex-types";


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
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
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
  public override width: number = 500;
  @Input()
  public override height: number | undefined = undefined;
  @Input()
  public override showToolbar: boolean = false;
  @Input()
  public override colors: string[] = Colors.defaultPalette;
  @Input()
  public override title: string | undefined = undefined;
  @Input()
  public override titleAlignment: TitleAlignment = "center";
  @Input()
  public isDonut: boolean = false;
  @Input()
  public override fill: ChartFill = "solid";
  @Input()
  public override shadow: boolean = true;
  @Input()
  public override legendPosition: LegendPosition = "bottom"

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
    if (userSessionService.getNightMode()) {
      this.colors = Colors.defaultPaletteNightMode;
    }
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

  update(data: PieChartData): void {
    this.chart.updateSeries(data.getData());
  }

  protected override getTooltip(): ApexTooltip {
    return {
      theme: 'dark',
      //fillSeriesColor: true,
      style: {
        fontFamily: 'Roboto',
      }
    }
  }

}
