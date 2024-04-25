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
import {StackedBarChartData, StackedBarsData} from "./stacked-bars-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";


type StackedBarsChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  labels: ApexDataLabels;
  fill: ApexFill;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  theme: ApexTheme;
};

type UpdateBarsChartOptions = {
  xaxis: ApexXAxis;
};

@Component({
  selector: 'app-stacked-bars-chart',
  templateUrl: './stacked-bars-chart.component.html',
  styleUrls: ['./stacked-bars-chart.component.scss']
})
export class StackedBarsChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: StackedBarsChartOptions;

  @Input()
  public data: StackedBarChartData;
  @Input()
  public width: number = 500;
  @Input()
  public height: number | undefined = undefined;
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
  public enableTotals: boolean = true;
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom";
  @Input()
  public shadow: boolean = true;
  @Input()
  public stackType: '100%' | 'normal' = "normal";
  @Input()
  public stacked: boolean = true;

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      chart: this.getChart('bar', this.width, this.height, this.shadow, this.showToolbar),
      series: this.setColors(this.data.getData()),
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
        distributed: false, // this line is mandatory for using colors
        horizontal: this.horizontal,
        barHeight: this.barThicknessPercentage + '%',
        columnWidth: this.barThicknessPercentage + '%',
        borderRadius: this.borderRadius,
        dataLabels: {
          total: {
            enabled: this.enableTotals,
            style: {
              fontWeight: 900
            }
          }
        }
      }
    }
  }

  update(data: StackedBarChartData) {
    this.chart.updateSeries(this.setColors(data.getData()), true);
    const updateOptions: UpdateBarsChartOptions = {
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

  setColors(data: StackedBarsData[]): StackedBarsData[] {
    for (let i = 0; i < data.length; i++) {
      data[i].color = this.colors[i % this.colors.length];
    }
    return data;
  }
}
