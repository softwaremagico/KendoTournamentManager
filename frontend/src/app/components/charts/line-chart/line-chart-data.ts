export class LineChartData {
  // X Group -> Property -> Value. For example (2010 -> Men -> 5)
  values: Map<Date, Map<string, number>>;
  subgroups: string[];
  subgroupsWithValues: string[];
  stackedData: any[][][];

  /**
   * Create a dataset for the stacked bars chart.
   * @param values Map of groups, with subgroups and values.
   * @param subgroups if not defined, subgroups will be calculated from previous parameter. Use this for forcing the order on the chart.
   */
  constructor(values: Map<Date, Map<string, number>>, subgroups?: string[]) {
    this.values = values;
    this.subgroups = subgroups!;
  }

  /* gets the max grouped data by group */
  getMax(): number {
    let max: number = 0;
    for (const key of this.values.keys()) {
      for (const subgroup of this.values.get(key)!.keys()) {
        if (this.values.get(key)!.get(subgroup)! > max) {
          max = this.values.get(key)!.get(subgroup)!;
        }
      }
    }
    return max;
  }

  getGroups(): number[] {
    return Array.from(this.values.keys()).map(d => d.getFullYear());
  }

  getSubgroups(): string[] {
    if (this.subgroups) {
      return this.subgroups;
    }
    const subgroups: Set<string> = new Set();
    for (const key of this.values.keys()) {
      for (const value of this.values.get(key)!.keys()) {
        subgroups.add(value);
      }
    }
    this.subgroups = Array.from(subgroups);
    return this.subgroups;
  }

  getSubgroupsWithValues(): string[] {
    if (this.subgroupsWithValues) {
      return this.subgroupsWithValues;
    }
    const subgroups: string[] = this.getSubgroups();
    for (let i = subgroups.length - 1; i > 0; i--) {
      let remove = true;
      for (const key of this.values.keys()) {
        if (this.values.get(key)!.get(subgroups[i])) {
          remove = false;
          break;
        }
      }
      if (remove) {
        subgroups.splice(i, 1);
      }
    }
    this.subgroupsWithValues = subgroups;
    return subgroups;
  }

  getStackedData(): any[][][] {
    if (this.stackedData) {
      return this.stackedData;
    }
    const dataMatrix: any[][][] = [];
    const subgroups: string[] = this.getSubgroups();
    for (let i: number = 0; i < subgroups.length; i++) {
      dataMatrix[i] = [];
      let j = 0;
      for (const key of this.values.keys()) {
        dataMatrix[i][j] = [];
        dataMatrix[i][j][0] = key.getFullYear();
        dataMatrix[i][j][1] = this.values.get(key)!.get(subgroups[i])!;
        j++;
      }
    }
    this.stackedData = dataMatrix;
    return this.stackedData;
  }

}
