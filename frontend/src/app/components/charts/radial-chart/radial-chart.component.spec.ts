import { Subject } from 'rxjs';
import { RadialChartComponent } from './radial-chart.component';
import { RadialChartData } from './radial-chart-data';
import { Colors } from '../colors';
import { DarkModeService } from '../../../services/notifications/dark-mode.service';
import { UserSessionService } from '../../../services/user-session.service';

describe('RadialChartComponent', () => {
  let component: RadialChartComponent;
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

    component = new RadialChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = RadialChartData.fromArray([
      ['A', 50],
      ['B', 75]
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

    const nightComponent = new RadialChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.chart.type).toBe('radialBar');
    expect(component.chartOptions.series).toEqual(component.data.getValues());
    expect(component.chartOptions.labels).toEqual(component.data.getLabels());
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return radial plot options with configured angles and inner circle', () => {
    component.startAngle = -90;
    component.endAngle = 90;
    component.innerCirclePercentage = 35;

    const options = (component as any).getPlotOptions();

    expect(options.radialBar?.startAngle).toBe(-90);
    expect(options.radialBar?.endAngle).toBe(90);
    expect(options.radialBar?.hollow?.size).toBe('35%');
  });

  it('should format total as average with two decimals and percent symbol', () => {
    const options = (component as any).getPlotOptions();
    const formatter = options.radialBar?.dataLabels?.total?.formatter as (w: any) => string;

    expect(formatter({})).toBe('62.50%');
  });

  it('should call chart.updateSeries on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries']);
    component.chart = chartSpy as any;
    const newData = RadialChartData.fromArray([
      ['X', 10],
      ['Y', 20]
    ]);

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
  });
});

