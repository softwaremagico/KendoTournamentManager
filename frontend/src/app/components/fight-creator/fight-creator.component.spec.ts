import { of, Subject } from 'rxjs';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { FightCreator } from './fight-creator.component';
import { TeamService } from '../../services/team.service';
import { FightService } from '../../services/fight.service';
import { GroupService } from '../../services/group.service';
import { MessageService } from '../../services/message.service';
import { GroupUpdatedService } from '../../services/notifications/group-updated.service';
import { Team } from '../../models/team';
import { Fight } from '../../models/fight';
import { Group } from '../../models/group';

describe('FightCreator', () => {
  let component: FightCreator;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let groupUpdatedServiceMock: GroupUpdatedService;

  const createTeam = (name: string): Team => ({ name, members: [] } as unknown as Team);

  const createFight = (id: number, team1?: Team, team2?: Team): Fight => ({
    id,
    team1,
    team2,
    duels: []
  } as unknown as Fight);

  beforeEach(() => {
    teamServiceSpy = jasmine.createSpyObj('TeamService', ['getRemainingFromTournament']);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['generateDuels']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    groupUpdatedServiceMock = {
      isGroupUpdated: new Subject<Group>()
    } as GroupUpdatedService;

    component = new FightCreator(
      teamServiceSpy,
      fightServiceSpy,
      groupServiceSpy,
      messageServiceSpy,
      groupUpdatedServiceMock
    );

    component.tournament = { id: 1, name: 'T1' } as any;
    component.fight = createFight(1);
    component.group = {
      id: 10,
      fights: []
    } as unknown as Group;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load and sort teams by name on getTeams', () => {
    const zeta = createTeam('Zeta');
    const alpha = createTeam('Alpha');
    teamServiceSpy.getRemainingFromTournament.and.returnValue(of([zeta, alpha]));

    component.getTeams();

    expect(teamServiceSpy.getRemainingFromTournament).toHaveBeenCalledOnceWith(component.tournament);
    expect(component.teamListData.teams.map(t => t.name)).toEqual(['Alpha', 'Zeta']);
    expect(component.teamListData.filteredTeams.map(t => t.name)).toEqual(['Alpha', 'Zeta']);
  });

  it('should call getTeams on ngOnInit', () => {
    spyOn(component, 'getTeams');

    component.ngOnInit();

    expect(component.getTeams).toHaveBeenCalled();
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.onClosed, 'emit');

    component.closeDialog();

    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should transfer team and keep lists sorted on dropTeam', () => {
    const alpha = createTeam('Alpha');
    const beta = createTeam('Beta');

    component.teamListData.teams = [beta, alpha];
    component.teamListData.filteredTeams = [beta, alpha];

    const event = {
      previousContainer: { data: [alpha] },
      container: { data: [] },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Team[], any>;

    const dropped = component.dropTeam(event);

    expect(dropped).toEqual(alpha);
    expect(component.teamListData.teams.map(t => t.name)).toEqual(['Alpha', 'Beta']);
    expect(component.teamListData.filteredTeams.map(t => t.name)).toEqual(['Alpha', 'Beta']);
  });

  it('should allow dropped element when destination list is empty', () => {
    const item = { data: createTeam('A') } as any;
    const drop = { data: [] } as any;

    expect(component.checkDroppedElement(item, drop)).toBeTrue();
  });

  it('should allow dropped element when destination has same element', () => {
    const team = createTeam('A');
    const item = { data: team } as any;
    const drop = { data: [team] } as any;

    expect(component.checkDroppedElement(item, drop)).toBeTrue();
  });

  it('should not allow dropped element when destination has different element', () => {
    const item = { data: createTeam('A') } as any;
    const drop = { data: [createTeam('B')] } as any;

    expect(component.checkDroppedElement(item, drop)).toBeFalse();
  });

  it('should add fight after previousFight and notify services in addFights', () => {
    const team1 = createTeam('A');
    const team2 = createTeam('B');
    const existing = createFight(20);
    const previous = createFight(30);
    const generated = createFight(40, team1, team2);
    const updatedGroup = { id: 10, fights: [existing, previous, generated] } as Group;

    component.selectedTeam1 = [team1];
    component.selectedTeam2 = [team2];
    component.group.fights = [existing, previous];
    component.previousFight = previous;

    fightServiceSpy.generateDuels.and.returnValue(of(generated));
    groupServiceSpy.update.and.returnValue(of(updatedGroup));
    const nextSpy = spyOn(groupUpdatedServiceMock.isGroupUpdated, 'next');
    spyOn(component, 'closeDialog');

    component.addFights();

    expect(fightServiceSpy.generateDuels).toHaveBeenCalled();
    expect(component.group.fights[2]).toEqual(generated);
    expect(groupServiceSpy.update).toHaveBeenCalledOnceWith(component.group);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledOnceWith('addFightMessage');
    expect(nextSpy).toHaveBeenCalledOnceWith(updatedGroup);
    expect(component.closeDialog).toHaveBeenCalled();
  });

  it('should push fight when there is no previousFight', () => {
    const generated = createFight(50);

    component.selectedTeam1 = [createTeam('A')];
    component.selectedTeam2 = [createTeam('B')];
    component.previousFight = undefined;
    component.group.fights = [];

    fightServiceSpy.generateDuels.and.returnValue(of(generated));
    groupServiceSpy.update.and.returnValue(of(component.group));

    component.addFights();

    expect(component.group.fights).toEqual([generated]);
  });
});


