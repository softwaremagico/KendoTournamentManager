import {BehaviorSubject, of} from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';
import {FightListComponent} from './fight-list.component';
import {MessageService} from '../../services/message.service';
import {FightService} from '../../services/fight.service';
import {TournamentService} from '../../services/tournament.service';
import {GroupService} from '../../services/group.service';
import {DuelService} from '../../services/duel.service';
import {TimeChangedService} from '../../services/notifications/time-changed.service';
import {DuelChangedService} from '../../services/notifications/duel-changed.service';
import {UntieAddedService} from '../../services/notifications/untie-added.service';
import {UserSessionService} from '../../services/user-session.service';
import {MembersOrderChangedService} from '../../services/notifications/members-order-changed.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {GroupUpdatedService} from '../../services/notifications/group-updated.service';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {EnvironmentService} from '../../environment.service';
import {LoginService} from '../../services/login.service';
import {AudioService} from '../../services/audio.service';
import {ProjectModeChangedService} from '../../services/notifications/project-mode-changed.service';
import {FileService} from '../../services/file.service';
import {RxStompService} from '../../websockets/rx-stomp.service';
import {Tournament} from '../../models/tournament';
import {TournamentScore} from '../../models/tournament-score.model';
import {TournamentType} from '../../models/tournament-type';
import {Fight} from '../../models/fight';
import {Duel} from '../../models/duel';
import {Group} from '../../models/group';
import {Team} from '../../models/team';
import {Participant} from '../../models/participant';
import {Score} from '../../models/score';
import {DuelType} from '../../models/duel-type';

