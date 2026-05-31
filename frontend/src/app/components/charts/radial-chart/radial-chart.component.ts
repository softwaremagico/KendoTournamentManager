import {Component, Input, ViewChild} from '@angular/core';
import {
  ApexChart,
  ApexFill,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle,
  ApexTooltip,
  ChartComponent
} from "ng-apexcharts";
import {Colors} from "../colors";
import {RadialChartData} from "./radial-chart-data";
import {CustomChartComponent} from "../custom-chart-component";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {ApexTheme} from "ng-apexcharts/lib/model/apex-types";

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
  public width: number = 500;
  @Input()
  public height: number | undefined = undefined;
  @Input()
  public showToolbar: boolean = false;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "gradient";
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"
  @Input()
  public shadow: boolean = true;
  @Input()
  public innerCirclePercentage: number = 40;
  @Input()
  public startAngle: number = 0;
  @Input()
  public endAngle: number = 360;

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
    if (userSessionService.getNightMode()) {
      this.colors = Colors.defaultPaletteNightMode;
    }
  }


  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      chart: this.getChart('radialBar', this.width, this.height, this.shadow, this.showToolbar),
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      fill: this.getFill(this.fill),
      plotOptions: this.getPlotOptions(),
      tooltip: this.getTooltip(),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      theme: this.getTheme()
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
