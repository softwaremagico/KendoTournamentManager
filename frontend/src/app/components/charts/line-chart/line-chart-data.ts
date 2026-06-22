export class LineChartData {
  name: string[] | undefined;
  elements: LineChartDataElement[];

  public static fromArray(elements: [string, number][]): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = [];
    lineChartData.elements.push(new LineChartDataElement(elements));
    return lineChartData;
  }

  public static fromMultipleArray(elements: [string, number][][]): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = [];
    for (let element of elements) {
      lineChartData.elements.push(new LineChartDataElement(element));
    }
    return lineChartData;
  }

  public static fromDataElements(element: LineChartDataElement): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = [];
    lineChartData.elements[0] = element;
    return lineChartData;
  }

  public static fromMultipleDataElements(elements: LineChartDataElement[]): LineChartData {
    const lineChartData: LineChartData = new LineChartData();
    lineChartData.elements = elements;
    return lineChartData;
  }

  constructor(name?: string[]) {
    this.name = name;
    this.elements = [];
  }

  getLabels(): string[] {
    const labels: Set<string> = new Set();
    for (const element of this.elements) {
      for (const point of element.points) {
        labels.add(point[0]);
      }
    }
    return Array.from(labels);
  }

  getData(): Data[] {
    const data: Data[] = [];
    const labels: string[] = this.getLabels();
    for (const element of this.elements) {
      const points: (number | null)[] = [];
      for (const label of labels) {
        const point: [string, number] | undefined = element.points.find(p => p[0] == label);
        if (point) {
          points.push(point[1]);
        } else {
          points.push(null);
        }

      }
      data.push({name: element.name, data: points});
    }
    return data;
  }
}

export class LineChartDataElement {
  name: string;
  //X,Y
  points: [string, number][];

  constructor(points?: [string, number][], name?: string) {
    if (points) {
      this.points = points;
    } else {
      this.points = [];
    }
    if (name) {
      this.name = name;
    } else {
      this.name = "";
    }
  }
}

type Data = {
  name: string,
  data: (number | null)[]
};
