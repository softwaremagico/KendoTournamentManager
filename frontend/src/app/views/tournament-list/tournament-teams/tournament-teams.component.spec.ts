import {of} from 'rxjs';
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
import {TranslocoService} from '@ngneat/transloco';
import {Tournament} from '../../../models/tournament';
import {Team} from '../../../models/team';
import {Participant} from '../../../models/participant';
import {Role} from '../../../models/role';
import {TournamentScore} from '../../../models/tournament-score.model';

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
      translocoServiceSpy
    );
    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize teams and participants in ngOnInit', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const p2 = buildParticipant(2, 'Bob', 'Jones');
    const team1 = buildTeam(1, 'Team A', [p1]);
    const role1 = { participant: p2 } as Role;

    teamServiceSpy.getFromTournament.and.returnValue(of([team1]));
    roleServiceSpy.getFromTournamentAndType.and.returnValue(of([role1]));

    component.ngOnInit();

    expect(teamServiceSpy.getFromTournament).toHaveBeenCalled();
    expect(roleServiceSpy.getFromTournamentAndType).toHaveBeenCalled();
    expect(component.teams.length).toBe(1);
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
    expect(participants.length).toBe(1);
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
    spyOn(component.onClosed, 'emit');

    const file = new File(['team,name'], 'teams.csv', { type: 'text/csv' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      get: () => ({ item: () => file, length: 1 } as any)
    });

    component.handleFileInput({ currentTarget: input } as any);

    expect(csvServiceSpy.addTeams).toHaveBeenCalledWith(file, 1);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('teamStored');
    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should get members container for a team from the Map', () => {
    const p1 = buildParticipant(1, 'Alice', 'Smith');
    const team1 = buildTeam(1, 'Team A', [p1]);
    component.members.set(team1, [p1]);

    const members = component.getMembersContainer(team1);

    expect(members).toEqual([p1]);
  });
});

