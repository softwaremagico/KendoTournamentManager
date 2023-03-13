import {AfterViewInit, Component, Input} from '@angular/core';
import {BarChartData} from "../bar-chart/bar-chart-data";
import * as d3 from "d3";
import {ScaleOrdinal} from "d3-scale";
import {v4 as uuid} from "uuid";
import {PieChartData} from "./pie-chart-data";

@Component({
  selector: 'app-pie-chart',
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent implements AfterViewInit {

  @Input()
  public title: string = "Bar Chart";
  @Input()
  public chartData: BarChartData[];
  @Input()
  private margin: number = 30;
  @Input()
  public width: number = 750;
  @Input()
  public height: number = 400;
  @Input()
  public colors: string[] = [
    "#fd7f6f",
    "#7eb0d5",
    "#b2e061",
    "#bd7ebe",
    "#ffb55a",
    "#ffee65",
    "#beb9db",
    "#fdcce5",
    "#8bd3c7"
  ];

  //Separation between pie slices.
  @Input()
  public strokeColor: string = "#121926";
  @Input()
  public strokeWidth: number = 2;

  //How separated are the labels from the center.
  @Input()
  public labelRadius: number = 100;

  //Inner Radius is for creating a donut chart.
  @Input()
  public innerRadius: number = 0;

  public uniqueId: string = "id" + uuid();

  private svg: any;

  private radius: number;

  ngAfterViewInit() {
    this.radius = Math.min(this.width, this.height) / 2 - this.margin;
    this.createSvg();
    this.drawPie(this.chartData);
  }

  private getMaxY(): number {
    return Math.max(...this.chartData.map(function (barChartData) {
      return barChartData.value;
    })) + 1;
  }

  private createSvg(): void {
    this.svg = d3.select("figure#" + this.uniqueId)
      .append("svg")
      .attr("width", this.width + (this.margin * 2))
      .attr("height", this.height + (this.margin * 2))
      .append("g")
      .attr("transform", "translate(" + ((this.width / 2) + this.margin) + "," + ((this.height / 2) + this.margin) + ")");
  }

  private drawPie(data: PieChartData[]): void {
    // Compute the position of each group on the pie:
    const pie = d3.pie<any>().value((data: PieChartData) => data.value);

    const scaleOrdinal: ScaleOrdinal<string, any> = d3.scaleOrdinal(this.colors);

    // Build the pie chart
    this.svg
      .selectAll('pieces')
      .data(pie(data))
      .enter()
      .append('path')
      .attr('d', d3.arc()
        .innerRadius(this.innerRadius)
        .outerRadius(this.radius)
      )
      .attr('fill', (pieChartData: PieChartData, index: string) => {
        return scaleOrdinal(index);
      })
      .attr("stroke", this.strokeColor)
      .style("stroke-width", this.strokeWidth + "px");

    // Add labels
    const labelLocation = d3.arc()
      .innerRadius(this.labelRadius)
      .outerRadius(this.radius);

    this.svg
      .selectAll('pieces')
      .data(pie(data))
      .enter()
      .append('text')
      .text((d: any) => d.data.key)
      .attr("transform", (d: any) => "translate(" + labelLocation.centroid(d) + ")")
      .style("text-anchor", "middle")
      .style("font-size", 15);
  }
}
