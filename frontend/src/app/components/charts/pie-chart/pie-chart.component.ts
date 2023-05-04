import {Component, Input, ViewChild} from '@angular/core';

import {
  ApexChart,
  ApexFill,
  ApexLegend,
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexTitleSubtitle,
  ChartComponent
} from "ng-apexcharts";
import {PieChartData} from "./pie-chart-data";
import {Colors} from "../colors";
import {DarkModeService} from "../../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../../services/user-session.service";
import {CustomChartComponent} from "../CustomChartComponent";


type PieChartOptions = {
  series: ApexNonAxisChartSeries;
  colors: string [];
  chart: ApexChart;
  fill: ApexFill;
  responsive: ApexResponsive[];
  labels: string[];
  title: ApexTitleSubtitle;
  legend: ApexLegend;
};

@Component({
  selector: 'app-pie-chart',
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent extends CustomChartComponent {

  @ViewChild('chart')
  private chart!: ChartComponent;

  public chartOptions: PieChartOptions;

  @Input()
  public data: PieChartData;
  @Input()
  public width: number = 500;
  @Input()
  public showToolbar: boolean = true;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public isDonut: boolean = false;
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public shadow: boolean = true;
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"

  constructor(darkModeService: DarkModeService, userSessionService: UserSessionService) {
    super(darkModeService, userSessionService);
  }

  protected setProperties(): void {
    this.chartOptions = {
      colors: this.colors,
      chart: {
        width: this.width,
        type: this.isDonut ? "donut" : "pie",
        dropShadow: {
          enabled: this.shadow,
          color: '#000',
          top: -5,
          left: 7,
          blur: 8,
          opacity: 0.2
        },
        toolbar: {
          show: this.showToolbar,
        },
      },
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      fill: {
        type: this.fill,
      },
      responsive: [
        {
          breakpoint: 480,
          options: {
            chart: {
              width: 200
            },
            legend: {
              position: this.legendPosition
            }
          }
        }
      ],
      title: {
        text: this.title,
        align: this.titleAlignment,
        style: {
          fontSize: '14px',
          fontWeight: 'bold',
          fontFamily: 'Roboto',
          color: this.titleTextColor
        },
      },
      legend: {
        position: this.legendPosition,
        labels: {
          colors: this.legendTextColor,
          useSeriesColors: false
        },
      },
    };
  }

  update(data: PieChartData) {
    this.chart.updateSeries(data.getData());
  }

}
