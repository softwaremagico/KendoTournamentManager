import {RadialChartDataBase, RadialChartDataElement} from '../radial-chart-data-base';

export {RadialChartDataElement} from '../radial-chart-data-base';

export class GaugeChartData extends RadialChartDataBase {

  public static fromArray(elements: [string, number][]): GaugeChartData {
    return RadialChartDataBase.fromArrayElements(new GaugeChartData(), elements);
  }

  public static fromDataElements(elements: RadialChartDataElement[]): GaugeChartData {
    return RadialChartDataBase.fromElements(new GaugeChartData(), elements);
  }
}
