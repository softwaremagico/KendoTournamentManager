export class PieChartData {
  elements: PieChartDataElement[];

  public static fromArray(elements: [string, number][]): PieChartData {
    const pieChartData: PieChartData = new PieChartData();
    pieChartData.elements = [];
    for (const element of elements) {
      pieChartData.elements.push(new PieChartDataElement(element[0], element[1]));
    }
    return pieChartData;
  }

  public static fromDataElements(elements: PieChartDataElement[]): PieChartData {
    const pieChartData: PieChartData = new PieChartData();
    pieChartData.elements = elements;
    return pieChartData;
  }

  constructor() {
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
      labels: this.getLabels(),
      data: this.getValues()
    }]
  }
}

export class PieChartDataElement {
  label: string;
  value: number;

  constructor(label: string, value: number) {
    this.label = label;
    this.value = value;
  }
}

type Data = {
  labels: string[],
  data: number[]
};
