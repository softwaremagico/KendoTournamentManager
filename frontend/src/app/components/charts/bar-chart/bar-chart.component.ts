import {Component, Input, OnInit} from '@angular/core';
import * as d3 from "d3";
import {Score} from "../../../models/score";
import {ScaleOrdinal} from "d3-scale";
import {BarChartData} from "./bar-chart-data";

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent implements OnInit {

  @Input()
  public title: string = "Bar Chart";
  @Input()
  public barChartData: BarChartData[];
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


  private svg: any;


  ngOnInit() {
    this.createSvg();
    this.drawBars(this.barChartData);
  }

  private getMaxY(): number {
    return Math.max(...this.barChartData.map(function (barChartData) {
      return barChartData.value;
    })) + 1;
  }

  private createSvg(): void {
    this.svg = d3.select("figure#content-chart")
      .append("svg")
      .attr("width", this.width + (this.margin * 2))
      .attr("height", this.height + (this.margin * 2))
      .append("g")
      .attr("transform", "translate(" + this.margin + "," + this.margin + ")");
  }

  private drawBars(data: BarChartData[]): void {
    // Create the X-axis band scale
    const x = d3.scaleBand()
      .range([0, this.width])
      .domain(data.map(barChartData => barChartData.key))
      .padding(0.2);

    // Draw the X-axis on the DOM
    this.svg.append("g")
      .attr("transform", "translate(0," + this.height + ")")
      .call(d3.axisBottom(x))
      .selectAll("text")
      .attr("transform", "translate(-10,0)rotate(-45)")
      .style("text-anchor", "end");

    // Create the Y-axis band scale
    const y = d3.scaleLinear()
      .domain([0, this.getMaxY()])
      .range([this.height, 0]);

    // Draw the Y-axis on the DOM
    this.svg.append("g")
      .call(d3.axisLeft(y));

    // Create and fill the bars

    const scaleOrdinal: ScaleOrdinal<string, any> = d3.scaleOrdinal(this.colors);
    this.svg.selectAll("bars")
      .data(data)
      .enter()
      .append("rect")
      .attr("x", (barChartData: BarChartData) => x(barChartData.key))
      .attr("y", (barChartData: BarChartData) => y(barChartData.value))
      .attr("width", x.bandwidth())
      .attr("height", (barChartData: BarChartData) => this.height - y(barChartData.value))
      .attr("fill", (barChartData: BarChartData, index: string) => {
        return scaleOrdinal(index);
      });
  }
}
