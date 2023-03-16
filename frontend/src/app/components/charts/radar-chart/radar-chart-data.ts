export class RadarBarChartData {
  name: string[] | undefined;
  elements: RadarBarChartDataElement[];

  public static fromDataElements(elements: RadarBarChartDataElement): RadarBarChartData {
    const barChartData: RadarBarChartData = new RadarBarChartData();
    barChartData.elements = [];
    barChartData.elements[0] = elements;
    return barChartData;
  }

  public static fromMultipleDataElements(elements: RadarBarChartDataElement[]): RadarBarChartData {
    const barChartData: RadarBarChartData = new RadarBarChartData();
    barChartData.elements = elements;
    return barChartData;
  }

  constructor(name?: string[]) {
    this.name = name;
  }

  getLabels(): string[] {
    return this.elements.map(e => e.name);
  }

  getData(): Data[] {
    const data: Map<string, Data> = new Map<string, Data>();
    for (const element of this.elements) {
      for (const point of element.points) {
        if (data.get(point[0]) === undefined) {
          data.set(point[0], new Data());
        }
        data.get(point[0])!.name = point[0];
        data.get(point[0])!.data.push(point[1]);
      }
    }
    return Array.from(data.values());
  }
}

export class RadarBarChartDataElement {
  name: string;
  //X,Y
  points: [string, number][];

  constructor(points: [string, number][], name?: string) {
    this.points = points;
    if (name) {
      this.name = name;
    } else {
      this.name = "";
    }
  }
}

export class Data {
  name: string;
  data: number[] = [];
  color: string; //Color be set on the chart component
}
