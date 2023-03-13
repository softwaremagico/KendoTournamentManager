export class StackedBarsChartData {
  key: string;
  groups: string[];
  values: number[];

  constructor(key: string, groups: string[], values: number[]) {
    this.key = key;
    this.groups = groups;
    this.values = values;
  }
}
