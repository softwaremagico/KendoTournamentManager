import {Component, Input} from '@angular/core';
import {Group} from "../../../../models/group";
import {BracketsMeasures} from "../brackets-measures";
import {Tournament} from "../../../../models/tournament";

@Component({
  selector: 'shiaijo-separator',
  templateUrl: './shiaijo.component.html'
})
export class ShiaijoComponent {

  static readonly SHIAIJO_HEIGHT = BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_PADDING + BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_MARGIN;

  @Input()
  getGroupTopSeparation: (level: number, group: number, groupsByLevel: Map<number, Group[]> | null) => number;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  //Needed for calculating the groupHigh
  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  totalShiaijos: number | undefined;

  @Input()
  level: number;

  @Input()
  shiaijo: number;

  @Input()
  color: string = "#001239";

  groups: Group[];


  totalTeams: number;

  x1: number;
  y1: number;
  x2: number;
  y2: number;

  height: number;
  width: number;

  constructor() {
    this.generateCoordinates();
  }

  generateCoordinates(): void {
    this.x1 = 0;
    this.x2 = BracketsMeasures.GROUP_WIDTH;
    this.y1 = BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_PADDING;
    this.y2 = BracketsMeasures.SHIAIJO_FONT_ROOM + BracketsMeasures.SHIAIJO_PADDING;
    this.height = ShiaijoComponent.SHIAIJO_HEIGHT;
    this.width = this.x2;
  }

  getGroupsInShiaijo(shiaijo: number, level: number): Group[] {
    if (this.groupsByLevel.get(level)) {
      return this.groupsByLevel.get(level)!.filter((_group: Group): boolean => _group.shiaijo == shiaijo);
    }
    return [];
  }

  getPreviousShiaijoName(): string {
    return Tournament.SHIAIJO_NAMES[this.shiaijo];
  }

  getNextShiaijoName(): string {
    return Tournament.SHIAIJO_NAMES[this.shiaijo + 1];
  }
}
