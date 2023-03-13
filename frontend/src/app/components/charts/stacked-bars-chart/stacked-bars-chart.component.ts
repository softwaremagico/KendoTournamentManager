import {AfterViewInit, Component, Input} from '@angular/core';
import {v4 as uuid} from "uuid";
import * as d3 from "d3";
import {transpose} from "d3";
import {ScaleOrdinal} from "d3-scale";
import {StackedBarsChartData} from "./stacked-bars-chart-data";

@Component({
  selector: 'app-stacked-bars-chart',
  templateUrl: './stacked-bars-chart.component.html',
  styleUrls: ['./stacked-bars-chart.component.scss']
})
export class StackedBarsChartComponent implements AfterViewInit {

  @Input()
  public title: string = "Bar Chart";
  @Input()
  public chartData: StackedBarsChartData[];
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
  }

  private getMaxY(data: StackedBarsChartData[]): number {
    const sums = data.map((_, i) => d3.sum(data.map(({ values }) => values[i])));
    return Math.max(...sums) + 1;
  }

  private createSvg(): void {
    this.svg = d3.select("figure#" + this.uniqueId)
      .append("svg")
      .attr("width", this.width + (this.margin * 2))
      .attr("height", this.height + (this.margin * 2))
      .append("g")
      .attr("transform", "translate(" + this.margin + "," + this.margin + ")");
  }

  private drawBars(data: StackedBarsChartData[]): void {
    // List of keys -> I show them on the X axis
    const keys = data.map(d => (d.key));
    // A, B, C, D
    const subgroups = data[0].groups;
    // Men, Kote, Do
    const groups = data.map(d => (d.key));


    // Create the X-axis band scale
    const x = d3.scaleBand()
      .domain(subgroups)
      .range([0, this.width])
      .padding(0.2);

    // Draw the X-axis on the DOM
    this.svg.append("g")
      .attr("transform", "translate(0," + this.height + ")")
      .call(d3.axisBottom(x).tickPadding(8).tickSize(5))
      .selectAll("text")
      .attr("transform", "translate(-10,0)rotate(-45)")
      .style("text-anchor", "end");

    // Create the Y-axis band scale
    const y = d3.scaleLinear()
      .domain([0, this.getMaxY(data)])
      .range([this.height, 0]);

    // Draw the Y-axis on the DOM
    this.svg.append("g")
      .call(d3.axisLeft(y));

    //stack the data? --> stack per subgroup
    const dataMatrix: any[][] = transpose(data.map(element => element.values));
    console.log("--->",dataMatrix, subgroups);
    const stackGen: Function = d3.stack().keys(subgroups);
    const stackedData = stackGen(dataMatrix);
    console.log("%% ",stackedData)

    // Create and fill the bars
    const scaleOrdinal: ScaleOrdinal<string, any> = d3.scaleOrdinal(this.colors);
    this.svg.append("g")
      .selectAll("g")
      .data(stackedData)
      .enter().append("g")
      .attr("fill", (StackedBarsChartData: StackedBarsChartData, index: string) => {
        return scaleOrdinal(index);
      })
      .selectAll("rect")
      // enter a second time = loop subgroup per subgroup to add all rectangles
      .data(function (d: any) {
        return d;
      })
      .enter().append("rect")
      .attr("x", (element: StackedBarsChartData, index: number) => x(data.map(element => element.key)[index]))
      .attr("y", (array1: number[]) => y(array1[1]))
      .attr("height", (array1: number[]) => {console.log(array1); return y(array1[0]) - y(array1[1])})
      .attr("width", x.bandwidth())
      .attr("stroke", this.strokeColor)
      .style("stroke-width", this.strokeWidth + "px");
  }

}
