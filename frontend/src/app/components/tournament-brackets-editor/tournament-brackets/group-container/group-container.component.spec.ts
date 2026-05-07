import { of, Subject } from 'rxjs';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Team } from '../../../../models/team';
import { Group } from '../../../../models/group';
import { GroupService } from '../../../../services/group.service';
import { GroupsUpdatedService } from '../groups-updated.service';
import { GroupContainerComponent } from './group-container.component';

describe('GroupContainerComponent', () => {
  let component: GroupContainerComponent;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let groupsUpdatedServiceMock: GroupsUpdatedService;
  let totalTeamsSubject: Subject<number>;

  const createTeam = (id: number, name: string): Team => ({
    id,
    name,
    members: []
  } as unknown as Team);

  const createGroup = (id: number, level: number, index: number, teams: Team[] = []): Group => ({
    id,
    level,
    index,
    teams,
    fights: []
  } as unknown as Group);

  beforeEach(() => {
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['setTeamsToGroup']);
    totalTeamsSubject = new Subject<number>();
    groupsUpdatedServiceMock = {
      areTotalTeamsNumberUpdated: totalTeamsSubject
    } as GroupsUpdatedService;

    component = new GroupContainerComponent(groupServiceSpy, groupsUpdatedServiceMock);

    component.level = 0;
    component.index = 0;
    component.group = createGroup(1, 0, 0, [createTeam(1, 'A')]);
    component.groupsByLevel = new Map<number, Group[]>([
      [0, [component.group, createGroup(2, 0, 1, [createTeam(2, 'B')])]]
    ]);
    component.getGroupHigh = () => 140;
    component.getGroupTopSeparation = () => 0;
    component.getGroupLeftSeparation = () => 0;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should update totalTeams, groupHigh and estimatedTeams on total teams change', () => {
    component.ngOnInit();

    totalTeamsSubject.next(10);

    expect(component.totalTeams).toBe(10);
    expect(component.groupHigh).toBe(140);
    expect(component.estimatedTeams).toBe(5);
  });

  it('should move team on dropTeam and persist teams to service', () => {
    const srcTeam = createTeam(3, 'C');
    const destinationGroup = createGroup(1, 0, 0, []);

    component.groupsByLevel = new Map<number, Group[]>([
      [0, [destinationGroup]]
    ]);

    const persistedGroup = createGroup(1, 0, 0, [srcTeam]);
    groupServiceSpy.setTeamsToGroup.and.returnValue(of(persistedGroup));

    const event = {
      previousContainer: { data: [srcTeam] },
      container: { data: destinationGroup.teams },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Team[], any>;

    component.dropTeam(event);

    expect(groupServiceSpy.setTeamsToGroup).toHaveBeenCalledOnceWith(1, [srcTeam]);
    expect(component.groupsByLevel.get(0)?.[0]).toEqual(persistedGroup);
    expect(component.groupHigh).toBe(140);
  });

  it('should allow drop only when level is 0, not disabled and below limit', () => {
    const predicate = component.checkDroppedElement(0, 2, [createTeam(1, 'A')], false);

    expect(predicate({} as any, {} as any)).toBeTrue();
  });

  it('should deny drop when dropping is disabled', () => {
    const predicate = component.checkDroppedElement(0, 2, [createTeam(1, 'A')], true);

    expect(predicate({} as any, {} as any)).toBeFalse();
  });

  it('should deny drop when level is not zero', () => {
    const predicate = component.checkDroppedElement(1, 2, [createTeam(1, 'A')], false);

    expect(predicate({} as any, {} as any)).toBeFalse();
  });

  it('should deny drop when teams reached the limit', () => {
    const predicate = component.checkDroppedElement(0, 1, [createTeam(1, 'A')], false);

    expect(predicate({} as any, {} as any)).toBeFalse();
  });

  it('should return true from isLocked when level is not 0', () => {
    component.level = 1;

    expect(component.isLocked()).toBeTrue();
  });

  it('should emit selected group on isClicked', () => {
    spyOn(component.elementClicked, 'emit');

    component.isClicked();

    expect(component.elementClicked.emit).toHaveBeenCalledOnceWith(component.group);
  });
});


