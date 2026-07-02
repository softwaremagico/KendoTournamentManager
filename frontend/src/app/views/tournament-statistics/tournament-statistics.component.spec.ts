import {TournamentStatisticsComponent} from './tournament-statistics.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {Router} from '@angular/router';
import {StatisticsService} from '../../services/statistics.service';
import {UserSessionService} from '../../services/user-session.service';
import {RankingService} from '../../services/ranking.service';
import {NameUtilsService} from '../../services/name-utils.service';
import {TranslocoService} from '@jsverse/transloco';
import {AchievementsService} from '../../services/achievements.service';
import {TournamentService} from '../../services/tournament.service';
import {EnvironmentService} from '../../environment.service';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {TournamentStatistics} from '../../models/tournament-statistics.model';
import {TournamentFightStatistics} from '../../models/tournament-fight-statistics.model';
import {RoleType} from '../../models/role-type';
import {ScoreOfCompetitor} from '../../models/score-of-competitor';
import {ScoreOfTeam} from '../../models/score-of-team';
import {Team} from '../../models/team';
import {Participant} from '../../models/participant';

describe('TournamentStatisticsComponent', () => {
  let component: TournamentStatisticsComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let statisticsServiceSpy: jasmine.SpyObj<StatisticsService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let nameUtilsServiceSpy: jasmine.SpyObj<NameUtilsService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let achievementsServiceSpy: jasmine.SpyObj<AchievementsService>;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;
  let environmentServiceSpy: jasmine.SpyObj<EnvironmentService>;

  const buildTournamentStatistics = (): TournamentStatistics => {
    const ts = new TournamentStatistics();
    ts.tournamentId = 1;
    ts.tournamentName = 'Liga Interna Summer Championship 2024';
    ts.teamSize = 3;
    ts.numberOfTeams = 4;
    ts.numberOfParticipants = new Map([
      [RoleType.COMPETITOR, 12],
      [RoleType.REFEREE, 3],
      [RoleType.ORGANIZER, 2]
    ]);
    ts.tournamentFightStatistics = new TournamentFightStatistics();
    ts.tournamentFightStatistics.fightsFinished = 8;
    ts.tournamentFightStatistics.fightsNumber = 10;
    return ts;
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'getCurrentNavigation']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isTransactionalBusy: { next: jasmine.createSpy('next') }
    });
    statisticsServiceSpy = jasmine.createSpyObj('StatisticsService', [
      'getTournamentStatistics',
      'getPreviousTournamentStatistics'
    ]);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getLanguage']);
    rankingServiceSpy = jasmine.createSpyObj('RankingService', [
      'getCompetitorsScoreRankingByTournament',
      'getTeamsScoreRankingByTournament'
    ]);
    nameUtilsServiceSpy = jasmine.createSpyObj('NameUtilsService', ['getDisplayName']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    achievementsServiceSpy = jasmine.createSpyObj('AchievementsService', ['getTournamentAchievements']);
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', ['get']);
    environmentServiceSpy = jasmine.createSpyObj('EnvironmentService', ['isAchievementsEnabled']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    userSessionServiceSpy.getLanguage.and.returnValue('en');
    translocoServiceSpy.translate.and.returnValue('translated');
    environmentServiceSpy.isAchievementsEnabled.and.returnValue(false);

    routerSpy.getCurrentNavigation.and.returnValue({
      extras: { state: { tournamentId: 5 } }
    } as any);

    component = new TournamentStatisticsComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      statisticsServiceSpy,
      userSessionServiceSpy,
      rankingServiceSpy,
      nameUtilsServiceSpy,
      translocoServiceSpy,
      achievementsServiceSpy,
      tournamentServiceSpy,
      environmentServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should read tournamentId from router state in constructor', () => {
    expect((component as any).tournamentId).toBe(5);
  });

  it('should navigate back to tournaments when state is null', () => {
    routerSpy.getCurrentNavigation.and.returnValue(null);

    component = new TournamentStatisticsComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      statisticsServiceSpy,
      userSessionServiceSpy,
      rankingServiceSpy,
      nameUtilsServiceSpy,
      translocoServiceSpy,
      achievementsServiceSpy,
      tournamentServiceSpy,
      environmentServiceSpy
    );

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments'], {});
  });

  it('should set locale to Spanish DatePipe when language is es', () => {
    userSessionServiceSpy.getLanguage.and.returnValue('es');

    component = new TournamentStatisticsComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      statisticsServiceSpy,
      userSessionServiceSpy,
      rankingServiceSpy,
      nameUtilsServiceSpy,
      translocoServiceSpy,
      achievementsServiceSpy,
      tournamentServiceSpy,
      environmentServiceSpy
    );

    expect((component.pipe as any).locale).toBe('es');
  });

  it('should set locale to English DatePipe when language is en', () => {
    expect((component.pipe as any).locale).toBe('en-US');
  });

  it('should abbreviate labels longer than 15 chars', () => {
    const label = component.getLabel('Summer Championship 2024');

    expect(label.length).toBeLessThan(20);
    expect(label).toContain('.');
  });

  it('should strip Liga Interna prefix from label', () => {
    const label = component.getLabel('Liga Interna Summer');

    expect(label).not.toContain('Liga Interna');
    expect(label.trim()).toBeTruthy();
  });

  it('should return full label when shorter than 15 chars', () => {
    const label = component.getLabel('Short Label');

    expect(label).toBe('Short Label');
  });

  it('should return empty string for convertSeconds with undefined', () => {
    const result = component.convertSeconds(undefined);

    expect(result).toBe('');
  });

  it('should return empty string for convertDate with undefined', () => {
    const result = component.convertDate(undefined);

    expect(result).toBe('');
  });

  it('should obtain points as array of tuples from tournamentStatistics', () => {
    const ts = buildTournamentStatistics();
    ts.tournamentFightStatistics.menNumber = 5;
    ts.tournamentFightStatistics.koteNumber = 3;

    translocoServiceSpy.translate.and.returnValue('translated');
    const points = (component as any).obtainPoints(ts);

    expect(points.length).toBeGreaterThan(0);
    expect(points[0]).toEqual(jasmine.any(Array));
  });

  it('should obtain participants as array of tuples from tournamentStatistics', () => {
    const ts = buildTournamentStatistics();
    translocoServiceSpy.translate.and.returnValue('translated');

    const participants = (component as any).obtainParticipants(ts);

    expect(participants.length).toBe(5);
    expect(participants[0][1]).toBe(12);
    expect(participants[1][1]).toBe(3);
  });

  it('should obtain team size as tuple', () => {
    const ts = buildTournamentStatistics();

    const result = (component as any).obtainTeamSize(ts);

    expect(result[1]).toBe(3);
  });

  it('should obtain teams count as tuple', () => {
    const ts = buildTournamentStatistics();

    const result = (component as any).obtainTeams(ts);

    expect(result[1]).toBe(4);
  });

  it('should return zero for fights over stats when fights are not set', () => {
    const ts = buildTournamentStatistics();
    ts.tournamentFightStatistics.fightsNumber = 0;
    ts.tournamentFightStatistics.fightsFinished = 0;

    (component as any).initializeFightsOverStatistics(ts);

    const chartData = component.fightsOverData;
    expect(chartData).toBeDefined();
  });

  it('should calculate fights over progress percentage', () => {
    const ts = buildTournamentStatistics();

    (component as any).initializeFightsOverStatistics(ts);

    const chartData = component.fightsOverData;
    expect(chartData.elements[0].value).toBe(80);
  });

  it('should return empty string from getCompetitorRanking when competitor is null', () => {
    const result = component.getCompetitorRanking({} as ScoreOfCompetitor);

    expect(result).toBe('');
  });

  it('should get competitor ranking display name when competitor exists', () => {
    nameUtilsServiceSpy.getDisplayName.and.returnValue('Smith, Alice');
    const competitor = { competitor: { name: 'Alice', lastname: 'Smith' } as Participant } as ScoreOfCompetitor;

    const result = component.getCompetitorRanking(competitor);

    expect(result).toBe('Smith, Alice');
    expect(nameUtilsServiceSpy.getDisplayName).toHaveBeenCalled();
  });

  it('should return empty string from getTeamsRanking when team is null', () => {
    const result = component.getTeamsRanking({} as ScoreOfTeam);

    expect(result).toBe('');
  });

  it('should get team name from getTeamsRanking when team exists', () => {
    const teamScore = { team: { name: 'Team Alpha' } as Team } as ScoreOfTeam;

    const result = component.getTeamsRanking(teamScore);

    expect(result).toBe('Team Alpha');
  });

  it('should navigate back to tournaments when goBackToTournament is called', () => {
    component.goBackToTournament();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments'], {});
  });

  it('should return zero participants by role when tournamentStatistics is undefined', () => {
    component.tournamentStatistics = undefined;

    const count = component.numberOfParticipantsByRole(RoleType.COMPETITOR);

    expect(count).toBe(0);
  });

  it('should return participant count by role when tournamentStatistics is set', () => {
    component.tournamentStatistics = buildTournamentStatistics();

    const count = component.numberOfParticipantsByRole(RoleType.COMPETITOR);

    expect(count).toBe(12);
  });
});






