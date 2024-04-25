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
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {LineChartData} from "./line-chart-data";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {CustomChartComponent} from "../custom-chart-component";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";


export type LineChartOptions = {
  series: ApexAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  labels: ApexDataLabels;
  fill: ApexFill;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
  stroke: ApexStroke;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  markers: ApexMarkers;
  theme: ApexTheme;
};

type UpdateLineChartOptions = {
  xaxis: ApexXAxis;
};

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: LineChartOptions;

  @Input()
  public data: LineChartData;
  @Input()
  public height: number | undefined = undefined;
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
  public curve: "straight" | "smooth" | "stepline" = "smooth";
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom";
  @Input()
  public shadow: boolean = true;
  @Input()
  public strokeWidth: number = 5;

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }


  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      series: this.data.getData(),
      chart: this.getChart('line', this.width, this.height, this.shadow, this.showToolbar),
      labels: this.getLabels(this.showValuesLabels),
      fill: this.getFill(this.fill),
      plotOptions: this.getPlotOptions(),
      tooltip: this.getTooltip(),
      xaxis: this.getXAxis(this.data.getLabels(), this.xAxisOnTop ? 'top' : 'bottom', this.xAxisTitle),
      yaxis: this.getYAxis(this.showYAxis, this.yAxisTitle),
      stroke: this.getStroke(this.strokeWidth, this.curve),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      markers: this.getMarkers(),
      theme:this.getTheme()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return {
      bar: {
        distributed: true, // this line is mandatory for using colors
        horizontal: this.horizontal,
        barHeight: this.barThicknessPercentage + '%',
        columnWidth: this.barThicknessPercentage + '%',
      }
    }
  }

  update(data: LineChartData) {
    this.chart.updateSeries(data.getData());
    const updateOptions: UpdateLineChartOptions = {
      xaxis: {
        categories: data.getLabels(),
        position: this.xAxisOnTop ? 'top' : 'bottom',
        title: {
          text: this.xAxisTitle
        }
      }
    }
    this.chart.updateOptions(updateOptions);
  }
}
