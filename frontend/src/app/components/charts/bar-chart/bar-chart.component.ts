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
import {BarChartData} from "./bar-chart-data";
import {CartesianChartComponent} from "../cartesian-chart-component";
import {ApexTheme} from "../apex-types";


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
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent extends CartesianChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: BarChartOptions;

  @Input()
  public data: BarChartData;

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCartesianChartOptions('bar', this.data.getData(), this.getPlotOptions(),
        this.getXAxis(this.data.getLabels(), this.xAxisOnTop ? 'top' : 'bottom', this.xAxisTitle))
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return this.getBarPlotOptions(true);
  }

  update(data: BarChartData) {
    this.chart.updateSeries(data.getData());
  }
}
