import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
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
import {StackedBarChartData, StackedBarsData} from "./stacked-bars-chart-data";
import {CartesianChartComponent} from "../cartesian-chart-component";
import {ApexTheme} from "../apex-types";


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

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-stacked-bars-chart',
  templateUrl: './stacked-bars-chart.component.html',
  styleUrls: ['./stacked-bars-chart.component.scss']
})
export class StackedBarsChartComponent extends CartesianChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: StackedBarsChartOptions;

  @Input()
  public data: StackedBarChartData;
  @Input()
  public enableTotals: boolean = true;
  @Input()
  public stackType: '100%' | 'normal' = "normal";
  @Input()
  public stacked: boolean = true;

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCartesianChartOptions('bar', this.setColors(this.data.getData()), this.getPlotOptions(),
        this.getXAxis(this.data.getLabels(), this.xAxisOnTop ? 'top' : 'bottom', this.xAxisTitle))
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return this.getBarPlotOptions(false, this.enableTotals);
  }

  update(data: StackedBarChartData) {
    this.chart.updateSeries(this.setColors(data.getData()), true);
    this.chart.updateOptions(this.getXAxisUpdateOptions(data.getLabels()));
  }

  setColors(data: StackedBarsData[]): StackedBarsData[] {
    return this.setSeriesColors(data);
  }
}
