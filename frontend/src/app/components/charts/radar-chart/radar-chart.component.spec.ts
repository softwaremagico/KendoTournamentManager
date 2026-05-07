import { Subject } from 'rxjs';
import { RadarChartComponent } from './radar-chart.component';
import { RadarChartData, RadarChartDataElement } from './radar-chart-data';
import { Colors } from '../colors';
import { DarkModeService } from '../../../services/notifications/dark-mode.service';
import { UserSessionService } from '../../../services/user-session.service';

describe('RadarChartComponent', () => {
  let component: RadarChartComponent;
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

    component = new RadarChartComponent(darkModeServiceMock, userSessionServiceSpy);
    component.data = RadarChartData.fromMultipleDataElements([
      new RadarChartDataElement([
        ['Speed', 8],
        ['Power', 6]
      ], 'First'),
      new RadarChartDataElement([
        ['Speed', 5],
        ['Power', 9]
      ], 'Second')
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

    const nightComponent = new RadarChartComponent(darkModeServiceMock, userSessionServiceSpy);

    expect(nightComponent.colors).toEqual(Colors.defaultPaletteNightMode);
  });

  it('should build chart options on ngOnInit', () => {
    component.ngOnInit();

    expect(component.chartOptions).toBeTruthy();
    expect(component.chartOptions.chart.type).toBe('radar');
    expect(component.chartOptions.series.length).toBe(2);
    expect(component.chartOptions.xaxis?.categories).toEqual(component.data.getLabels());
    expect(component.chartOptions.theme?.mode).toBe('light');
  });

  it('should update theme mode when dark mode changes', () => {
    component.ngOnInit();

    darkModeSubject.next(true);

    expect(component.chartOptions.theme?.mode).toBe('dark');
  });

  it('should return radar plot options with configured size and inner colors', () => {
    component.radarSize = 170;
    component.innerColors = ['#111111', '#222222'];

    const options = (component as any).getPlotOptions();

    expect(options.radar?.size).toBe(170);
    expect(options.radar?.polygons?.fill?.colors).toEqual(['#111111', '#222222']);
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

  it('should call chart.updateSeries on update', () => {
    const chartSpy = jasmine.createSpyObj('ChartComponent', ['updateSeries']);
    component.chart = chartSpy as any;
    const newData = RadarChartData.fromDataElements(
      new RadarChartDataElement([
        ['Speed', 10],
        ['Power', 10]
      ], 'Only')
    );

    component.update(newData);

    expect(chartSpy.updateSeries).toHaveBeenCalledOnceWith(newData.getData());
  });
});

