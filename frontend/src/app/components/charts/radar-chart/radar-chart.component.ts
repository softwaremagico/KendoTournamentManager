import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
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
import {RadarChartData} from "./radar-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {ApexTheme} from "../apex-types";

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
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
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
  public radarSize: number = 140;
  @Input()
  public showValuesLabels: boolean = false;
  @Input()
  public xAxisTitle: string | undefined = undefined;
  @Input()
  public opacity: number = 0.4;
  @Input()
  public strokeWidth: number = 5;
  @Input()
  public innerColors: string[] = ["#ffffff"]

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCommonChartOptions('radar', this.opacity),
      series: this.setColors(this.data.getData()),
      labels: this.getLabels(this.showValuesLabels),
      plotOptions: this.getPlotOptions(),
      xaxis: this.getXAxis(this.data.getLabels(), undefined, this.xAxisTitle),
      markers: this.getMarkers(),
      stroke: this.getStroke(this.strokeWidth)
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
    return this.setSeriesColors(data);
  }
}
