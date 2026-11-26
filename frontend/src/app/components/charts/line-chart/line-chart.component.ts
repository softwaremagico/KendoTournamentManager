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
  ApexYAxis,
  ChartComponent
} from "ng-apexcharts";
import {LineChartData} from "./line-chart-data";
import {CartesianChartComponent} from "../cartesian-chart-component";
import {ApexTheme} from "../apex-types";


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

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent extends CartesianChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: LineChartOptions;

  @Input()
  public data: LineChartData;
  @Input()
  public curve: "straight" | "smooth" | "stepline" = "smooth";
  @Input()
  public strokeWidth: number = 5;

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCartesianChartOptions('line', this.data.getData(), this.getPlotOptions(),
        this.getXAxis(this.data.getLabels(), this.xAxisOnTop ? 'top' : 'bottom', this.xAxisTitle)),
      stroke: this.getStroke(this.strokeWidth, this.curve),
      markers: this.getMarkers()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return this.getBarPlotOptions(true);
  }

  update(data: LineChartData) {
    this.chart.updateSeries(data.getData());
    this.chart.updateOptions(this.getXAxisUpdateOptions(data.getLabels()));
  }
}
