import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApexChart, ApexFill, ApexLegend, ApexPlotOptions, ApexTitleSubtitle, ChartComponent} from "ng-apexcharts";
import {Colors} from "../colors";
import {RadialChartData} from "./radial-chart-data";

type RadialChartOptions = {
  series: number[];
  colors: string [];
  labels: string[];
  fill: ApexFill;
  chart: ApexChart;
  plotOptions: ApexPlotOptions;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
};


@Component({
  selector: 'app-radial-chart',
  templateUrl: './radial-chart.component.html',
  styleUrls: ['./radial-chart.component.scss']
})
export class RadialChartComponent implements OnInit {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: RadialChartOptions;

  @Input()
  public data: RadialChartData;
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


  ngOnInit() {
    this.chartOptions = {
      colors: this.colors,
      chart: {
        width: this.width,
        type: "radialBar",
        toolbar: {
          show: this.showToolbar,
        },
        dropShadow: {
          enabled: this.shadow,
          color: '#000',
          top: 18,
          left: 7,
          blur: 10,
          opacity: 0.2
        },
      },
      series: this.data.getValues(),
      labels: this.data.getLabels(),
      fill: {
        type: this.fill,
        gradient: {
          shade: "light",
          shadeIntensity: 0.4,
          inverseColors: false,
          opacityFrom: 1,
          opacityTo: 1,
          stops: [0, 50, 53, 91]
        }
      },
      plotOptions: {
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
      },
      title: {
        text: this.title,
        align: this.titleAlignment
      },
      legend: {
        position: this.legendPosition
      },
    };
  }
}
