import {of, Subject} from 'rxjs';
import {TournamentTeamsComponent} from './tournament-teams.component';
import {MessageService} from '../../../services/message.service';
import {LoggerService} from '../../../services/logger.service';
import {TeamService} from '../../../services/team.service';
import {RoleService} from '../../../services/role.service';
import {NameUtilsService} from '../../../services/name-utils.service';
import {SystemOverloadService} from '../../../services/notifications/system-overload.service';
import {RbacService} from '../../../services/rbac/rbac.service';
import {GroupService} from '../../../services/group.service';
import {FightService} from '../../../services/fight.service';
import {RankingService} from '../../../services/ranking.service';
import {StatisticsChangedService} from '../../../services/notifications/statistics-changed.service';
import {FilterResetService} from '../../../services/notifications/filter-reset.service';
import {CsvService} from '../../../services/csv-service';
import {TranslocoService} from '@jsverse/transloco';
import {Tournament} from '../../../models/tournament';
import {Team} from '../../../models/team';
import {Participant} from '../../../models/participant';
import {Role} from '../../../models/role';
import {TournamentScore} from '../../../models/tournament-score.model';
import {Group} from '../../../models/group';
import {ChangeDetectorRef} from '@angular/core';
import {ScoreOfCompetitor} from '../../../models/score-of-competitor';

