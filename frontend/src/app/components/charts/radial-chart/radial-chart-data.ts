import {RadialChartDataBase, RadialChartDataElement} from '../radial-chart-data-base';

export {RadialChartDataElement} from '../radial-chart-data-base';

export class RadialChartData extends RadialChartDataBase {

  public static fromArray(elements: [string, number][]): RadialChartData {
    return RadialChartDataBase.fromArrayElements(new RadialChartData(), elements);
  }

  public static fromDataElements(elements: RadialChartDataElement[]): RadialChartData {
    return RadialChartDataBase.fromElements(new RadialChartData(), elements);
  }
}
