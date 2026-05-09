import {of, Subject} from 'rxjs';
import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {SenbatsuFightCreatorComponent} from './senbatsu-fight-creator.component';
import {TeamService} from '../../services/team.service';
import {FightService} from '../../services/fight.service';
import {GroupService} from '../../services/group.service';
import {MessageService} from '../../services/message.service';
import {GroupUpdatedService} from '../../services/notifications/group-updated.service';
import {TournamentExtendedPropertiesService} from '../../services/tournament-extended-properties.service';
import {Team} from '../../models/team';

describe('SenbatsuFightCreatorComponent', () => {
  let component: SenbatsuFightCreatorComponent;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let groupUpdatedServiceMock: GroupUpdatedService;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;

  const createTeam = (name: string): Team => ({
    name,
    members: []
  } as unknown as Team);

  beforeEach(() => {
    teamServiceSpy = jasmine.createSpyObj('TeamService', ['getRemainingFromTournament']);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['generateDuels']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    groupUpdatedServiceMock = {
      isGroupUpdated: new Subject()
    } as GroupUpdatedService;
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj('TournamentExtendedPropertiesService', ['getByTournamentAndKey']);

    component = new SenbatsuFightCreatorComponent(
      teamServiceSpy,
      fightServiceSpy,
      groupServiceSpy,
      messageServiceSpy,
      groupUpdatedServiceMock,
      tournamentExtendedPropertiesServiceSpy
    );

    component.tournament = { id: 1, name: 'Senbatsu' } as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should select first team as challenger and set disabled teams by challenge distance', () => {
    const challenger = createTeam('A');
    const b = createTeam('B');
    const c = createTeam('C');
    const d = createTeam('D');

    teamServiceSpy.getRemainingFromTournament.and.returnValue(of([challenger, b, c, d]));
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(
      of({ propertyValue: '2' } as any)
    );

    component.getTeams();

    expect(component.selectedTeam1).toEqual([challenger]);
    expect(component.teamListData.teams).toEqual([b, c, d]);
    expect(component.teamListData.filteredTeams).toEqual([b, c, d]);
    expect(component.teamDragDisabled).toEqual([d]);
  });

  it('should keep selectedTeam1 empty when there are no teams', () => {
    teamServiceSpy.getRemainingFromTournament.and.returnValue(of([]));
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(
      of({ propertyValue: '1' } as any)
    );

    component.getTeams();

    expect(component.selectedTeam1).toEqual([]);
    expect(component.teamListData.teams).toEqual([]);
    expect(component.teamDragDisabled).toEqual([]);
  });

  it('should reorder team lists using original order after dropTeam', () => {
    const b = createTeam('B');
    const c = createTeam('C');
    const d = createTeam('D');

    teamServiceSpy.getRemainingFromTournament.and.returnValue(of([createTeam('A'), b, c, d]));
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(
      of({ propertyValue: '2' } as any)
    );

    component.getTeams();

    component.teamListData.teams = [d, b, c];
    component.teamListData.filteredTeams = [d, b, c];

    const event = {
      previousContainer: { data: [b] },
      container: { data: [] },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Team[], any>;

    const dropped = component.dropTeam(event);

    expect(dropped).toEqual(b);
    expect(component.teamListData.teams).toEqual([b, c, d]);
    expect(component.teamListData.filteredTeams).toEqual([b, c, d]);
  });
});

