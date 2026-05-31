import {Subject} from 'rxjs';
import {Group} from '../../../../models/group';
import {GroupsUpdatedService} from '../groups-updated.service';
import {BracketsMeasures} from '../brackets-measures';
import {ArrowsComponent} from './arrows.component';

describe('ArrowsComponent', () => {
  let component: ArrowsComponent;
  let areGroupsUpdatedSubject: Subject<Group[]>;
  let areTotalTeamsUpdatedSubject: Subject<number>;
  let areRelationsUpdatedSubject: Subject<Map<number, any[]>>;
  const noopFn = () => 0;
  beforeEach(() => {
    areGroupsUpdatedSubject = new Subject<Group[]>();
    areTotalTeamsUpdatedSubject = new Subject<number>();
    areRelationsUpdatedSubject = new Subject<Map<number, any[]>>();
    const groupsUpdatedServiceMock: Partial<GroupsUpdatedService> = {
      areGroupsUpdated: areGroupsUpdatedSubject as any,
      areTotalTeamsNumberUpdated: areTotalTeamsUpdatedSubject as any,
      areRelationsUpdated: areRelationsUpdatedSubject as any
    };
    component = new ArrowsComponent(groupsUpdatedServiceMock as GroupsUpdatedService);
    component.level = 0;
    component.numberOfWinnersFirstLevel = 1;
    component.groupsByLevel = new Map([[0, [{ id: 1, level: 0, index: 0, teams: [], shiaijo: 0 } as unknown as Group]]]);
    component.getGroupTopSeparation = noopFn as any;
    component.getGroupHigh = noopFn as any;
    component.getGroupYCoordinate = noopFn as any;
  });
  it('should create the component', () => {
    expect(component).toBeTruthy();
  });
  it('should default arrow_size to ArrowsComponent.ARROW_SIZE', () => {
    expect(component.arrow_size).toBe(ArrowsComponent.ARROW_SIZE);
  });
  it('should update totalTeams on areTotalTeamsNumberUpdated emission', () => {
    component.ngOnInit();
    areTotalTeamsUpdatedSubject.next(12);
    expect(component.totalTeams).toBe(12);
  });
  it('should not update totalTeams when the same value is emitted', () => {
    component.totalTeams = 8;
    component.ngOnInit();
    areTotalTeamsUpdatedSubject.next(8);
    expect(component.totalTeams).toBe(8);
  });
  it('should update relations and call generateCoordinates on areRelationsUpdated', () => {
    spyOn(component, 'generateCoordinates');
    component.ngOnInit();
    const relations = new Map([[0, [{ src: 0, dest: 0, winner: 0 }]]]);
    areRelationsUpdatedSubject.next(relations);
    expect(component.relations).toEqual([{ src: 0, dest: 0, winner: 0 }] as any);
    expect(component.generateCoordinates).toHaveBeenCalled();
  });
  it('should return 0 from getArrowX1Coordinate', () => {
    expect(component.getArrowX1Coordinate(0, 0)).toBe(0);
  });
  it('should return levelSeparation from getArrowX2Coordinate', () => {
    expect(component.getArrowX2Coordinate(1, 0)).toBe(BracketsMeasures.levelSeparation(1));
  });
  it('should return empty array from getInboundArrowsSrc when no relations', () => {
    component.relations = undefined;
    expect(component.getInboundArrowsSrc(0)).toEqual([]);
  });
  it('should return matching src arrows from getInboundArrowsSrc', () => {
    component.relations = [
      { src: 0, dest: 1, winner: 0 },
      { src: 2, dest: 1, winner: 0 },
      { src: 3, dest: 0, winner: 0 }
    ];
    expect(component.getInboundArrowsSrc(1)).toEqual([0, 2]);
  });
  it('should return empty array from getOutboundArrowsDest when no relations', () => {
    component.relations = undefined;
    expect(component.getOutboundArrowsDest(0)).toEqual([]);
  });
  it('should return matching dest arrows from getOutboundArrowsDest', () => {
    component.relations = [
      { src: 0, dest: 1, winner: 0 },
      { src: 0, dest: 2, winner: 0 },
      { src: 1, dest: 3, winner: 0 }
    ];
    expect(component.getOutboundArrowsDest(0)).toEqual([1, 2]);
  });
  it('should return 0 for getTotalWidth when level is the last level', () => {
    component.level = 0;
    expect(component.getTotalWidth()).toBe(0);
  });
  it('should produce coordinates with primary color when winner is 0', () => {
    const grp = { shiaijo: 0 } as unknown as Group;
    component.groupsByLevel = new Map([[0, [grp]], [1, [grp]]]);
    component.relations = [{ src: 0, dest: 0, winner: 0 }];
    component.generateCoordinates();
    expect(component.coordinates.length).toBe(1);
    expect(component.coordinates[0].color).toBe('var(--component-color)');
  });
  it('should use secondary color when winner is non-zero', () => {
    const grp = { shiaijo: 0 } as unknown as Group;
    component.groupsByLevel = new Map([[0, [grp]], [1, [grp]]]);
    component.relations = [{ src: 0, dest: 0, winner: 1 }];
    component.generateCoordinates();
    expect(component.coordinates[0].color).toBe('var(--secondary-team-arrow)');
  });
  it('should return 0 shiaijos crossed when groups share the same shiaijo', () => {
    const g0 = { shiaijo: 0 } as unknown as Group;
    const g1 = { shiaijo: 0 } as unknown as Group;
    component.groupsByLevel = new Map([[0, [g0, g1]]]);
    expect(component.getNumberOfShiaijosCrossed(0, 1)).toBe(0);
  });
  it('should return 1 shiaijo crossed when adjacent groups have different shiaijos', () => {
    const g0 = { shiaijo: 0 } as unknown as Group;
    const g1 = { shiaijo: 1 } as unknown as Group;
    component.groupsByLevel = new Map([[0, [g0, g1]]]);
    expect(component.getNumberOfShiaijosCrossed(0, 1)).toBe(1);
  });
});