describe('TournamentTeamsComponent', () => {
  let component: TournamentTeamsComponent;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let loggerServiceSpy: jasmine.SpyObj<LoggerService>;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;
  let roleServiceSpy: jasmine.SpyObj<RoleService>;
  let nameUtilsServiceSpy: jasmine.SpyObj<NameUtilsService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let statisticsChangedServiceSpy: jasmine.SpyObj<StatisticsChangedService>;
  let filterResetServiceSpy: jasmine.SpyObj<FilterResetService>;
  let csvServiceSpy: jasmine.SpyObj<CsvService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let changeDetectorRefSpy: jasmine.SpyObj<ChangeDetectorRef>;

  const buildTournament = (): Tournament => {
    const t = new Tournament();
    t.id = 1;
    t.name = 'Test Tournament';
    t.teamSize = 3;
    t.tournamentScore = new TournamentScore();
    return t;
  };

  const buildParticipant = (id: number, name: string, lastname: string): Participant => ({
    id,
    name,
    lastname,
    idCard: 'ID' + id
  } as Participant);

  const buildTeam = (id: number, name: string, members: (Participant | undefined)[]): Team => ({
    id,
    name,
    members: members as Participant[],
    locked: false,
    editing: false
  } as Team);

  beforeEach(() => {
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage', 'handleError', 'warningMessage']);
    loggerServiceSpy = jasmine.createSpyObj('LoggerService', ['info', 'error']);
    teamServiceSpy = jasmine.createSpyObj('TeamService', [
      'getFromTournament', 'update', 'add', 'delete', 'deleteByMemberAndTournament',
      'setAll', 'getTeamsByTournament'
    ]);
    roleServiceSpy = jasmine.createSpyObj('RoleService', ['getFromTournamentAndType']);
    nameUtilsServiceSpy = jasmine.createSpyObj('NameUtilsService', ['getLastnameNameNoSpaces']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isBusy: { next: jasmine.createSpy('next') }
    });
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', [
      'getFromTournament', 'addTeamsToGroup', 'deleteTeamsFromGroup'
    ]);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['getFromTournament']);
    rankingServiceSpy = jasmine.createSpyObj('RankingService', ['getCompetitorsGlobalScoreRanking']);
    statisticsChangedServiceSpy = jasmine.createSpyObj('StatisticsChangedService', [], {
      areStatisticsChanged: { next: jasmine.createSpy('next') }
    });
    filterResetServiceSpy = jasmine.createSpyObj('FilterResetService', [], {
      resetFilter: { next: jasmine.createSpy('next') }
    });
    csvServiceSpy = jasmine.createSpyObj('CsvService', ['addTeams']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    changeDetectorRefSpy = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck', 'detectChanges']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    fightServiceSpy.getFromTournament.and.returnValue(of([]));
    groupServiceSpy.getFromTournament.and.returnValue(of([]));
    translocoServiceSpy.translate.and.returnValue('translated');

    component = new TournamentTeamsComponent(
      messageServiceSpy,
      loggerServiceSpy,
      teamServiceSpy,
      roleServiceSpy,
      nameUtilsServiceSpy,
      systemOverloadServiceSpy,
      rbacServiceSpy,
      groupServiceSpy,
      fightServiceSpy,
      rankingServiceSpy,
      statisticsChangedServiceSpy,
      filterResetServiceSpy,
      csvServiceSpy,
      translocoServiceSpy,
      changeDetectorRefSpy
    );
    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize teams and participants in ngOnInit', () => {
    const initialUserListData = component.userListData;
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const team1 = buildTeam(1, 'Team A', [p1]);
    const role1 = { participant: p2 } as Role;

    teamServiceSpy.getFromTournament.and.returnValue(of([team1]));
    roleServiceSpy.getFromTournamentAndType.and.returnValue(of([role1]));

    component.ngOnInit();

    expect(teamServiceSpy.getFromTournament).toHaveBeenCalled();
    expect(roleServiceSpy.getFromTournamentAndType).toHaveBeenCalled();
    expect(component.teams).toHaveSize(1);
    expect(component.userListData).not.toBe(initialUserListData);
    expect(component.groupsLoaded).toBeTrue();
  });

  it('should wait for groups to load before enabling team creation', () => {
    const groups = new Subject<Group[]>();
    groupServiceSpy.getFromTournament.and.returnValue(groups);
    teamServiceSpy.getFromTournament.and.returnValue(of([]));
    roleServiceSpy.getFromTournamentAndType.and.returnValue(of([]));

    component.ngOnInit();

    expect(component.groupsLoaded).toBeFalse();
    groups.next([]);
    groups.complete();
    expect(component.groupsLoaded).toBeTrue();
  });

  it('should mark the view for check after balancing teams asynchronously', () => {
    const participants = [
      buildParticipant(1, 'Alice', 'Smith'),
      buildParticipant(2, 'Bob', 'Jones'),
      buildParticipant(3, 'Carol', 'Brown')
    ];
    const team = buildTeam(1, 'Team A', []);
    const ranking = participants.map((participant: Participant): ScoreOfCompetitor => {
      const score = new ScoreOfCompetitor();
      score.competitor = participant;
      return score;
    });
    component.teams = [team];
    component.members.set(team, []);
    component.userListData.participants = participants;
    component.userListData.filteredParticipants = participants;
    const rankingResponse = new Subject<ScoreOfCompetitor[]>();
    rankingServiceSpy.getCompetitorsGlobalScoreRanking.and.returnValue(rankingResponse);
    teamServiceSpy.update.and.returnValue(of(team));

    component.balancedTeams();
    expect(changeDetectorRefSpy.markForCheck).not.toHaveBeenCalled();
    rankingResponse.next(ranking);

    expect(changeDetectorRefSpy.markForCheck).toHaveBeenCalled();
  });

  it('should get card title as lastname + name when member exists', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team1 = buildTeam(1, 'Team A', [p1]);
    component.members.set(team1, [p1]);

    const title = component.getCardTitle(team1, 0);

    expect(title).toBe('Smith, Alice');
  });

  it('should return empty string for card title when member is undefined', () => {
    const team1 = buildTeam(1, 'Team A', []);
    component.members.set(team1, [undefined]);

    const title = component.getCardTitle(team1, 0);

    expect(title).toBe('');
  });

  it('should get card subtitle as club name when member has club', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    (p1 as any).club = { name: 'Kendo Club' };
    const team1 = buildTeam(1, 'Team A', [p1]);
    component.members.set(team1, [p1]);

    const subtitle = component.getCardSubTitle(team1, 0);

    expect(subtitle).toBe('Kendo Club');
  });

  it('should return empty string from card subtitle when member has no club', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team1 = buildTeam(1, 'Team A', [p1]);
    component.members.set(team1, [p1]);

    const subtitle = component.getCardSubTitle(team1, 0);

    expect(subtitle).toBe('');
  });

  it('should lock team when team is locked', () => {
    const team1 = buildTeam(1, 'Team A', []);
    team1.locked = true;

    expect(component.isTeamLocked(team1)).toBeTrue();
  });

  it('should not lock team when team is not locked', () => {
    const team1 = buildTeam(1, 'Team A', []);
    team1.locked = false;

    expect(component.isTeamLocked(team1)).toBeFalse();
  });

  it('should allow editing when tournament teamSize is greater than 1', () => {
    const team1 = buildTeam(1, 'Team A', []);
    component.tournament.teamSize = 3;

    component.setEditable(team1, true);

    expect(team1.editing).toBeTrue();
  });

  it('should not allow editing when tournament teamSize is 1', () => {
    const team1 = buildTeam(1, 'Team A', []);
    component.tournament.teamSize = 1;

    component.setEditable(team1, true);

    expect(team1.editing).toBeFalse();
  });

  it('should get random member from list and remove it', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const participants = [p1, p2];

    const selectedMember = component.getRandomMember(participants);

    expect(selectedMember).toBeTruthy();
    expect(participants).toHaveSize(1);
  });

  it('should download PDF and trigger file download when tournament has id', () => {
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:http://localhost');
    teamServiceSpy.getTeamsByTournament.and.returnValue(of(new Blob(['test'])));

    component.downloadPDF();

    expect(teamServiceSpy.getTeamsByTournament).toHaveBeenCalledWith(1);
    expect((component as any).loadingGlobal).toBeFalse();
  });

  it('should handle file input and import CSV teams', () => {
    csvServiceSpy.addTeams.and.returnValue(of([]));
    spyOn(component.closed, 'emit');

    const file = new File(['team,name'], 'teams.csv', { type: 'text/csv' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      get: () => ({ item: () => file, length: 1 } as any)
    });

    component.handleFileInput({ currentTarget: input } as any);

    expect(csvServiceSpy.addTeams).toHaveBeenCalledWith(file, 1);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('teamStored');
    expect(component.closed.emit).toHaveBeenCalled();
  });

  it('should get members container for a team from the Map', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team1 = buildTeam(1, 'Team A', [p1]);
    component.members.set(team1, [p1]);

    const members = component.getMembersContainer(team1);

    expect(members).toEqual([p1]);
  });

  it('locks participants when the tournament is locked and removes assigned members', () => {
    component.tournament.locked = true;
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const team = buildTeam(1, 'Team A', [p2]);
    roleServiceSpy.getFromTournamentAndType.and.returnValue(of([{participant: p1}, {participant: p2}] as Role[]));
    teamServiceSpy.getFromTournament.and.returnValue(of([team]));

    component.ngOnInit();

    expect(p1.locked).toBeTrue();
    expect(component.userListData.participants).toEqual([p1]);
    expect(component.members.get(team)).toEqual([p2]);
    expect(component.teamSize).toEqual([0, 1, 2]);
  });

  it('locks teams that already have fights and ignores empty fight references', () => {
    const team1 = buildTeam(1, 'Team A', []);
    const team2 = buildTeam(2, 'Team B', []);
    roleServiceSpy.getFromTournamentAndType.and.returnValue(of([]));
    teamServiceSpy.getFromTournament.and.returnValue(of([team1, team2]));
    fightServiceSpy.getFromTournament.and.returnValue(of([
      {team1, team2: undefined}, {team1: undefined, team2}
    ] as any));

    component.ngOnInit();

    expect(team1.locked).toBeTrue();
    expect(team2.locked).toBeTrue();
  });

  it('reorders and transfers members between teams and the available list', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const p3 = buildParticipant(3, 'Carol', 'Brown');
    const source = buildTeam(1, 'Team A', [p1, p2]);
    const target = buildTeam(2, 'Team B', [p3]);
    component.teams = [source, target];
    component.members.set(source, [p1, p2]);
    component.members.set(target, [p3]);
    component.userListData.participants = [];
    component.userListData.filteredParticipants = [];
    teamServiceSpy.update.and.returnValue(of(source));

    const reorderEvent = {
      previousContainer: {data: [p1, p2]}, container: {data: [p1, p2]},
      previousIndex: 1, item: {data: p2}
    } as any;
    component.dropMember(reorderEvent, source, 0);
    expect(source.members).toEqual([p2, p1]);

    component.userListData.participants = [p3];
    const moveEvent = {
      previousContainer: {data: [p3]}, container: {data: source.members},
      previousIndex: 0, item: {data: p3}
    } as any;
    component.dropMember(moveEvent, source, 1);
    expect(source.members[1]).toBe(p3);
    expect(component.userListData.participants).toEqual([]);
    expect(filterResetServiceSpy.resetFilter.next).toHaveBeenCalledWith(true);
  });

  it('removes a member from a team and returns it to the available list', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team = buildTeam(1, 'Team A', [p1]);
    component.teams = [team];
    component.members.set(team, [p1]);
    teamServiceSpy.deleteByMemberAndTournament.and.returnValue(of(team));
    teamServiceSpy.update.and.returnValue(of(team));
    component.tournament.teamSize = 1;
    component.userListData.participants = [];
    component.userListData.filteredParticipants = [];

    component.members.set(team, team.members);
    component.removeFromTeam({
      previousContainer: {data: team.members}, container: {data: []},
      previousIndex: 0, item: {data: p1}
    } as any);

    expect(team.members[0]).toBeUndefined();
    expect(teamServiceSpy.deleteByMemberAndTournament).toHaveBeenCalledWith(p1, component.tournament);
    expect(component.userListData.participants).toEqual([p1]);
  });

  it('accepts drops only on unlocked teams with a free slot', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team = buildTeam(1, 'Team A', [p1]);

    expect(component.dropListEnterPredicate(0, team)({} as any, {} as any)).toBeFalse();
    expect(component.dropListEnterPredicate(1, team)({} as any, {} as any)).toBeTrue();
    team.locked = true;
    expect(component.dropListEnterPredicate(1, team)({} as any, {} as any)).toBeFalse();
    expect(component.dropListEnterPredicate(1, undefined as any)({} as any, {} as any)).toBeTrue();
  });

  it('updates team names, adds teams to the default group and deletes teams', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team = buildTeam(1, 'Team A', [p1]);
    const group = {...new Group(), id: 4} as Group;
    component.groups = [group];
    component.teams = [team];
    component.userListData.participants = [];
    component.userListData.filteredParticipants = [];
    teamServiceSpy.update.and.returnValue(of(team));
    teamServiceSpy.add.and.returnValue(of(buildTeam(2, 'Team B', [])));
    teamServiceSpy.delete.and.returnValue(of(team));
    groupServiceSpy.addTeamsToGroup.and.returnValue(of(group));
    groupServiceSpy.deleteTeamsFromGroup.and.returnValue(of(group));

    component.updateTeamName(team);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoTeamUpdated');

    component.addTeam();
    expect(component.teams).toHaveSize(2);
    expect(groupServiceSpy.addTeamsToGroup).toHaveBeenCalled();

    component.deleteTeam(team);
    expect(groupServiceSpy.deleteTeamsFromGroup).toHaveBeenCalledWith(4, [team]);
    expect(component.teams).toEqual([component.teams[0]]);
    expect(component.userListData.participants).toEqual([p1]);
  });

  it('assigns one team per participant when the team size is one', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Alice', 'Smith');
    component.tournament.teamSize = 1;
    component.userListData.participants = [p1, p2];
    nameUtilsServiceSpy.getLastnameNameNoSpaces.and.returnValue('smithAlice');
    const stored = [buildTeam(1, 'smithAlice', [p1]), buildTeam(2, 'smithAlice (2)', [p2])];
    teamServiceSpy.setAll.and.returnValue(of(stored));

    component.generateTeams();

    expect(teamServiceSpy.setAll).toHaveBeenCalledTimes(1);
    expect((teamServiceSpy.setAll.calls.mostRecent().args[0] as Team[]).map(team => team.name))
      .toEqual(['smithAlice', 'smithAlice (2)']);
    expect(component.teams).toEqual(stored);
  });

  it('creates random teams with the remaining participants', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const team = buildTeam(1, 'Team A', []);
    component.teams = [team];
    component.members.set(team, []);
    component.userListData.participants = [p1, p2];
    component.userListData.filteredParticipants = [p1, p2];
    teamServiceSpy.update.and.returnValue(of(team));

    component.randomTeams();

    expect(team.members.filter(Boolean).map(member => member!.id).sort()).toEqual([1, 2]);
    expect(teamServiceSpy.update).toHaveBeenCalledWith(team);
  });

  it('reports a CSV import error when the imported team is invalid', () => {
    const file = new File(['team,invalid'], 'teams.csv', {type: 'text/csv'});
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      get: () => ({item: () => file, length: 1} as any)
    });
    csvServiceSpy.addTeams.and.returnValue(of([buildTeam(1, 'Invalid', [])]));
    spyOn(component.closed, 'emit');

    component.handleFileInput({currentTarget: input} as any);

    expect(messageServiceSpy.errorMessage).toHaveBeenCalledWith('translated');
    expect(component.closed.emit).not.toHaveBeenCalled();
  });
});
