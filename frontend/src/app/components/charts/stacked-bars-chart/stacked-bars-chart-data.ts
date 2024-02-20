export class StackedBarChartData {
  elements: StackedBarChartDataElement[];

  public static fromDataElements(elements: StackedBarChartDataElement): StackedBarChartData {
    const barChartData: StackedBarChartData = new StackedBarChartData();
    barChartData.elements = [];
    barChartData.elements[0] = elements;
    return barChartData;
  }

  public static fromMultipleDataElements(elements: StackedBarChartDataElement[]): StackedBarChartData {
    const barChartData: StackedBarChartData = new StackedBarChartData();
    barChartData.elements = elements;
    return barChartData;
  }

  constructor() {
    this.elements = [];
  }

  getLabels(): string[] {
    return this.elements.map(e => e.name);
  }

  getData(): StackedBarsData[] {
    const data: Map<string, StackedBarsData> = new Map<string, StackedBarsData>();
    for (const element of this.elements) {
      for (const point of element.points) {
        if (data.get(point[0]) === undefined) {
          data.set(point[0], new StackedBarsData());
        }
        data.get(point[0])!.name = point[0];
        data.get(point[0])!.data.push(point[1]);
      }
    }
    return Array.from(data.values());
  }

  convert(element: StackedBarChartDataElement): StackedBarsData {
    const stackedBarsData: StackedBarsData = new StackedBarsData();
    for (const point of element.points) {
      stackedBarsData.name = point[0];
      stackedBarsData.data.push(point[1]);
    }
    return stackedBarsData;
  }
}

export class StackedBarChartDataElement {
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

export class StackedBarsData {
  name: string;
  data: number[] = [];
  color: string; //Color be set on the chart component

  constructor(data?: number[], name?: string) {
    if (data) {
      this.data = data;
    }
    if (name) {
      this.name = name;
    }
  }
}
