import {ParticipantStatisticsComponent} from './participant-statistics.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {ActivatedRoute, Router} from '@angular/router';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {StatisticsService} from '../../services/statistics.service';
import {UserSessionService} from '../../services/user-session.service';
import {TranslocoService} from '@ngneat/transloco';
import {RankingService} from '../../services/ranking.service';
import {AchievementsService} from '../../services/achievements.service';
import {ParticipantService} from '../../services/participant.service';
import {LoginService} from '../../services/login.service';
import {EnvironmentService} from '../../environment.service';
import {ParticipantStatistics} from '../../models/participant-statistics.model';
import {ParticipantFightStatistics} from '../../models/participant-fight-statistics.model';
import {RoleType} from '../../models/role-type';

describe('ParticipantStatisticsComponent', () => {
  let component: ParticipantStatisticsComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let activatedRouteSpy: jasmine.SpyObj<ActivatedRoute>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let statisticsServiceSpy: jasmine.SpyObj<StatisticsService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let achievementsServiceSpy: jasmine.SpyObj<AchievementsService>;
  let participantServiceSpy: jasmine.SpyObj<ParticipantService>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let environmentServiceSpy: jasmine.SpyObj<EnvironmentService>;

  const buildParticipantStatistics = (): ParticipantStatistics => {
    const ps = new ParticipantStatistics();
    ps.tournaments = 3;
    ps.totalTournaments = 5;
    ps.participantFightStatistics = new ParticipantFightStatistics();
    ps.participantFightStatistics.duelsNumber = 10;
    ps.participantFightStatistics.menNumber = 4;
    ps.participantFightStatistics.koteNumber = 2;
    ps.participantFightStatistics.doNumber = 1;
    ps.participantFightStatistics.tsukiNumber = 0;
    ps.participantFightStatistics.ipponNumber = 0;
    ps.participantFightStatistics.fusenGachiNumber = 0;
    ps.participantFightStatistics.hansokuNumber = 1;
    ps.participantFightStatistics.averageWinTime = 45;
    ps.participantFightStatistics.averageLostTime = 60;
    return ps;
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'getCurrentNavigation']);
    activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: {
        queryParamMap: {
          get: jasmine.createSpy('get').and.returnValue(null)
        }
      }
    });
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isTransactionalBusy: { next: jasmine.createSpy('next') }
    });
    statisticsServiceSpy = jasmine.createSpyObj('StatisticsService', [
      'getParticipantStatistics', 'getYourWorstNightmare', 'getWorstNightmareOf'
    ]);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getLanguage']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    rankingServiceSpy = jasmine.createSpyObj('RankingService', ['getCompetitorsRanking']);
    achievementsServiceSpy = jasmine.createSpyObj('AchievementsService', ['getParticipantAchievements']);
    participantServiceSpy = jasmine.createSpyObj('ParticipantService', ['get', 'getByUsername']);
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['getJwtValue', 'logout', 'setParticipantUserSession']);
    environmentServiceSpy = jasmine.createSpyObj('EnvironmentService', ['isAchievementsEnabled']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    userSessionServiceSpy.getLanguage.and.returnValue('en');
    translocoServiceSpy.translate.and.returnValue('translated');
    environmentServiceSpy.isAchievementsEnabled.and.returnValue(false);
    loginServiceSpy.getJwtValue.and.returnValue('jwt-token');

    routerSpy.getCurrentNavigation.and.returnValue({
      extras: { state: { participantId: 42 } }
    } as any);

    component = new ParticipantStatisticsComponent(
      routerSpy,
      activatedRouteSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      userSessionServiceSpy,
      statisticsServiceSpy,
      translocoServiceSpy,
      rankingServiceSpy,
      achievementsServiceSpy,
      participantServiceSpy,
      loginServiceSpy,
      environmentServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should read participantId from router state in constructor', () => {
    expect((component as any).participantId).toBe(42);
  });

  it('should navigate back to participants when state has invalid participantId', () => {
    routerSpy.getCurrentNavigation.and.returnValue({
      extras: { state: { participantId: 'invalid_id' } }
    } as any);

    component = new ParticipantStatisticsComponent(
      routerSpy,
      activatedRouteSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      userSessionServiceSpy,
      statisticsServiceSpy,
      translocoServiceSpy,
      rankingServiceSpy,
      achievementsServiceSpy,
      participantServiceSpy,
      loginServiceSpy,
      environmentServiceSpy
    );

    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/registry/participants'],
      {}
    );
  });

  it('should initialize data on ngOnInit when JWT is valid', () => {
    loginServiceSpy.getJwtValue.and.returnValue('token');
    spyOn(component, 'initializeData');

    component.ngOnInit();

    expect(component.initializeData).toHaveBeenCalled();
  });

  it('should set locale to German DatePipe when language is de', () => {
    userSessionServiceSpy.getLanguage.and.returnValue('de');

    component = new ParticipantStatisticsComponent(
      routerSpy,
      activatedRouteSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      userSessionServiceSpy,
      statisticsServiceSpy,
      translocoServiceSpy,
      rankingServiceSpy,
      achievementsServiceSpy,
      participantServiceSpy,
      loginServiceSpy,
      environmentServiceSpy
    );

    expect((component.pipe as any).locale).toBe('de');
  });

  it('should initialize score statistics with participant stats', () => {
    const ps = buildParticipantStatistics();

    (component as any).initializeScoreStatistics(ps);

    expect(component.hitsTypeChartData).toBeTruthy();
    expect(component.receivedHitsTypeChartData).toBeTruthy();
    expect(component.performance).toBeTruthy();
    expect(component.performanceRadialData).toBeTruthy();
  });

  it('should calculate performance statistics from participant stats', () => {
    const ps = buildParticipantStatistics();
    ps.participantFightStatistics.duelsNumber = 10;
    ps.participantFightStatistics.menNumber = 5;
    ps.participantFightStatistics.receivedMenNumber = 3;

    const performance = (component as any).generatePerformanceStatistics(ps);

    expect(performance.length).toBe(5);
    expect(performance[0][0]).toBe('attack');
    expect(performance[1][0]).toBe('defense');
    expect(performance[2][0]).toBe('willpower');
    expect(performance[3][0]).toBe('aggressiveness');
    expect(performance[4][0]).toBe('affection');
  });

  it('should obtain points array from participant fight statistics', () => {
    const ps = buildParticipantStatistics();
    ps.participantFightStatistics.menNumber = 5;

    const points = (component as any).obtainPoints(ps);

    expect(points.length).toBe(7);
    expect(points[0][1]).toBe(5);
  });

  it('should obtain received points array from participant fight statistics', () => {
    const ps = buildParticipantStatistics();
    ps.participantFightStatistics.receivedMenNumber = 3;
    ps.participantFightStatistics.receivedKoteNumber = 1;

    const received = (component as any).obtainReceivedPoints(ps);

    expect(received.length).toBe(7);
    expect(received[0][1]).toBe(3);
  });

  it('should return zero performance willpower when no tournaments', () => {
    const ps = buildParticipantStatistics();
    ps.totalTournaments = 0;

    const performance = (component as any).generatePerformanceStatistics(ps);

    expect(performance[2][1]).toBe(0);
  });

  it('should return zero aggressiveness when averageWinTime is zero', () => {
    const ps = buildParticipantStatistics();
    ps.participantFightStatistics.averageWinTime = 0;

    const performance = (component as any).generatePerformanceStatistics(ps);

    expect(performance[3][1]).toBe(0);
  });

  it('should navigate to registry/participants on goBack without backUrl', () => {
    component.goBack();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/registry/participants'], {});
  });

  it('should open participant fights and navigate to fights view', () => {
    component.openParticipantFights();

    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/participants/fights'],
      { state: { participantId: 42 } }
    );
  });

  it('should not navigate when participantId is undefined in openParticipantFights', () => {
    (component as any).participantId = undefined;

    component.openParticipantFights();

    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should return zero numberOfPerformedRoles when participantStatistics is undefined', () => {
    component.participantStatistics = undefined;

    const count = component.numberOfPerformedRoles(RoleType.COMPETITOR);

    expect(count).toBe(0);
  });
});


