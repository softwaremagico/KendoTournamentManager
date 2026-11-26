import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {
  ApexChart,
  ApexFill,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle,
  ApexTooltip,
  ChartComponent
} from "ng-apexcharts";
import {RadialChartData} from "./radial-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {ApexTheme, ChartFill, LegendPosition, TitleAlignment} from "../apex-types";

type RadialChartOptions = {
  series: number[];
  colors: string [];
  labels: string[];
  fill: ApexFill;
  chart: ApexChart;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  theme: ApexTheme;
};


@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-radial-chart',
  templateUrl: './radial-chart.component.html',
  styleUrls: ['./radial-chart.component.scss']
})
export class RadialChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: RadialChartOptions;

  @Input()
  public data: RadialChartData;
  @Input()
  public override fill: ChartFill = "gradient";
  @Input()
  public innerCirclePercentage: number = 40;
  @Input()
  public startAngle: number = 0;
  @Input()
  public endAngle: number = 360;

  protected setProperties(): void {
    this.chartOptions = {
      ...this.getCommonChartOptions('radialBar'),
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      plotOptions: this.getPlotOptions()
    };
  }

  protected getPlotOptions(): ApexPlotOptions {
    return {
      radialBar: {
        startAngle: this.startAngle,
        endAngle: this.endAngle,
        dataLabels: {
          name: {
            fontSize: "22px"
          },
          value: {
            offsetY: 5,
            fontSize: "16px"
          },
          total: {
            show: true,
            label: "Total",
            color: "#000000",
            formatter: (w: any) => {
              return (Math.round((this.data.getTotal() / this.data.getValues().length) * 100) / 100).toFixed(2) + "%";
            }
          }
        },
        hollow: {
          size: this.innerCirclePercentage + "%"
        }
      }
    }
  }

  update(data: RadialChartData) {
    this.chart.updateSeries(data.getData());
  }
}
