export class LineChartData {
  name: string | undefined;
  elements: LineChartDataElement[];

  public static fromArray(elements: [string, number][]): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = [];
    for (const element of elements) {
      lineChartData.elements.push(new LineChartDataElement(element[0], element[1]));
    }
    return lineChartData;
  }

  public static fromDataElements(elements: LineChartDataElement[]): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = elements;
    return lineChartData;
  }

  constructor(name?: string) {
    this.name = name;
  }

  getLabels(): string[] {
    return this.elements.map(e => e.x);
  }

  getValues(): number[] {
    return this.elements.map(e => e.y);
  }

  getData(): Data[] {
    return [{
      name: this.name ? this.name : "",
      data: this.getValues()
    }]
  }
}

export class LineChartDataElement {
  x: string;
  y: number;

  constructor(x: string, y: number) {
    this.x = x;
    this.y = y;
  }
}

export type Data = {
  name: string,
  data: number[]
};
