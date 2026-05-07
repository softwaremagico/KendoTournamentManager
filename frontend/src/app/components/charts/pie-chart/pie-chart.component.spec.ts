import { Subject } from 'rxjs';
import { PieChartComponent } from './pie-chart.component';
import { PieChartData } from './pie-chart-data';
import { Colors } from '../colors';
import { DarkModeService } from '../../../services/notifications/dark-mode.service';
import { UserSessionService } from '../../../services/user-session.service';

describe('PieChartComponent', () => {
  let component: PieChartComponent;
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

    component = new PieChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = PieChartData.fromArray([
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

    const nightComponent = new PieChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build pie chart options on ngOnInit by default', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.chart.type).toBe('pie');
    expect(component.chartOptions.series).toEqual(component.data.getValues());
    expect(component.chartOptions.labels).toEqual(component.data.getLabels());
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should build donut chart options when isDonut is true', () => {
    component.isDonut = true;

    (component as any).setProperties();

    expect(component.chartOptions.chart.type).toBe('donut');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return undefined from getPlotOptions', () => {
    expect((component as any).getPlotOptions()).toBeUndefined();
  });

  it('should use dark tooltip theme', () => {
    const tooltip = (component as any).getTooltip();

    expect(tooltip.theme).toBe('dark');
    expect(tooltip.style?.fontFamily).toBe('Roboto');
  });

  it('should call chart.updateSeries on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries']);
    (component as any).chart = chartSpy;
    const newData = PieChartData.fromArray([
      ['X', 1],
      ['Y', 2]
    ]);

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
  });
});

