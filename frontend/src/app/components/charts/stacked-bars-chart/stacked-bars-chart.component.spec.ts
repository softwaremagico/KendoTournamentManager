import { Subject } from 'rxjs';
import { StackedBarsChartComponent } from './stacked-bars-chart.component';
import { StackedBarChartData, StackedBarChartDataElement } from './stacked-bars-chart-data';
import { Colors } from '../colors';
import { DarkModeService } from '../../../services/notifications/dark-mode.service';
import { UserSessionService } from '../../../services/user-session.service';

describe('StackedBarsChartComponent', () => {
  let component: StackedBarsChartComponent;
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

    component = new StackedBarsChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = StackedBarChartData.fromMultipleDataElements([
      new StackedBarChartDataElement([
        ['Won', 3],
        ['Lost', 1]
      ], 'Team A'),
      new StackedBarChartDataElement([
        ['Won', 2],
        ['Lost', 2]
      ], 'Team B')
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

    const nightComponent = new StackedBarsChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.chart.type).toBe('bar');
    expect(component.chartOptions.series.length).toBe(2);
    expect(component.chartOptions.xaxis?.categories).toEqual(component.data.getLabels());
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return plot options using configured bar options and total labels', () => {
    component.horizontal = true;
    component.barThicknessPercentage = 60;
    component.borderRadius = 4;
    component.enableTotals = false;

    const options = (component as any).getPlotOptions();

    expect(options.bar?.distributed).toBeFalse();
    expect(options.bar?.horizontal).toBeTrue();
    expect(options.bar?.barHeight).toBe('60%');
    expect(options.bar?.columnWidth).toBe('60%');
    expect(options.bar?.borderRadius).toBe(4);
    expect(options.bar?.dataLabels?.total?.enabled).toBeFalse();
  });

  it('should assign colors cyclically in setColors', () => {
    component.colors = ['#a', '#b'];
    const data = [
      { name: 'S1', data: [1] },
      { name: 'S2', data: [2] },
      { name: 'S3', data: [3] }
    ] as any;

    const withColors = component.setColors(data);

    expect(withColors[0].color).toBe('#a');
    expect(withColors[1].color).toBe('#b');
    expect(withColors[2].color).toBe('#a');
  });

  it('should call chart.updateSeries and updateOptions on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries', 'updateOptions']);
    component.chart = chartSpy as any;
    component.xAxisOnTop = true;
    component.xAxisTitle = 'Teams';

    const newData = StackedBarChartData.fromDataElements(
      new StackedBarChartDataElement([
        ['Won', 1],
        ['Lost', 0]
      ], 'Team C')
    );

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalled();
    expect(chartSpy.updateOptions).toHaveBeenCalledOnceWith({
      xaxis: {
        categories: newData.getLabels(),
        position: 'top',
        title: {
          text: 'Teams'
        }
      }
    });
  });
});

