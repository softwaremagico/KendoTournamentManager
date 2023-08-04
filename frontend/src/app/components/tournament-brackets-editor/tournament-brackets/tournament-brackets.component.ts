import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Group} from "../../../models/group";

@Component({
  selector: 'app-tournament-brackets',
  templateUrl: './tournament-brackets.component.html',
  styleUrls: ['./tournament-brackets.component.scss']
})
export class TournamentBracketsComponent implements OnInit {

  static readonly GROUP_HIGH: number = 100;
  static readonly GROUP_WIDTH: number = 300;
  static readonly GROUP_SEPARATION: number = 150;
  static readonly LEVEL_SEPARATION: number = 100;

  @Input()
  groups: Group[];

  @Input()
  relations: Map<number, { src: number, dest: number }[]>;

  // @ViewChildren('group', {read: ElementRef})
  // public dynComponents: QueryList<ElementRef>;

  @ViewChild('group-0') group0: ElementRef;
  @ViewChild('group-4') group4: ElementRef;
  @ViewChild('group-7') group7: ElementRef;


  groupsByLevel: Map<number, Group[]> = new Map();


  constructor() {
  }

  ngOnInit(): void {
    this.groupsByLevel = TournamentBracketsComponent.convert(this.groups);
  }

  // ngOnChanges(changes: SimpleChanges): void {
  //   if (changes['groups']) {
  //     this.groupsByLevel = TournamentBracketsComponent.convert(this.groups);
  //   }
  // }

  private static convert(groups: Group[]): Map<number, Group[]> {
    const groupsByLevel: Map<number, Group[]> = new Map();
    for (const group of groups) {
      if (group.level !== undefined) {
        if (!groupsByLevel.get(group.level)) {
          groupsByLevel.set(group.level, []);
        }
        groupsByLevel.get(group.level)?.push(group);
      }
    }
    return groupsByLevel;
  }

  getRowTopMargin(row: number): number {
    return (Math.pow(2, row) - 1) * 100;
  }

  getGroupSeparation(column: number): number {
    return ((column * 2 + 1) * TournamentBracketsComponent.GROUP_SEPARATION);
  }

  getGroupHigh(level: number, index: number): number {
    if (this.groupsByLevel && this.groupsByLevel!.get(level) && this.groupsByLevel.get(level)![index]) {
      const teams: number = this.groupsByLevel.get(level)![index].teams.length;
      console.log('***REMOVED***>', teams)
      if (teams && teams > 1) {
        return teams * 90;
      }
    }
    return TournamentBracketsComponent.GROUP_HIGH;
  }

  getGroupTopSeparation(level: number, group: number, groupsByLevel: Map<number, Group[]>): number {
    if (level == 0) {
      return group * (TournamentBracketsComponent.GROUP_SEPARATION + this.getGroupHigh(level, group));
    }
    if (groupsByLevel && groupsByLevel.get(level)) {
      const maxHeight: number = groupsByLevel.get(0)!.length * (this.getGroupHigh(level, group) + TournamentBracketsComponent.GROUP_SEPARATION);
      const portion: number = (maxHeight / groupsByLevel.get(level)!.length);
      return (portion * (group + 1)) - portion / 2 - this.getGroupHigh(level, group) / 2 - TournamentBracketsComponent.GROUP_SEPARATION / 2
    }
    return 0;
  }

  getGroupLeftSeparation(level: number, group: number): number {
    return (TournamentBracketsComponent.GROUP_WIDTH + TournamentBracketsComponent.LEVEL_SEPARATION) * level;
  }

  getArrowX1Coordinate(level: number, currentIndex: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * (level + 1) + TournamentBracketsComponent.LEVEL_SEPARATION * level + 5;
  }

  getArrowY1Coordinate(level: number, currentIndex: number): number {
    return this.getGroupTopSeparation(level, currentIndex, this.groupsByLevel) + this.getGroupHigh(level, currentIndex) / 2;
  }

  getArrowX2Coordinate(column: number, currentIndex: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * column + TournamentBracketsComponent.LEVEL_SEPARATION * column + 5;
  }

  getArrowY2Coordinate(column: number, sourceIndex: number, destinationIndex: number): number {
    let correction: number = 15;
    if (sourceIndex % 2 === 0) {
      correction = -correction;
    }
    return this.getGroupTopSeparation(column, destinationIndex, this.groupsByLevel) + this.getGroupHigh(column, sourceIndex) / 2 + correction;
  }

}
