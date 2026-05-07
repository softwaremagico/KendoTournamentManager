import { Group } from '../../../../models/group';
import { Tournament } from '../../../../models/tournament';
import { BracketsMeasures } from '../brackets-measures';
import { ShiaijoComponent } from './shiaijo.component';

describe('ShiaijoComponent', () => {
  let component: ShiaijoComponent;

  const createGroup = (shiaijo: number): Group => ({
    shiaijo
  } as unknown as Group);

  beforeEach(() => {
    component = new ShiaijoComponent();
    component.groupsByLevel = new Map<number, Group[]>();
    component.level = 0;
    component.shiaijo = 0;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should generate default coordinates in constructor', () => {
    expect(component.x1).toBe(0);
    expect(component.x2).toBe(BracketsMeasures.GROUP_WIDTH);
    expect(component.y1).toBe(BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_PADDING);
    expect(component.y2).toBe(BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_PADDING);
    expect(component.height).toBe(ShiaijoComponent.SHIAIJO_HEIGHT);
    expect(component.width).toBe(BracketsMeasures.GROUP_WIDTH);
  });

  it('should return groups filtered by shiaijo and level', () => {
    component.groupsByLevel = new Map<number, Group[]>([
      [0, [createGroup(0), createGroup(1), createGroup(0)]]
    ]);

    const result = component.getGroupsInShiaijo(0, 0);

    expect(result.length).toBe(2);
    expect(result.every((group) => group.shiaijo === 0)).toBeTrue();
  });

  it('should return empty array when level does not exist in groupsByLevel', () => {
    component.groupsByLevel = new Map<number, Group[]>([[1, [createGroup(0)]]]);

    expect(component.getGroupsInShiaijo(0, 0)).toEqual([]);
  });

  it('should return previous shiaijo name based on current shiaijo index', () => {
    component.shiaijo = 0;

    expect(component.getPreviousShiaijoName()).toBe(Tournament.SHIAIJO_NAMES[0]);
  });

  it('should return next shiaijo name based on current shiaijo index', () => {
    component.shiaijo = 0;

    expect(component.getNextShiaijoName()).toBe(Tournament.SHIAIJO_NAMES[1]);
  });
});

