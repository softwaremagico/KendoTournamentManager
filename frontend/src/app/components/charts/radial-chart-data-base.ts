/** Elemento compartido por los datos de gráficas radiales y de gauge. */
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

/** Comportamiento común de `GaugeChartData` y `RadialChartData`. */
export class RadialChartDataBase {
  name: string | undefined;
  elements: RadialChartDataElement[];

  constructor(name?: string) {
    this.name = name;
    this.elements = [];
  }

  static fromArrayElements<T extends RadialChartDataBase>(data: T, elements: [string, number][]): T {
    for (const element of elements) {
      data.elements.push(new RadialChartDataElement(element[0], element[1]));
    }
    return data;
  }

  static fromElements<T extends RadialChartDataBase>(data: T, elements: RadialChartDataElement[]): T {
    data.elements = elements;
    return data;
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
