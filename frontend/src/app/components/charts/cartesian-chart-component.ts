import {Directive, Input} from '@angular/core';
import {ApexAxisChartSeries, ApexPlotOptions, ApexXAxis, ApexYAxis} from 'ng-apexcharts';
import {CustomChartComponent} from './custom-chart-component';

/**
 * Entradas y helpers compartidos por las gráficas de barras, barras apiladas y
 * líneas. Mantiene las mismas opciones que antes construía cada componente.
 */
@Directive()
export abstract class CartesianChartComponent extends CustomChartComponent {

  @Input()
  public horizontal: boolean = false;
  @Input()
  public barThicknessPercentage: number = 75;
  @Input()
  public showValuesLabels: boolean = true;
  @Input()
  public xAxisOnTop: boolean = false;
  @Input()
  public xAxisTitle: string | undefined = undefined;
  @Input()
  public yAxisTitle: string | undefined = undefined;
  @Input()
  public showYAxis: boolean = true;
  @Input()
  public borderRadius: number = 0;

  protected getCartesianChartOptions(type: 'bar' | 'line', series: ApexAxisChartSeries,
                                     plotOptions: ApexPlotOptions, xaxis: ApexXAxis,
                                     fillOpacity?: number, yaxis?: ApexYAxis) {
    return {
      ...this.getCommonChartOptions(type, fillOpacity),
      series: series,
      labels: this.getLabels(this.showValuesLabels),
      plotOptions: plotOptions,
      xaxis: xaxis,
      yaxis: yaxis ?? this.getYAxis(this.showYAxis, this.yAxisTitle)
    };
  }

  protected getBarPlotOptions(distributed: boolean, totalsEnabled?: boolean): ApexPlotOptions {
    return {
      bar: {
        distributed: distributed,
        horizontal: this.horizontal,
        barHeight: this.barThicknessPercentage + '%',
        columnWidth: this.barThicknessPercentage + '%',
        borderRadius: this.borderRadius,
        ...(totalsEnabled === undefined ? {} : {
          dataLabels: {
            total: {
              enabled: totalsEnabled,
              style: {
                fontWeight: 900
              }
            }
          }
        })
      }
    };
  }

  protected getXAxisUpdateOptions(labels: string[]): { xaxis: ApexXAxis } {
    return {
      xaxis: {
        categories: labels,
        position: this.xAxisOnTop ? 'top' : 'bottom',
        title: {
          text: this.xAxisTitle
        }
      }
    };
  }
}
