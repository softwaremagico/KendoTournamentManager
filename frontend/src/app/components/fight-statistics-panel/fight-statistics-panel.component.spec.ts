import {BehaviorSubject, of} from 'rxjs';
import {TournamentFightStatistics} from '../../models/tournament-fight-statistics.model';
import {Tournament} from '../../models/tournament';
import {StatisticsService} from '../../services/statistics.service';
import {StatisticsChangedService} from '../../services/notifications/statistics-changed.service';
import {FightStatisticsPanelComponent} from './fight-statistics-panel.component';

describe('FightStatisticsPanelComponent', () => {
  let component: FightStatisticsPanelComponent;
  let statisticsServiceSpy: jasmine.SpyObj<StatisticsService>;
  let statisticsChangedSubject: BehaviorSubject<boolean>;
  let statisticsChangedService: StatisticsChangedService;

  const createTournament = (id: number): Tournament =>
    ({ id, name: 'T' } as unknown as Tournament);

  beforeEach(() => {
    statisticsServiceSpy = jasmine.createSpyObj('StatisticsService', ['getFightStatistics']);
    statisticsChangedSubject = new BehaviorSubject<boolean>(false);
    statisticsChangedService = {
      areStatisticsChanged: statisticsChangedSubject
    } as StatisticsChangedService;

    component = new FightStatisticsPanelComponent(
      statisticsServiceSpy,
      statisticsChangedService
    );
  });

  afterEach(() => {
    component.ngOnDestroy();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load fight statistics on statistics change notification', () => {
    const stats = new TournamentFightStatistics();
    stats.estimatedTime = 3750; // 1h 2m 30s
    component.tournament = createTournament(1);
    component.teams = false;
    statisticsServiceSpy.getFightStatistics.and.returnValue(of(stats));

    component.ngOnInit();
    statisticsChangedSubject.next(true);

    expect(statisticsServiceSpy.getFightStatistics).toHaveBeenCalledWith(1, true, false);
    expect(component.fightStatistics).toBe(stats);
    expect(component.hours).toBe(1);
    expect(component.minutes).toBe(2);
    expect(component.seconds).toBe(30);
  });

  it('should set empty TournamentFightStatistics when service returns null', () => {
    component.tournament = createTournament(2);
    component.teams = true;
    statisticsServiceSpy.getFightStatistics.and.returnValue(of(null as any));

    component.ngOnInit();
    statisticsChangedSubject.next(true);

    expect(component.fightStatistics).toBeTruthy();
  });

  it('should not call statistics service when tournament has no id', () => {
    component.tournament = { name: 'No ID' } as Tournament;

    component.ngOnInit();
    statisticsChangedSubject.next(true);

    expect(statisticsServiceSpy.getFightStatistics).not.toHaveBeenCalled();
  });

  it('should format double digit correctly for numbers < 10', () => {
    expect(component.toDoubleDigit(5)).toBe('05');
  });

  it('should format double digit correctly for numbers >= 10', () => {
    expect(component.toDoubleDigit(15)).toBe('15');
  });

  it('should return 00 for NaN in toDoubleDigit', () => {
    expect(component.toDoubleDigit(NaN)).toBe('00');
  });

  it('should return formatted time string from getTime', () => {
    component.hours = 1;
    component.minutes = 5;
    component.seconds = 9;

    expect(component.getTime()).toBe('01:05:09');
  });

  it('should return 00:00:00 when hours, minutes and seconds are zero', () => {
    component.hours = 0;
    component.minutes = 0;
    component.seconds = 0;

    expect(component.getTime()).toBe('00:00:00');
  });

  it('should stop updating after ngOnDestroy', () => {
    component.tournament = createTournament(3);
    component.teams = false;
    statisticsServiceSpy.getFightStatistics.and.returnValue(of(new TournamentFightStatistics()));
    component.ngOnInit();
    // The BehaviorSubject emits once on subscribe; record that call count.
    const callsAfterInit = statisticsServiceSpy.getFightStatistics.calls.count();

    component.ngOnDestroy();
    statisticsChangedSubject.next(true);

    // No new calls should have occurred after destroy.
    expect(statisticsServiceSpy.getFightStatistics.calls.count()).toBe(callsAfterInit);
  });
});


