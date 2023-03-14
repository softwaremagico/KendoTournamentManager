export class StackedBarsChartData {
  // X Group -> Property -> Value. For example (2010 -> Men -> 5)
  values: Map<string, Map<string, number>>;

  constructor(values: Map<string, Map<string, number>>) {
    this.values = values;
    console.log(values);
  }

  /* gets the max grouped data by group */
  getMax(): number {
    const data: any[][][] = this.getStackedData();
    let max: number = 0;
    for (let i: number = 0; i < data.length; i++) {
      for (let j: number = 0; j < data[i].length; j++) {
        if (data[i][j][1]! > max) {
          max = data[i][j][1];
        }
      }
    }
    return max;
  }

  getGroups(): string[] {
    return Array.from(this.values.keys());
  }

  getSubgroups(): string[] {
    const subgroups: Set<string> = new Set();
    for (const key of this.values.keys()) {
      for (const value of this.values.get(key)!.keys()) {
        subgroups.add(value);
      }
    }
    return Array.from(subgroups);
  }

  getStackedData(): any[][][] {
    const dataMatrix: any[][][] = [];
    const subgroups: string[] = this.getSubgroups();
    for (let i: number = 0; i < subgroups.length; i++) {
      dataMatrix[i] = [];
      let j = 0;
      for (const key of this.values.keys()) {
        dataMatrix[i][j] = [];
        if (i == 0) {
          dataMatrix[i][j].push(0);
        } else {
          dataMatrix[i][j].push(dataMatrix[i - 1][j][1]);
        }
        dataMatrix[i][j].push(dataMatrix[i][j][0] + this.values.get(key)!.get(subgroups[i])!);
        j++;
      }
    }
    return dataMatrix;
  }

}
