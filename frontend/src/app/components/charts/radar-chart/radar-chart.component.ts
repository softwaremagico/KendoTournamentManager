import {Component, Input, OnInit, ViewChild} from '@angular/core';
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
  ApexXAxis,
  ChartComponent
} from "ng-apexcharts";
import {StackedBarsData} from "../stacked-bars-chart/stacked-bars-chart-data";
import {Colors} from "../colors";
import {RadarChartData} from "./radar-chart-data";

type RadarChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  labels: ApexDataLabels;
  fill: ApexFill;
  plotOptions: ApexPlotOptions;
  xaxis: ApexXAxis;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  markers: ApexMarkers;
  stroke: ApexStroke;
};

@Component({
  selector: 'app-radar-chart',
  templateUrl: './radar-chart.component.html',
  styleUrls: ['./radar-chart.component.scss']
})
export class RadarChartComponent implements OnInit {

  @ViewChild('chart')
  chart!: ChartComponent;

  public chartOptions: RadarChartOptions;

  @Input()
  public data: RadarChartData;
  @Input()
  public width: number = 600;
  @Input()
  public radarSize: number = 140;
  @Input()
  public showToolbar: boolean = true;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public showValuesLabels: boolean = false;
  @Input()
  public xAxisTitle: string | undefined = undefined;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: "left" | "center" | "right" = "center";
  @Input()
  public fill: "gradient" | "solid" | "pattern" | "image" = "solid";
  @Input()
  public shadow: boolean = true;
  @Input()
  public opacity: number = 0.4;
  @Input()
  public strokeWidth: number = 5;
  @Input()
  public innerColors: string[] = ["#ffffff"]
  @Input()
  public legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"

  ngOnInit() {
    this.chartOptions = {
      chart: {
        width: this.width,
        type: "radar",
        toolbar: {
          show: this.showToolbar,
        },
        dropShadow: {
          enabled: this.shadow,
          color: '#000',
          blur: 1,
          left: 1,
          top: 1
        },
      },
      series: this.setColors(this.data.getData()),
      labels: {
        enabled: this.showValuesLabels
      },
      fill: {
        type: this.fill,
        opacity: this.opacity,
      },
      markers: {
        size: 0
      },
      stroke: {
        width: this.strokeWidth
      },
      plotOptions: {
        radar: {
          size: this.radarSize,
          polygons: {
            fill: {
              colors: this.innerColors
            }
          }
        }
      },
      xaxis: {
        categories: this.data.getLabels()
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

  update(data: RadarChartData) {
    this.chart.updateSeries(data.getData());
  }

  setColors(data: StackedBarsData[]): StackedBarsData[] {
    for (let i = 0; i < data.length; i++) {
      data[i].color = this.colors[i % this.colors.length];
    }
    return data;
  }
}
