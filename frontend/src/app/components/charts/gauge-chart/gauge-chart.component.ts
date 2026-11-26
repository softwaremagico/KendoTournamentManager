import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {ApexChart, ApexFill, ApexPlotOptions, ApexTitleSubtitle, ApexTooltip, ChartComponent} from "ng-apexcharts";
import {GaugeChartData} from "./gauge-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {ApexTheme} from "../apex-types";

export type GaugeChartOptions = {
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
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
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
  public opacity: number = 1;
  @Input()
  public innerCirclePercentage: number = 50;
  @Input()
  public trackBackgroundColor: string = "#e7e7e7";
  @Input()
  public trackBackgroundThicknessPercentage: number = 97;

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCommonChartOptions('radialBar', this.opacity),
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      plotOptions: this.getPlotOptions()
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
            fontSize: "12px",
            formatter: function (value: number): string {
              return value.toFixed(2) + "%"
            }
          }
        },
        hollow: {
          size: this.innerCirclePercentage + "%"
        }
      }
    }
  }

  update(data: GaugeChartData): void {
    this.chart.updateSeries(data.getData());
  }
}