describe('FightListComponent', () => {
  let component: FightListComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let activatedRouteStub: ActivatedRoute;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let environmentServiceSpy: jasmine.SpyObj<EnvironmentService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let duelServiceSpy: jasmine.SpyObj<DuelService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let fileServiceSpy: jasmine.SpyObj<FileService>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;
  let audioServiceSpy: jasmine.SpyObj<AudioService>;

  let timeChangedServiceMock: TimeChangedService;
  let duelChangedServiceMock: DuelChangedService;
  let untieAddedServiceMock: UntieAddedService;
  let membersOrderChangedServiceMock: MembersOrderChangedService;
  let groupUpdatedServiceMock: GroupUpdatedService;
  let systemOverloadServiceMock: SystemOverloadService;
  let rxStompServiceMock: RxStompService;
  let projectModeChangedServiceMock: ProjectModeChangedService;

  const buildTournament = (): Tournament => {
    const tournament = new Tournament();
    tournament.id = 9;
    tournament.name = 'Winter Cup';
    tournament.type = TournamentType.LEAGUE;
    tournament.teamSize = 3;
    tournament.shiaijos = 2;
    tournament.duelsDuration = 180;
    tournament.tournamentScore = new TournamentScore();
    return tournament;
  };

  const buildParticipant = (id: number, name: string, lastname: string, clubName = 'Club'): Participant => ({
    id,
    name,
    lastname,
    idCard: `ID-${id}`,
    club: { name: clubName } as any,
    hasAvatar: false
  } as Participant);

  const buildTeam = (id: number, name: string, members: (Participant | undefined)[]): Team => ({
    id,
    name,
    members
  } as Team);

  const buildDuel = (id: number, finished = false, substitute = false): Duel => ({
    id,
    finished,
    substitute,
    competitor1: buildParticipant(id * 10 + 1, 'Alice', 'Álvarez', 'Dojo Norte'),
    competitor2: buildParticipant(id * 10 + 2, 'Bob', 'Muñoz', 'Dojo Sur'),
    competitor1Score: [],
    competitor2Score: [],
    duration: 0,
    totalDuration: 180,
    type: DuelType.STANDARD
  } as any);

  const buildFight = (id: number, duels: Duel[], shiaijo = 0, level = 0, finishedNames = false): Fight => ({
    id,
    duels,
    shiaijo,
    level,
    team1: buildTeam(id * 10 + 1, finishedNames ? 'Equipo Finalizado' : 'Equipo Águila', [buildParticipant(1, 'Álvaro', 'Pérez', 'Kendo Club')]),
    team2: buildTeam(id * 10 + 2, 'Equipo León', [buildParticipant(2, 'Lucía', 'Gómez', 'Bushido Club')])
  } as Fight);

  const buildGroup = (id: number, level: number, fights: Fight[], unties: Duel[] = [], shiaijo = 0): Group => ({
    id,
    level,
    index: id,
    fights,
    unties,
    shiaijo,
    teams: []
  } as any);

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'getCurrentNavigation']);
    activatedRouteStub = {
      snapshot: {
        queryParamMap: {
          get: jasmine.createSpy('get').and.returnValue(null)
        }
      }
    } as any;
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', ['get', 'update']);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['create', 'update', 'delete', 'getFightSummaryPDf']);
    environmentServiceSpy = jasmine.createSpyObj('EnvironmentService', ['getWebsocketPrefix']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['getFromTournament']);
    duelServiceSpy = jasmine.createSpyObj('DuelService', ['update']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getSwappedColors', 'getSwappedTeams', 'setSwappedColors', 'setSwappedTeams']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    fileServiceSpy = jasmine.createSpyObj('FileService', ['getTournamentPicture']);
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['getJwtValue', 'setGuestUserSession']);
    audioServiceSpy = jasmine.createSpyObj('AudioService', ['playWhistle', 'stopWhistle']);

    timeChangedServiceMock = {
      isElapsedTimeChanged: new BehaviorSubject<number>(0),
      isTotalTimeChanged: new BehaviorSubject<number>(0)
    } as TimeChangedService;
    duelChangedServiceMock = {
      isDuelUpdated: new BehaviorSubject<Duel>(new Duel())
    } as DuelChangedService;
    untieAddedServiceMock = {
      isDuelsAdded: new BehaviorSubject<Duel[]>([])
    } as UntieAddedService;
    membersOrderChangedServiceMock = {
      membersOrderChanged: new BehaviorSubject<Fight>(new Fight()),
      membersOrderAllowed: new BehaviorSubject<boolean>(false)
    } as MembersOrderChangedService;
    groupUpdatedServiceMock = {
      isGroupUpdated: new BehaviorSubject<Group>(new Group())
    } as GroupUpdatedService;
    systemOverloadServiceMock = {
      isTransactionalBusy: { next: jasmine.createSpy('next') }
    } as any;
    rxStompServiceMock = {} as RxStompService;
    projectModeChangedServiceMock = {
      isProjectMode: new BehaviorSubject<boolean>(false)
    } as ProjectModeChangedService;

    routerSpy.getCurrentNavigation.and.returnValue({ extras: { state: { tournamentId: 9 } } } as any);
    environmentServiceSpy.getWebsocketPrefix.and.returnValue('/ws');
    userSessionServiceSpy.getSwappedColors.and.returnValue(false);
    userSessionServiceSpy.getSwappedTeams.and.returnValue(false);
    loginServiceSpy.getJwtValue.and.returnValue('jwt');
    tournamentServiceSpy.update.and.returnValue(of({}) as any);
    duelServiceSpy.update.and.returnValue(of({}) as any);
    fightServiceSpy.create.and.returnValue(of([] as any));
    fightServiceSpy.update.and.returnValue(of({}) as any);
    fightServiceSpy.delete.and.returnValue(of({}) as any);
    fightServiceSpy.getFightSummaryPDf.and.returnValue(of(new Blob(['pdf'])) as any);
    fileServiceSpy.getTournamentPicture.and.returnValue(of(null) as any);

    component = new FightListComponent(
      routerSpy,
      activatedRouteStub,
      tournamentServiceSpy,
      fightServiceSpy,
      environmentServiceSpy,
      groupServiceSpy,
      duelServiceSpy,
      timeChangedServiceMock,
      duelChangedServiceMock,
      untieAddedServiceMock,
      groupUpdatedServiceMock,
      userSessionServiceSpy,
      membersOrderChangedServiceMock,
      messageServiceSpy,
      rbacServiceSpy,
      fileServiceSpy,
      systemOverloadServiceMock,
      rxStompServiceMock,
      loginServiceSpy,
      audioServiceSpy,
      projectModeChangedServiceMock
    );

    component.tournament = buildTournament();
    component.groups = [];
    component.filteredFights = new Map<number, Fight[]>();
    component.filteredUnties = new Map<number, Duel[]>();
    component.filteredLevels = [];
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should select fight and infer selected group', () => {
    const fight = buildFight(1, [buildDuel(1)]);
    const group = buildGroup(1, 0, [fight]);
    component.groups = [group];

    component.selectFight(fight);

    expect(component.selectedFight).toBe(fight);
    expect(component.selectedGroup).toBe(group);
  });

  it('should clear selectedGroup when selectFight receives undefined', () => {
    component.selectedGroup = buildGroup(1, 0, []);

    component.selectFight(undefined);

    expect(component.selectedFight).toBeUndefined();
    expect(component.selectedGroup).toBeUndefined();
  });

  it('should detect when a fight is over only if all duels are finished', () => {
    expect(component.isFightOver(buildFight(1, [buildDuel(1, true), buildDuel(2, true)]))).toBeTrue();
    expect(component.isFightOver(buildFight(2, [buildDuel(3, true), buildDuel(4, false)]))).toBeFalse();
  });

  it('should allow timer only when selected duel can start', () => {
    spyOn(component.resetTimerPosition, 'next');
    component.selectedDuel = buildDuel(1);

    component.showTimer(true);

    expect(component.timer).toBeTrue();
    expect(component.resetTimerPosition.next).toHaveBeenCalledWith(true);
  });

  it('should not show timer when duel cannot start', () => {
    spyOn(component.resetTimerPosition, 'next');
    component.selectedDuel = { competitor1: null, competitor2: null } as any;

    component.showTimer(true);

    expect(component.timer).toBeFalse();
    expect(component.resetTimerPosition.next).not.toHaveBeenCalled();
  });

  it('should set and remove ippon scores for absent competitor', () => {
    const duel = {
      competitor1: buildParticipant(1, 'Ana', 'Lopez'),
      competitor2: null,
      competitor1Score: [],
      competitor2Score: []
    } as any;

    component.setIpponScores(duel);
    expect(duel.competitor1Score).toEqual([Score.FUSEN_GACHI, Score.FUSEN_GACHI]);

    component.removeIpponScores(duel);
    expect(duel.competitor1Score).toEqual([]);
  });

  it('should compute group ranking to show preferring finishedGroup', () => {
    const selected = buildGroup(1, 0, []);
    const finished = buildGroup(2, 0, []);
    component.selectedGroup = selected;
    component.finishedGroup = finished;

    expect(component.groupRankingToShow()).toBe(finished);

    component.finishedGroup = null;
    expect(component.groupRankingToShow()).toBe(selected);
  });

  it('should find group for selected duel in fights and unties', () => {
    const duelInFight = buildDuel(10);
    const untie = buildDuel(20);
    const group = buildGroup(1, 0, [buildFight(1, [duelInFight])], [untie]);
    component.groups = [group];

    expect(component.getGroup(duelInFight)).toBe(group);
    expect(component.getGroup(untie)).toBe(group);
    expect(component.getGroup(undefined)).toBeNull();
  });

  it('should report all duels over only when all fights and unties are finished', () => {
    component.groups = [
      buildGroup(1, 0, [buildFight(1, [buildDuel(1, true)])], [buildDuel(2, true)])
    ];
    expect(component.areAllDuelsOver()).toBeTrue();

    component.groups = [
      buildGroup(1, 0, [buildFight(1, [buildDuel(1, false)])], [])
    ];
    expect(component.areAllDuelsOver()).toBeFalse();
  });

  it('should select first unfinished untie before fight duels', () => {
    const untie = buildDuel(5, false, false);
    const fight = buildFight(1, [buildDuel(6, false, false)]);
    component.groups = [buildGroup(1, 0, [fight], [untie])];
    spyOn(component, 'resetFilter');
    spyOn(component, 'selectDuel').and.callThrough();
    spyOn(component, 'selectFight').and.callThrough();

    const found = component.selectFirstUnfinishedDuel();

    expect(found).toBeTrue();
    expect(component.selectDuel).toHaveBeenCalledWith(untie);
    expect(component.selectFight).not.toHaveBeenCalledWith(fight);
  });

  it('should select first unfinished fight duel when there are no valid unties', () => {
    const fightDuel = buildDuel(7, false, false);
    const fight = buildFight(2, [fightDuel]);
    component.groups = [buildGroup(1, 0, [fight], [buildDuel(8, true, false)])];
    spyOn(component, 'resetFilter');

    const found = component.selectFirstUnfinishedDuel();

    expect(found).toBeTrue();
    expect(component.selectedFight).toBe(fight);
    expect(component.selectedDuel).toBe(fightDuel);
  });

  it('should update selected duel and time notifications when selecting duel', () => {
    const duel = buildDuel(1);
    duel.duration = 12;
    duel.totalDuration = 90;

    component.selectDuel(duel);

    expect(component.selectedDuel).toBe(duel);
    expect(duelChangedServiceMock.isDuelUpdated.value).toBe(duel);
    expect(timeChangedServiceMock.isElapsedTimeChanged.value).toBe(12);
    expect(timeChangedServiceMock.isTotalTimeChanged.value).toBe(90);
  });

  it('should ignore substitute duels on selectDuel', () => {
    const duel = buildDuel(1, false, true);

    component.selectDuel(duel);

    expect(component.selectedDuel).toBeUndefined();
  });

  it('should toggle swapped colors and persist them', () => {
    component.swapColors();
    expect(component.swappedColors).toBeTrue();
    expect(userSessionServiceSpy.setSwappedColors).toHaveBeenCalledWith(true);
  });

  it('should toggle swapped teams and persist them', () => {
    component.swapTeams();
    expect(component.swappedTeams).toBeTrue();
    expect(userSessionServiceSpy.setSwappedTeams).toHaveBeenCalledWith(true);
  });

  it('should enable member order and notify service', () => {
    component.enableMemberOrder(true);

    expect(component.membersOrder).toBeTrue();
    expect(membersOrderChangedServiceMock.membersOrderAllowed.value).toBeTrue();
  });

  it('should filter fights by normalized member and team names', () => {
    const matchingFight = buildFight(1, [buildDuel(1)], 0, 2);
    const nonMatchingFight = {
      ...buildFight(2, [buildDuel(2)], 1, 1),
      team1: buildTeam(1, 'Tigres', [buildParticipant(3, 'Mario', 'Rossi', 'Roma Club')]),
      team2: buildTeam(2, 'Lobos', [buildParticipant(4, 'Paolo', 'Verdi', 'Milano Club')])
    } as Fight;
    const matchingUntie = {
      ...buildDuel(3),
      competitor1: buildParticipant(5, 'Ángel', 'Núñez', 'Kyoto Club'),
      competitor2: buildParticipant(6, 'Sora', 'Tanaka', 'Osaka Club')
    } as Duel;

    component.groups = [
      buildGroup(1, 0, [matchingFight, nonMatchingFight], [matchingUntie], 0)
    ];

    component.filter('angel');

    expect(component.filteredFights.get(1)?.length).toBe(0);
    expect(component.filteredUnties.get(1)?.length).toBe(1);

    component.filter('aguila');
    expect(component.filteredFights.get(1)?.length).toBe(1);
    expect(component.filteredLevels).toContain(2);
  });

  it('should respect selected shiaijo and hideFinishedFights during filtering', () => {
    const finishedFight = buildFight(1, [buildDuel(1, true)], 0, 0, true);
    const openFight = buildFight(2, [buildDuel(2, false)], 1, 1);
    component.groups = [
      buildGroup(1, 0, [finishedFight], [], 0),
      buildGroup(2, 1, [openFight], [], 1)
    ];
    component.selectedShiaijo = 1;
    component.hideFinishedFights = true;

    component.filter('equipo');

    expect(component.filteredFights.get(1)?.length).toBe(0);
    expect(component.filteredFights.get(2)?.length).toBe(1);
  });

  it('should reset filter and emit reset signal', () => {
    spyOn(component, 'filter');
    spyOn(component.resetFilterValue, 'next');

    component.resetFilter();

    expect(component.filter).toHaveBeenCalledWith('');
    expect(component.resetFilterValue.next).toHaveBeenCalledWith(true);
  });

  it('should provide shiaijo tag based on selected index', () => {
    component.selectedShiaijo = -1;
    expect(component.getShiaijoTag()).toBe('-');

    component.selectedShiaijo = 1;
    expect(component.getShiaijoTag()).toBe(Tournament.SHIAIJO_NAMES[1]);
  });

  it('should change project mode, hide finished fights and emit update', () => {
    spyOn(component, 'resetFilter');

    component.changeProjectMode(true);

    expect(component.projectorMode).toBeTrue();
    expect(component.hideFinishedFights).toBeTrue();
    expect(component.resetFilter).toHaveBeenCalled();
    expect(projectModeChangedServiceMock.isProjectMode.value).toBeTrue();
  });

  it('should finish tournament when date is provided and reopen when date is undefined', () => {
    component.tournament = buildTournament();
    const finishDate = new Date('2024-06-01');

    component.finishTournament(finishDate);
    expect(component.tournament.finishedAt).toEqual(finishDate);
    expect(tournamentServiceSpy.update).toHaveBeenCalledTimes(1);

    component.finishTournament(undefined);
    expect(component.tournament.finishedAt).toBeUndefined();
    expect(tournamentServiceSpy.update).toHaveBeenCalledTimes(2);
  });

  it('should choose teams or competitors classification according to tournament setup', () => {
    spyOn(component, 'showTeamsClassification');
    spyOn(component, 'showCompetitorsClassification');

    component.tournament = buildTournament();
    component.showClassification();
    expect(component.showTeamsClassification).toHaveBeenCalledWith(true);

    component.tournament.teamSize = 1;
    component.tournament.type = TournamentType.CUSTOMIZED;
    component.showClassification();
    expect(component.showCompetitorsClassification).toHaveBeenCalled();
  });
});

