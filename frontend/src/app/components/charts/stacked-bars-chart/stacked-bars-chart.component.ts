import {AfterViewInit, Component, Input} from '@angular/core';
import {v4 as uuid} from "uuid";
import * as d3 from "d3";
import {ScaleOrdinal} from "d3-scale";
import {StackedBarsChartData} from "./stacked-bars-chart-data";
import {select} from "d3";

@Component({
  selector: 'app-stacked-bars-chart',
  templateUrl: './stacked-bars-chart.component.html',
  styleUrls: ['./stacked-bars-chart.component.scss']
})
export class StackedBarsChartComponent implements AfterViewInit {

  @Input()
  public title: string = "Bar Chart";
  @Input()
  public chartData: StackedBarsChartData;
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
  @Input()
  public strokeColor: string = "#121926";
  @Input()
  public strokeWidth: number = 2;

  public uniqueId: string = "id" + uuid();

  private svg: any;

  ngAfterViewInit() {
    this.createSvg();
    this.drawBars(this.chartData);
    this.createLegend(this.chartData);
  }

  private getMaxY(data: StackedBarsChartData): number {
    // const sums = data.values.map((_, i) => d3.sum(data.map(({ values }) => values[i])));
    return data.getMax() + 1;
  }

  private createSvg(): void {
    this.svg = d3.select("div#" + this.uniqueId)
      .append("svg")
      .attr("width", this.width + (this.margin * 2))
      .attr("height", this.height + (this.margin * 2))
      .append("g")
      .attr("transform", "translate(" + this.margin + "," + this.margin + ")");
  }

  private createLegend(data: StackedBarsChartData) {
    const legendItems = select("#legend")
      .selectAll("li")
      .data(data.subgroups);

    legendItems
      .enter()
      .append("li")
      .attr("class", "legend-list")
      .style("--gen-color", ( color ) => this.colors[data.subgroups.indexOf(color)])
      .text((label) => label);
  }

  private drawBars(data: StackedBarsChartData): void {
    // Tournament1, Tournament2, Tournament3, Tournament4
    const groups = data.getGroups();
    // Men, Kote, Do
    const subgroups = data.getSubgroups();


    // Create the X-axis band scale
    const x = d3.scaleBand()
      .domain(groups)
      .range([0, this.width])
      .padding(0.2);

    // Draw the X-axis on the DOM
    this.svg.append("g")
      .attr("transform", "translate(0," + this.height + ")")
      .call(d3.axisBottom(x).tickPadding(8).tickSize(5));

    // Create the Y-axis band scale
    const y = d3.scaleLinear()
      .domain([0, data.getMax()])
      .range([this.height, 0]);

    // Draw the Y-axis on the DOM
    this.svg.append("g")
      .call(d3.axisLeft(y));

    const stackedData: any[][][] = data.getStackedData();

    // Create and fill the bars
    const scaleOrdinal: ScaleOrdinal<string, any> = d3.scaleOrdinal(this.colors);
    this.svg.append("g")
      .selectAll("g")
      .data(stackedData)
      .enter().append("g")
      .attr("fill", (data: any, index: string) => {
        return scaleOrdinal(index);
      })
      .selectAll("rect")
      .data(function (data: any) {
        return data;
      })
      .enter().append("rect")
      .attr("x", (d: any, i: any) => {
        return x(groups[i])
      })
      .attr("y", (d: any) => {
        return y(d[1])
      })
      .attr("height", (array1: number[]) => {
        return y(array1[0]) - y(array1[1])
      })
      .attr("width", x.bandwidth())
      .attr("stroke", this.strokeColor)
      .style("stroke-width", this.strokeWidth + "px");
  }

}
