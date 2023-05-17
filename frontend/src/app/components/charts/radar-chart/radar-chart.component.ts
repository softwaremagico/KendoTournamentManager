import {Component, Input, ViewChild} from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexMarkers,
  ApexPlotOptions,
  ApexStroke,
  ApexTitleSubtitle,
  ApexTooltip,
  ApexXAxis,
  ChartComponent
} from "ng-apexcharts";
import {StackedBarsData} from "../stacked-bars-chart/stacked-bars-chart-data";
import {Colors} from "../colors";
import {RadarChartData} from "./radar-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";

type RadarChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  labels: ApexDataLabels;
  fill: ApexFill;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
  xaxis: ApexXAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  markers: ApexMarkers;
  stroke: ApexStroke;
  theme: ApexTheme;
};

@Component({
  selector: 'app-radar-chart',
  templateUrl: './radar-chart.component.html',
  styleUrls: ['./radar-chart.component.scss']
})
export class RadarChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: RadarChartOptions;

  @Input()
  public data: RadarChartData;
  @Input()
  public width: number = 600;
  @Input()
  public radarSize: number = 140;
  @Input()
  public showToolbar: boolean = true;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public showValuesLabels: boolean = false;
  @Input()
  public xAxisTitle: string | undefined = undefined;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public shadow: boolean = true;
  @Input()
  public opacity: number = 0.4;
  @Input()
  public strokeWidth: number = 5;
  @Input()
  public innerColors: string[] = ["#ffffff"]
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      chart: this.getChart('radar', this.width, this.shadow, this.showToolbar),
      series: this.setColors(this.data.getData()),
      labels: this.getLabels(this.showValuesLabels),
      fill: this.getFill(this.fill, this.opacity),
      markers: this.getMarkers(),
      stroke: this.getStroke(this.strokeWidth),
      plotOptions: this.getPlotOptions(),
      tooltip: this.getTooltip(),
      xaxis: this.getXAxis(this.data.getLabels()),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      theme: this.getTheme()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return {
      radar: {
        size: this.radarSize,
        polygons: {
          fill: {
            colors: this.innerColors
          }
        }
      }
    }
  }

  update(data: RadarChartData) {
    this.chart.updateSeries(data.getData());
  }

  setColors(data: StackedBarsData[]): StackedBarsData[] {
    for (let i = 0; i < data.length; i++) {
      data[i].color = this.colors[i % this.colors.length];
    }
    return data;
  }
}
