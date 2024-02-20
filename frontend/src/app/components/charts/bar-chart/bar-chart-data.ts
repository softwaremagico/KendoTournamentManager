export class BarChartData {
  name: string | undefined;
  elements: BarChartDataElement[];

  public static fromArray(elements: [string, number][]): BarChartData {
    const barChartData: BarChartData = new BarChartData();
    barChartData.elements = [];
    for (const element of elements) {
      barChartData.elements.push(new BarChartDataElement(element[0], element[1]));
    }
    return barChartData;
  }

  public static fromDataElements(elements: BarChartDataElement[]): BarChartData {
    const barChartData: BarChartData = new BarChartData();
    barChartData.elements = elements;
    return barChartData;
  }

  constructor(name?: string) {
    this.name = name;
    this.elements = [];
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

export class BarChartDataElement {
  x: string;
  y: number;

  constructor(x: string, y: number) {
    this.x = x;
    this.y = y;
  }
}

type Data = {
  name: string,
  data: number[]
};
