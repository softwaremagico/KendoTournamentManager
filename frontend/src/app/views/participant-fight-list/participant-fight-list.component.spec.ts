import {of} from 'rxjs';
import {ParticipantFightListComponent} from './participant-fight-list.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {Router} from '@angular/router';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {FightService} from '../../services/fight.service';
import {DuelService} from '../../services/duel.service';
import {Tournament} from '../../models/tournament';
import {Fight} from '../../models/fight';
import {Team} from '../../models/team';
import {Participant} from '../../models/participant';
import {TournamentScore} from '../../models/tournament-score.model';

describe('ParticipantFightListComponent', () => {
  let component: ParticipantFightListComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let duelServiceSpy: jasmine.SpyObj<DuelService>;

  const buildTournament = (id: number): Tournament => {
    const t = new Tournament();
    t.id = id;
    t.name = 'Tournament ' + id;
    t.tournamentScore = new TournamentScore();
    t.createdAt = new Date('2024-01-0' + id);
    return t;
  };

  const tournament1 = buildTournament(1);
  const tournament2 = buildTournament(2);

  const buildParticipant = (id: number): Participant => ({
    id,
    name: 'Name' + id,
    lastname: 'Lastname' + id,
    idCard: 'ID' + id
  } as Participant);

  const buildTeam = (name: string, members: Participant[]): Team => ({
    id: 1,
    name,
    members
  } as Team);

  const buildFight = (tournament: Tournament, team1Name: string, team2Name: string): Fight => ({
    tournament,
    team1: buildTeam(team1Name, [buildParticipant(1)]),
    team2: buildTeam(team2Name, [buildParticipant(2)])
  } as Fight);

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'getCurrentNavigation']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isTransactionalBusy: {next: jasmine.createSpy('next')}
    });
    fightServiceSpy = jasmine.createSpyObj('FightService', ['getFromParticipant']);
    duelServiceSpy = jasmine.createSpyObj('DuelService', ['getUntiesFromParticipant']);

    routerSpy.getCurrentNavigation.and.returnValue({
      extras: {state: {participantId: 42}}
    } as any);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    fightServiceSpy.getFromParticipant.and.returnValue(of([]));
    duelServiceSpy.getUntiesFromParticipant.and.returnValue(of([]));

    component = new ParticipantFightListComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      fightServiceSpy,
      duelServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should read participantId from router state in constructor', () => {
    expect(component.participantId).toBe(42);
  });

  it('should navigate back to statistics when participantId is not a number', () => {
    routerSpy.getCurrentNavigation.and.returnValue({
      extras: {state: {participantId: 'invalid'}}
    } as any);

    component = new ParticipantFightListComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      fightServiceSpy,
      duelServiceSpy
    );

    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/participants/statistics'],
      {state: {participantId: undefined}}
    );
  });

  it('should initialize data when participantId is set in ngOnInit', () => {
    spyOn(component, 'initializeData');

    component.ngOnInit();

    expect(component.initializeData).toHaveBeenCalled();
  });

  it('should go back to statistics when participantId is undefined in ngOnInit', () => {
    routerSpy.getCurrentNavigation.and.returnValue(null);

    component = new ParticipantFightListComponent(
      routerSpy,
      rbacServiceSpy,
      systemOverloadServiceSpy,
      fightServiceSpy,
      duelServiceSpy
    );

    component.ngOnInit();

    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/participants/statistics'],
      {state: {participantId: undefined}}
    );
  });

  it('should load fights and group them by tournament in initializeData', () => {
    const fight1 = buildFight(tournament1, 'Team A', 'Team B');
    const fight2 = buildFight(tournament1, 'Team C', 'Team D');
    const fight3 = buildFight(tournament2, 'Team E', 'Team F');

    fightServiceSpy.getFromParticipant.and.returnValue(of([fight1, fight2, fight3]));
    duelServiceSpy.getUntiesFromParticipant.and.returnValue(of([]));

    component.initializeData();

    expect(component.competitorFights.size).toBe(2);
    expect(component.tournaments.length).toBe(2);
  });

  it('should sort tournaments by createdAt descending', () => {
    const fight1 = buildFight(tournament1, 'Team A', 'Team B');
    const fight2 = buildFight(tournament2, 'Team E', 'Team F');

    fightServiceSpy.getFromParticipant.and.returnValue(of([fight1, fight2]));
    duelServiceSpy.getUntiesFromParticipant.and.returnValue(of([]));

    component.initializeData();

    expect(component.tournaments[0].id).toBe(2);
    expect(component.tournaments[1].id).toBe(1);
  });

  it('should filter fights by team name', () => {
    const fight1 = buildFight(tournament1, 'Kendo Team Alpha', 'Kendo Team Beta');
    const fight2 = buildFight(tournament1, 'Other Team', 'Another Team');

    fightServiceSpy.getFromParticipant.and.returnValue(of([fight1, fight2]));
    duelServiceSpy.getUntiesFromParticipant.and.returnValue(of([]));

    component.initializeData();
    component.filter('kendo');

    const filtered = component.filteredFights.get(tournament1);
    expect(filtered?.length).toBe(1);
    expect(filtered?.[0].team1.name).toBe('Kendo Team Alpha');
  });

  it('should show all fights when filter is empty', () => {
    const fight1 = buildFight(tournament1, 'Team A', 'Team B');
    const fight2 = buildFight(tournament1, 'Team C', 'Team D');

    fightServiceSpy.getFromParticipant.and.returnValue(of([fight1, fight2]));
    duelServiceSpy.getUntiesFromParticipant.and.returnValue(of([]));

    component.initializeData();
    component.filter('');

    const filtered = component.filteredFights.get(tournament1);
    expect(filtered?.length).toBe(2);
  });

  it('should reset filter values and emit signal', () => {
    spyOn(component.resetFilterValue, 'next');

    component.resetFilter();

    expect(component.resetFilterValue.next).toHaveBeenCalledWith(true);
  });

  it('should navigate to statistics when goBackToStatistics is called', () => {
    component.goBackToStatistics();

    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/participants/statistics'],
      {state: {participantId: 42}}
    );
  });
});

