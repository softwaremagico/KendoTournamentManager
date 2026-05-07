import { Subject } from 'rxjs';
import { LineChartComponent } from './line-chart.component';
import { LineChartData } from './line-chart-data';
import { Colors } from '../colors';
import { DarkModeService } from '../../../services/notifications/dark-mode.service';
import { UserSessionService } from '../../../services/user-session.service';

describe('LineChartComponent', () => {
  let component: LineChartComponent;
  let darkModeServiceMock: DarkModeService;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let darkModeSubject: Subject<boolean>;

  beforeEach(() => {
    darkModeSubject = new Subject<boolean>();
    darkModeServiceMock = {
      darkModeSwitched: darkModeSubject
    } as DarkModeService;
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getNightMode']);
    userSessionServiceSpy.getNightMode.and.returnValue(false);

    component = new LineChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = LineChartData.fromMultipleArray([
      [
        ['Jan', 10],
        ['Feb', 12]
      ],
      [
        ['Jan', 9],
        ['Mar', 14]
      ]
    ]);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should keep default palette in constructor when night mode is disabled', () => {
    expect(component.colors).toEqual(Colors.defaultPalette);
  });

  it('should set night palette in constructor when night mode is enabled', () => {
    userSessionServiceSpy.getNightMode.and.returnValue(true);

    const nightComponent = new LineChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.series).toEqual(component.data.getData());
    expect(component.chartOptions.xaxis?.categories).toEqual(component.data.getLabels());
    expect(component.chartOptions.chart.type).toBe('line');
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return plot options using configured orientation and thickness', () => {
    component.horizontal = true;
    component.barThicknessPercentage = 65;

    const options = (component as any).getPlotOptions();

    expect(options.bar?.distributed).toBeTrue();
    expect(options.bar?.horizontal).toBeTrue();
    expect(options.bar?.barHeight).toBe('65%');
    expect(options.bar?.columnWidth).toBe('65%');
  });

  it('should set x axis on top and custom stroke options', () => {
    component.xAxisOnTop = true;
    component.strokeWidth = 3;
    component.curve = 'stepline';

    (component as any).setProperties();

    expect(component.chartOptions.xaxis?.position).toBe('top');
    expect(component.chartOptions.stroke?.width).toBe(3);
    expect(component.chartOptions.stroke?.curve).toBe('stepline');
  });

  it('should call chart.updateSeries and updateOptions on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries', 'updateOptions']);
    component.chart = chartSpy as any;
    component.xAxisOnTop = true;
    component.xAxisTitle = 'Months';

    const newData = LineChartData.fromArray([
      ['Apr', 1],
      ['May', 2]
    ]);

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
    expect(chartSpy.updateOptions).toHaveBeenCalledOnceWith({
      xaxis: {
        categories: newData.getLabels(),
        position: 'top',
        title: {
          text: 'Months'
        }
      }
    });
  });
});

