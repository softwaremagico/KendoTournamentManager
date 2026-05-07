import { Subject } from 'rxjs';
import { Group } from '../../../models/group';
import { Team } from '../../../models/team';
import { GroupsUpdatedService } from './groups-updated.service';
import { TournamentBracketsComponent } from './tournament-brackets.component';

describe('TournamentBracketsComponent', () => {
  let component: TournamentBracketsComponent;
  let groupsUpdatedServiceMock: GroupsUpdatedService;
  let areGroupsUpdatedSubject: Subject<Group[]>;
  let areRelationsUpdatedSubject: Subject<Map<number, { src: number; dest: number; winner: number }[]>>;

  const createTeam = (id: number, name: string): Team => ({
    id,
    name,
    members: []
  } as unknown as Team);

  const createGroup = (id: number, level: number, index: number, shiaijo: number, teams: Team[] = []): Group => ({
    id,
    level,
    index,
    shiaijo,
    teams,
    fights: []
  } as unknown as Group);

  beforeEach(() => {
    areGroupsUpdatedSubject = new Subject<Group[]>();
    areRelationsUpdatedSubject = new Subject<Map<number, { src: number; dest: number; winner: number }[]>>();

    groupsUpdatedServiceMock = {
      areGroupsUpdated: areGroupsUpdatedSubject,
      areRelationsUpdated: areRelationsUpdatedSubject
    } as GroupsUpdatedService;

    component = new TournamentBracketsComponent(groupsUpdatedServiceMock);
    component.tournament = { id: 1, name: 'T1', shiaijos: 4 } as any;
    component.totalTeams = 8;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should convert groups into a map grouped by level', () => {
    const groups = [
      createGroup(1, 0, 0, 0),
      createGroup(2, 1, 0, 0),
      createGroup(3, 0, 1, 1)
    ];

    const grouped = TournamentBracketsComponent.convert(groups);

    expect(grouped.get(0)?.length).toBe(2);
    expect(grouped.get(1)?.length).toBe(1);
  });

  it('should return empty map from convert when groups are undefined', () => {
    const grouped = TournamentBracketsComponent.convert(undefined as unknown as Group[]);

    expect(grouped.size).toBe(0);
  });

  it('should update groupsByLevel and shiaijosByLevel when groups are emitted', () => {
    component.ngOnInit();
    const groups = [
      createGroup(1, 0, 0, 0, [createTeam(1, 'A')]),
      createGroup(2, 0, 1, 1, [createTeam(2, 'B')]),
      createGroup(3, 1, 0, 0, [createTeam(3, 'C')])
    ];

    areGroupsUpdatedSubject.next(groups);

    expect(component.groupsByLevel.get(0)?.length).toBe(2);
    expect(component.groupsByLevel.get(1)?.length).toBe(1);
    expect(component.shiaijosByLevel.get(0)).toEqual([0]);
  });

  it('should calculate numberOfWinnersFirstLevel from relation winners', () => {
    component.ngOnInit();

    const relations = new Map<number, { src: number; dest: number; winner: number }[]>();
    relations.set(0, [
      { src: 0, dest: 0, winner: 0 },
      { src: 1, dest: 0, winner: 2 }
    ]);

    areRelationsUpdatedSubject.next(relations);

    expect(component.numberOfWinnersFirstLevel).toBe(3);
    expect(component.relations).toBe(relations);
  });

  it('should return empty shiaijos list when level has one group', () => {
    component.groupsByLevel = new Map<number, Group[]>([
      [0, [createGroup(1, 0, 0, 0)]]
    ]);

    const shiaijos = component.getShiaijos();

    expect(shiaijos.get(0)).toEqual([]);
  });

  it('should return groups filtered by shiaijo and level', () => {
    const g1 = createGroup(1, 0, 0, 0);
    const g2 = createGroup(2, 0, 1, 1);
    const g3 = createGroup(3, 0, 2, 0);

    component.groupsByLevel = new Map<number, Group[]>([
      [0, [g1, g2, g3]]
    ]);

    const groupsInShiaijo0 = component.getGroupsInShiaijo(0, 0);

    expect(groupsInShiaijo0).toEqual([g1, g3]);
  });

  it('should emit selected group through elementClicked on isSelected', () => {
    const group = createGroup(1, 0, 0, 0);
    spyOn(component.elementClicked, 'emit');

    component.isSelected(group);

    expect(component.elementClicked.emit).toHaveBeenCalledOnceWith(group);
  });

  it('should return empty array in getGroupsInShiaijo when level does not exist', () => {
    component.groupsByLevel = new Map<number, Group[]>();

    expect(component.getGroupsInShiaijo(0, 99)).toEqual([]);
  });
});

