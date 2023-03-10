import {Component, Input, OnInit} from '@angular/core';
import * as d3 from "d3";
import {Score} from "../../../models/score";

@Component({
  selector: 'app-score-chart',
  templateUrl: './score-chart.component.html',
  styleUrls: ['./score-chart.component.scss']
})
export class ScoreChartComponent implements OnInit {

  @Input()
  private scores: { "score": Score; "value": number }[] =
    [
      {"score": Score.MEN, "value": 5},
      {"score": Score.KOTE, "value": 5},
      {"score": Score.DO, "value": 3},
    ];

  @Input()
  public width = 700;
  @Input()
  public height = 700;

  private margin = 50;

  public chartId;

  private svg: any;

  private colors: any;

  constructor() {
    this.chartId = "Cosa"
  }

  ngOnInit() {
    this.createSvg();
    this.drawBars(this.scores);
  }

  private getMaxY(): number {
    return Math.max(...this.scores.map(function (s) {
      return s.value;
    })) + 1;
  }

  private createSvg(): void {
    this.svg = d3.select("figure#bar-chart")
      .append("svg")
      .attr("width", this.width)
      .attr("height", this.height)
      .append("g")
      .attr("transform", "translate(" + this.margin + "," + this.margin + ")");
  }

  private drawBars(data: any[]): void {
    // Create the X-axis band scale
    const x = d3.scaleBand()
      .range([0, this.width])
      .domain(data.map(d => d.score))
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
    this.svg.selectAll("bars")
      .data(data)
      .enter()
      .append("rect")
      .attr("x", (d: any) => x(d.score))
      .attr("y", (d: any) => y(d.value))
      .attr("width", x.bandwidth())
      .attr("height", (d: any) => this.height - y(d.value))
      .attr("fill", "#d04a35");
  }
}
