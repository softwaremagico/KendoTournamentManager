import {of} from 'rxjs';
import {Duel} from '../../models/duel';
import {Group} from '../../models/group';
import {ScoreOfTeam} from '../../models/score-of-team';
import {Team} from '../../models/team';
import {Tournament} from '../../models/tournament';
import {TournamentType} from '../../models/tournament-type';
import {RankingService} from '../../services/ranking.service';
import {MessageService} from '../../services/message.service';
import {NameUtilsService} from '../../services/name-utils.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {TournamentExtendedPropertiesService} from '../../services/tournament-extended-properties.service';
import {TeamRankingComponent} from './team-ranking.component';
import {SwissTieBreakRule} from '../../models/swiss-tie-break-rule';

describe('TeamRankingComponent', () => {
  let component: TeamRankingComponent;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let routerSpy: jasmine.SpyObj<any>;
  let nameUtilsSpy: jasmine.SpyObj<NameUtilsService>;

  const createTeam = (name: string): Team => ({
    name,
    members: []
  } as unknown as Team);

  beforeEach(() => {
    rankingServiceSpy = jasmine.createSpyObj('RankingService', [
      'getTeamsScoreRankingByGroup',
      'getTeamsScoreRankingByTournament',
      'getTeamsScoreRankingByGroupAsPdf',
      'getTeamsScoreRankingByTournamentAsPdf'
    ]);
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj('TournamentExtendedPropertiesService', ['getByTournamentAndKey']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['warningMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    nameUtilsSpy = jasmine.createSpyObj('NameUtilsService', ['getNameLastname']);

    component = new TeamRankingComponent(
      rankingServiceSpy,
      {} as any,
      tournamentExtendedPropertiesServiceSpy,
      messageServiceSpy,
      rbacServiceSpy,
      routerSpy,
      nameUtilsSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load championship ranking by group and winners on init', () => {
    const tournament = { id: 1, type: TournamentType.CHAMPIONSHIP, name: 'T1' } as Tournament;
    const group = { id: 2, index: 0 } as Group;
    const teamA = createTeam('A');
    const teamB = createTeam('B');
    const scores = [
      { sortingIndex: 0, team: teamA },
      { sortingIndex: 0, team: teamB }
    ] as unknown as ScoreOfTeam[];

    component.tournament = tournament;
    component.group = group;
    component.fightsFinished = true;
    rankingServiceSpy.getTeamsScoreRankingByGroup.and.returnValue(of(scores));
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(of({ propertyValue: '1' } as any));

    component.ngOnInit();

    expect(rankingServiceSpy.getTeamsScoreRankingByGroup).toHaveBeenCalledOnceWith(2);
    expect(component.teamScores).toBe(scores);
    expect(component.numberOfWinners).toBe(1);
    expect(component.existsDraws).toBeTrue();
    expect(messageServiceSpy.warningMessage).toHaveBeenCalledOnceWith('drawScore');
  });

  it('should load non championship ranking by tournament on init', () => {
    const tournament = { id: 5, type: TournamentType.LEAGUE, name: 'L1' } as Tournament;
    const scores = [{ sortingIndex: 0, team: createTeam('A') }] as unknown as ScoreOfTeam[];

    component.tournament = tournament;
    rankingServiceSpy.getTeamsScoreRankingByTournament.and.returnValue(of(scores));

    component.ngOnInit();

    expect(rankingServiceSpy.getTeamsScoreRankingByTournament).toHaveBeenCalledOnceWith(5);
    expect(component.teamScores).toBe(scores);
    expect(component.numberOfWinners).toBe(1);
  });

  it('should load swiss group ranking and selected tie-break rule on init', () => {
    const tournament = { id: 9, type: TournamentType.SWISS, name: 'S1' } as Tournament;
    const group = { id: 11, index: 0 } as Group;
    const scores = [{ sortingIndex: 0, team: createTeam('A') }] as unknown as ScoreOfTeam[];

    component.tournament = tournament;
    component.group = group;
    rankingServiceSpy.getTeamsScoreRankingByGroup.and.returnValue(of(scores));
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(of({ propertyValue: SwissTieBreakRule.BUCHHOLZ } as any));

    component.ngOnInit();

    expect(rankingServiceSpy.getTeamsScoreRankingByGroup).toHaveBeenCalledOnceWith(11);
    expect(component.teamScores).toBe(scores);
    expect((component as any).swissTieBreakRule).toBe(SwissTieBreakRule.BUCHHOLZ);
    expect((component as any).showSwissTieBreakScore()).toBeTrue();
  });

  it('should format swiss tie-break value based on selected rule', () => {
    component.tournament = { type: TournamentType.SWISS } as Tournament;
    (component as any).swissTieBreakRule = SwissTieBreakRule.BUCHHOLZ;
    expect((component as any).getSwissTieBreakValue({ swissTieBreakValue: 12.6 } as ScoreOfTeam)).toBe('13');

    (component as any).swissTieBreakRule = SwissTieBreakRule.SONNEBORN_BERGER;
    expect((component as any).getSwissTieBreakValue({ swissTieBreakValue: 12.64 } as ScoreOfTeam)).toBe('12.6');
  });

  it('should detect draw winner based on sortingIndex and fightsFinished', () => {
    component.fightsFinished = true;
    component.teamScores = [
      { sortingIndex: 0, team: createTeam('A') },
      { sortingIndex: 0, team: createTeam('B') }
    ] as unknown as ScoreOfTeam[];

    expect(component.isDrawWinner(0)).toBeTrue();
    expect(component.isDrawWinner(1)).toBeFalse();
  });

  it('should return draw winners for an index', () => {
    const teamA = createTeam('A');
    const teamB = createTeam('B');
    component.fightsFinished = true;
    component.teamScores = [
      { sortingIndex: 0, team: teamA },
      { sortingIndex: 0, team: teamB },
      { sortingIndex: 1, team: createTeam('C') }
    ] as unknown as ScoreOfTeam[];

    const winners = component.getDrawWinners(0);

    expect(winners).toEqual([teamA, teamB]);
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.closed, 'emit');

    component.closeDialog();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });

  it('should set untie teams popup and draw teams when untieTeams is called', () => {
    const teamA = createTeam('A');
    component.fightsFinished = true;
    component.teamScores = [{ sortingIndex: 0, team: teamA }] as unknown as ScoreOfTeam[];

    component.untieTeams(0);

    expect((component as any).untieTeamsPopup).toBeTrue();
    expect((component as any).drawTeams).toEqual([teamA]);
  });

  it('should close untie popup and emit duels on untieFights when duels exist', () => {
    const duels = [new Duel()];
    spyOn(component.closed, 'emit');
    (component as any).untieTeamsPopup = true;

    component.untieFights(duels);

    expect((component as any).untieTeamsPopup).toBeFalse();
    expect(component.closed.emit).toHaveBeenCalledOnceWith(duels);
  });

  it('should close untie popup and not emit when untieFights receives empty duels', () => {
    spyOn(component.closed, 'emit');
    (component as any).untieTeamsPopup = true;

    component.untieFights([]);

    expect((component as any).untieTeamsPopup).toBeFalse();
    expect(component.closed.emit).not.toHaveBeenCalled();
  });

  it('should navigate to statistics and close dialog when openStatistics is called', () => {
    component.tournament = { id: 12, name: 'T Stats' } as Tournament;
    spyOn(component, 'closeDialog');

    component.openStatistics();

    expect(component.closeDialog).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledOnceWith(['/tournaments/statistics'], { state: { tournamentId: 12 } });
  });

  it('should build team members tooltip text with line breaks', () => {
    const team = {
      members: [{ id: 1 } as any, { id: 2 } as any]
    } as Team;
    nameUtilsSpy.getNameLastname.and.returnValues('John Doe', 'Jane Roe');

    const result = component.getTeamMembers(team);

    expect(result).toBe('John Doe\nJane Roe\n');
  });
});

