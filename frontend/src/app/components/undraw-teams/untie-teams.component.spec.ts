import { of } from 'rxjs';
import { Duel } from '../../models/duel';
import { DuelType } from '../../models/duel-type';
import { Participant } from '../../models/participant';
import { Team } from '../../models/team';
import { Tournament } from '../../models/tournament';
import { GroupService } from '../../services/group.service';
import { MessageService } from '../../services/message.service';
import { UntieAddedService } from '../../services/notifications/untie-added.service';
import { RbacService } from '../../services/rbac/rbac.service';
import { UntieTeamsComponent } from './untie-teams.component';

describe('UntieTeamsComponent', () => {
  let component: UntieTeamsComponent;
  let untieAddedServiceMock: UntieAddedService;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  const createTournament = (): Tournament => ({
    id: 1,
    name: 'Test Tournament',
    duelsDuration: 180,
    teamSize: 3,
    fightSize: 3,
    locked: false
  } as unknown as Tournament);

  const createTeam = (name: string): Team => ({
    name,
    members: []
  } as unknown as Team);

  const createParticipant = (name: string): Participant => ({
    id: name.length,
    name,
    lastname: 'Test',
    idCard: `ID-${name}`,
    locked: false,
    hasAvatar: false
  } as unknown as Participant);

  beforeEach(() => {
    untieAddedServiceMock = {
      isDuelsAdded: {
        next: jasmine.createSpy('next')
      }
    } as unknown as UntieAddedService;
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['addUnties']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new UntieTeamsComponent(
      untieAddedServiceMock,
      groupServiceSpy,
      messageServiceSpy,
      rbacServiceSpy
    );

    component.tournament = createTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should return 0 total duels when teams length is less than 2', () => {
    component.teams = [createTeam('A')];

    expect(component.getTotalDuels()).toBe(0);
  });

  it('should return 1 total duel when teams length is 2', () => {
    component.teams = [createTeam('A'), createTeam('B')];

    expect(component.getTotalDuels()).toBe(1);
  });

  it('should return teams length total duels when teams length is greater than 2', () => {
    component.teams = [createTeam('A'), createTeam('B'), createTeam('C')];

    expect(component.getTotalDuels()).toBe(3);
  });

  it('should initialize duels on ngOnInit with UNDRAW type and tournament duration', () => {
    component.teams = [createTeam('A'), createTeam('B'), createTeam('C')];

    component.ngOnInit();

    expect(component.duels.length).toBe(3);
    expect(component.duels[0].type).toBe(DuelType.UNDRAW);
    expect(component.duels[0].totalDuration).toBe(180);
    expect(component.duels[0].tournament).toBe(component.tournament);
  });

  it('should update totalDuels when teams input changes', () => {
    component.teams = [createTeam('A'), createTeam('B')];

    component.ngOnChanges({
      teams: {
        currentValue: component.teams,
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false
      }
    });

    expect(component.totalDuels).toBe(1);
  });

  it('should return false from duelsCompleted when a duel has missing competitors', () => {
    const duel = new Duel();
    duel.competitor1 = createParticipant('One');
    component.duels = [duel];

    expect(component.duelsCompleted()).toBeFalse();
  });

  it('should return true from duelsCompleted when all duels have competitors', () => {
    const duel = new Duel();
    duel.competitor1 = createParticipant('One');
    duel.competitor2 = createParticipant('Two');
    component.duels = [duel];

    expect(component.duelsCompleted()).toBeTrue();
  });

  it('should set competitor1 and competitor2 in selected duel index', () => {
    component.duels = [new Duel()];
    const competitor1 = createParticipant('One');
    const competitor2 = createParticipant('Two');

    component.setCompetitor1(0, [competitor1]);
    component.setCompetitor2(0, [competitor2]);

    expect(component.duels[0].competitor1).toBe(competitor1);
    expect(component.duels[0].competitor2).toBe(competitor2);
  });

  it('should create fights, notify services and emit onClosed when groupId exists', () => {
    component.groupId = 10;
    component.duels = [new Duel()];
    groupServiceSpy.addUnties.and.returnValue(of({} as any));
    spyOn(component.onClosed, 'emit');

    component.createFights();

    expect(groupServiceSpy.addUnties).toHaveBeenCalledOnceWith(10, component.duels);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledOnceWith('addFight');
    expect((untieAddedServiceMock.isDuelsAdded.next as jasmine.Spy)).toHaveBeenCalledOnceWith(component.duels);
    expect(component.onClosed.emit).toHaveBeenCalledOnceWith(component.duels);
  });

  it('should not call addUnties when groupId is undefined', () => {
    component.groupId = undefined;

    component.createFights();

    expect(groupServiceSpy.addUnties).not.toHaveBeenCalled();
  });

  it('should emit empty array when closeDialog is called', () => {
    spyOn(component.onClosed, 'emit');

    component.closeDialog();

    expect(component.onClosed.emit).toHaveBeenCalledOnceWith([]);
  });
});

