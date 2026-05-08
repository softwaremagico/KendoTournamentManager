import { of } from 'rxjs';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { LeagueGeneratorComponent } from './league-generator.component';
import { TeamService } from '../../../services/team.service';
import { RbacService } from '../../../services/rbac/rbac.service';
import { TournamentExtendedPropertiesService } from '../../../services/tournament-extended-properties.service';
import { MessageService } from '../../../services/message.service';
import { RankingService } from '../../../services/ranking.service';
import { Tournament } from '../../../models/tournament';
import { TournamentScore } from '../../../models/tournament-score.model';
import { TournamentType } from '../../../models/tournament-type';
import { Team } from '../../../models/team';
import { Participant } from '../../../models/participant';
import { DrawResolution } from '../../../models/draw-resolution';
import { LeagueFightsOrder } from '../../../models/league-fights-order';
import { TournamentExtraPropertyKey } from '../../../models/tournament-extra-property-key';

describe('LeagueGeneratorComponent', () => {
  let component: LeagueGeneratorComponent;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;

  const buildTournament = (type: TournamentType = TournamentType.LEAGUE): Tournament => {
    const t = new Tournament();
    t.id = 1;
    t.name = 'Test Tournament';
    t.type = type;
    t.teamSize = 3;
    t.tournamentScore = new TournamentScore();
    return t;
  };

  const buildParticipant = (id: number): Participant => ({ id, name: 'Name' + id, lastname: 'Last' + id } as Participant);
  const buildTeam = (id: number, name: string, members: Participant[] = []): Team => ({ id, name, members } as Team);

  beforeEach(() => {
    teamServiceSpy = jasmine.createSpyObj('TeamService', ['getFromTournament']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj('TournamentExtendedPropertiesService', ['getByTournament', 'update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage']);
    rankingServiceSpy = jasmine.createSpyObj('RankingService', ['getCompetitorsGlobalScoreRanking']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    teamServiceSpy.getFromTournament.and.returnValue(of([]));
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([]));
    tournamentExtendedPropertiesServiceSpy.update.and.returnValue(of({}) as any);
    rankingServiceSpy.getCompetitorsGlobalScoreRanking.and.returnValue(of([]));

    component = new LeagueGeneratorComponent(
      teamServiceSpy,
      rbacServiceSpy,
      tournamentExtendedPropertiesServiceSpy,
      messageServiceSpy,
      rankingServiceSpy
    );

    component.tournament = buildTournament(TournamentType.LEAGUE);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize default properties in constructor', () => {
    expect(component.areFightsMaximized).toBeFalse();
    expect(component.firstInFirstOut).toBeTrue();
    expect(component.avoidDuplicatedFights).toBeTrue();
    expect(component.selectedDrawResolution).toBe(DrawResolution.BOTH_ELIMINATED);
  });

  it('should initialize teams data in ngOnInit', () => {
    const teams = [buildTeam(1, 'B Team'), buildTeam(2, 'A Team')];
    teamServiceSpy.getFromTournament.and.returnValue(of(teams));

    component.ngOnInit();

    expect(teamServiceSpy.getFromTournament).toHaveBeenCalledWith(component.tournament);
    expect(component.teams.length).toBe(2);
    expect(component.teamListData.teams[0].name).toBe('A Team');
  });

  it('should emit teamsOrder when acceptAction is called', () => {
    spyOn(component.onClosed, 'emit');
    component.teamsOrder = [buildTeam(1, 'Team A')];

    component.acceptAction();

    expect(component.onClosed.emit).toHaveBeenCalledWith(component.teamsOrder);
  });

  it('should emit empty array when cancelDialog is called', () => {
    spyOn(component.onClosed, 'emit');

    component.cancelDialog();

    expect(component.onClosed.emit).toHaveBeenCalledWith([]);
  });

  it('should sort teams alphabetically in sortedTeams', () => {
    component.teamListData.teams = [buildTeam(1, 'B Team'), buildTeam(2, 'A Team')];
    component.teamListData.filteredTeams = [...component.teamListData.teams];

    component.sortedTeams();

    expect(component.teamsOrder.length).toBe(2);
    expect(component.teamsOrder[0].name).toBe('A Team');
    expect(component.teamListData.teams.length).toBe(0);
  });

  it('should return and remove random team from list', () => {
    const teams = [buildTeam(1, 'A'), buildTeam(2, 'B')];
    spyOn(Math, 'random').and.returnValue(0);

    const selected = component.getRandomTeam(teams);

    expect(selected.name).toBe('A');
    expect(teams.length).toBe(1);
  });

  it('should reverse teamsOrder in reverseTeams', () => {
    component.teamsOrder = [buildTeam(1, 'A'), buildTeam(2, 'B')];

    component.reverseTeams();

    expect(component.teamsOrder[0].name).toBe('B');
    expect(component.teamsOrder[1].name).toBe('A');
  });

  it('should return translation tag for draw resolution', () => {
    expect(component.getDrawResolutionTranslationTag(DrawResolution.OLDEST_ELIMINATED)).toBe('oldestEliminated');
    expect(component.getDrawResolutionTranslationTag(undefined as any)).toBe('');
  });

  it('should return hint tag for draw resolution', () => {
    expect(component.getDrawResolutionHintTag(DrawResolution.NEWEST_ELIMINATED)).toBe('newestEliminatedHint');
    expect(component.getDrawResolutionHintTag(undefined as any)).toBe('');
  });

  it('should persist max fights toggle in service', () => {
    const event = { checked: true } as MatSlideToggleChange;

    component.maxFightsToggle(event);

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyKey).toBe(TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
    expect(updateArg.propertyValue).toBe('true');
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoTournamentUpdated');
  });

  it('should persist draw resolution selection in service', () => {
    component.selectDrawResolution(DrawResolution.BOTH_ELIMINATED);

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(component.selectedDrawResolution).toBe(DrawResolution.BOTH_ELIMINATED);
    expect(updateArg.propertyKey).toBe(TournamentExtraPropertyKey.KING_DRAW_RESOLUTION);
    expect(updateArg.propertyValue).toBe(DrawResolution.BOTH_ELIMINATED);
  });

  it('should persist fifo toggle in service', () => {
    component.fifoToggle({ checked: false } as MatSlideToggleChange);

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyKey).toBe(TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION);
    expect(updateArg.propertyValue).toBe(LeagueFightsOrder.LIFO);
  });

  it('should persist avoid duplicates toggle in service', () => {
    component.avoidDuplicatesToggle({ checked: true } as MatSlideToggleChange);

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyKey).toBe(TournamentExtraPropertyKey.AVOID_DUPLICATES);
    expect(updateArg.propertyValue).toBe('true');
  });

  it('should reorder teams by ranking in balancedTeams', () => {
    const p1 = buildParticipant(1);
    const p2 = buildParticipant(2);
    const p3 = buildParticipant(3);
    const teamA = buildTeam(1, 'Team A', [p2, p3]);
    const teamB = buildTeam(2, 'Team B', [p1]);

    component.teams = [teamA, teamB];
    rankingServiceSpy.getCompetitorsGlobalScoreRanking.and.returnValue(of([
      { competitor: p1 } as any,
      { competitor: p2 } as any,
      { competitor: p3 } as any
    ]));

    component.balancedTeams();

    expect(component.teamsOrder.length).toBe(2);
    expect(component.teamsOrder[0].name).toBe('Team B');
  });
});

