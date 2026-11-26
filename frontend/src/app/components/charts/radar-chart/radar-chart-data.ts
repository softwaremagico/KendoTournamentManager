export class RadarChartData {
  name: string[] | undefined;
  elements: RadarChartDataElement[];

  public static fromDataElements(elements: RadarChartDataElement): RadarChartData {
    const barChartData: RadarChartData = new RadarChartData();
    barChartData.elements = [];
    barChartData.elements[0] = elements;
    return barChartData;
  }

  public static fromMultipleDataElements(elements: RadarChartDataElement[]): RadarChartData {
    const barChartData: RadarChartData = new RadarChartData();
    barChartData.elements = elements;
    return barChartData;
  }

  constructor(name?: string[]) {
    this.name = name;
    this.elements = [];
  }

  getLabels(): string[] {
    return this.elements.map(e => e.name);
  }

  getData(): RadarData[] {
    const data: Map<string, RadarData> = new Map<string, RadarData>();
    for (const element of this.elements) {
      for (const point of element.points) {
        if (data.get(point[0]) === undefined) {
          data.set(point[0], new RadarData());
        }
        data.get(point[0])!.name = point[0];
        data.get(point[0])!.data.push(point[1]);
      }
    }
    return Array.from(data.values());
  }
}

export class RadarChartDataElement {
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

export class RadarData {
  name: string;
  data: number[] = [];
  color: string; //Color be set on the chart component
}
