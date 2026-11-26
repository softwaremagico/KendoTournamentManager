import {Subject} from 'rxjs';
import {BarChartComponent} from './bar-chart.component';
import {BarChartData} from './bar-chart-data';
import {Colors} from '../colors';
import {DarkModeService} from '../../../services/notifications/dark-mode.service';
import {UserSessionService} from '../../../services/user-session.service';

describe('BarChartComponent', () => {
  let component: BarChartComponent;
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

    component = new BarChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = BarChartData.fromArray([
      ['A', 10],
      ['B', 20]
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

    const nightComponent = new BarChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.series).toEqual(component.data.getData());
    expect(component.chartOptions.xaxis?.categories).toEqual(component.data.getLabels());
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return plot options using configured orientation and thickness', () => {
    component.horizontal = true;
    component.barThicknessPercentage = 60;
    component.borderRadius = 5;

    const options = (component as any).getPlotOptions();

    expect(options.bar?.distributed).toBeTrue();
    expect(options.bar?.horizontal).toBeTrue();
    expect(options.bar?.barHeight).toBe('60%');
    expect(options.bar?.columnWidth).toBe('60%');
    expect(options.bar?.borderRadius).toBe(5);
  });

  it('should set x axis on top when xAxisOnTop is true', () => {
    component.xAxisOnTop = true;

    (component as any).setProperties();

    expect(component.chartOptions.xaxis?.position).toBe('top');
  });

  it('should call chart.updateSeries on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries']);
    component.chart = chartSpy as any;
    const newData = BarChartData.fromArray([
      ['X', 1],
      ['Y', 2]
    ]);

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
  });
});

