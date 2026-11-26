import {Subject} from 'rxjs';
import {GaugeChartComponent} from './gauge-chart.component';
import {GaugeChartData} from './gauge-chart-data';
import {Colors} from '../colors';
import {DarkModeService} from '../../../services/notifications/dark-mode.service';
import {UserSessionService} from '../../../services/user-session.service';

describe('GaugeChartComponent', () => {
  let component: GaugeChartComponent;
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

    component = new GaugeChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = GaugeChartData.fromArray([
      ['Win rate', 62.5]
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

    const nightComponent = new GaugeChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.series).toEqual(component.data.getValues());
    expect(component.chartOptions.labels).toEqual(component.data.getLabels());
    expect(component.chartOptions.chart.type).toBe('radialBar');
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should configure plot options with inner circle and track settings', () => {
    component.innerCirclePercentage = 40;
    component.trackBackgroundColor = '#dddddd';
    component.trackBackgroundThicknessPercentage = 90;

    const options = (component as any).getPlotOptions();

    expect(options.radialBar?.track?.background).toBe('#dddddd');
    expect(options.radialBar?.track?.strokeWidth).toBe('90%');
    expect(options.radialBar?.hollow?.size).toBe('40%');
  });

  it('should format radial bar value with two decimals and percent symbol', () => {
    const options = (component as any).getPlotOptions();
    const formatter = options.radialBar?.dataLabels?.value?.formatter as (value: number) => string;

    expect(formatter(12)).toBe('12.00%');
    expect(formatter(12.3456)).toBe('12.35%');
  });

  it('should call chart.updateSeries on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries']);
    component.chart = chartSpy as any;
    const newData = GaugeChartData.fromArray([
      ['Win rate', 77.77]
    ]);

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
  });
});

