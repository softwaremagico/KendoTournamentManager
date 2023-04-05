export class GaugeChartData {
  name: string | undefined;
  elements: RadialChartDataElement[];

  public static fromArray(elements: [string, number][]): GaugeChartData {
    const radialChartData: GaugeChartData = new GaugeChartData();
    radialChartData.elements = [];
    for (const element of elements) {
      radialChartData.elements.push(new RadialChartDataElement(element[0], element[1]));
    }
    return radialChartData;
  }

  public static fromDataElements(elements: RadialChartDataElement[]): GaugeChartData {
    const radialChartData: GaugeChartData = new GaugeChartData();
    radialChartData.elements = elements;
    return radialChartData;
  }

  constructor(name?: string) {
    this.name = name;
    this.elements = [];
  }

  getLabels(): string[] {
    return this.elements.map(e => e.label);
  }

  getValues(): number[] {
    return this.elements.map(e => e.value);
  }

  getData(): Data[] {
    return [{
      name: this.name ? this.name : "",
      data: this.getValues()
    }]
  }

  getTotal(): number {
    return this.elements.reduce((sum, e) => sum + e.value, 0);
  }
}

export class RadialChartDataElement {
  label: string;
  value: number;

  constructor(x: string, y: number) {
    this.label = x;
    this.value = y;
  }
}

type Data = {
  name: string,
  data: number[]
};
