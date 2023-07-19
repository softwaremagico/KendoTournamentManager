import {Component, ElementRef, Input, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Group} from "../../models/group";

@Component({
  selector: 'app-tournament-brackets',
  templateUrl: './tournament-brackets.component.html',
  styleUrls: ['./tournament-brackets.component.scss']
})
export class TournamentBracketsComponent implements OnInit {

  static readonly GROUP_HIGH: number = 100;
  static readonly GROUP_WIDTH: number = 200;
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


  groupsByLevel: Map<number, Group[]>;


  constructor() {
  }

  ngOnInit(): void {
      this.groupsByLevel = TournamentBracketsComponent.convert(this.groups);
  }

  // ngOnChanges(changes: SimpleChanges): void {
  //   if (changes['groups']) {
  //     this.groupsByLevel = this.convert(this.groups);
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

  getGroupTopSeparation(column: number, group: number): number {
    if (column == 0) {
      return group * (TournamentBracketsComponent.GROUP_SEPARATION + TournamentBracketsComponent.GROUP_HIGH);
    }
    if (this.groupsByLevel.get(column)) {
      const maxHeight: number = this.groupsByLevel.get(0)!.length * (TournamentBracketsComponent.GROUP_HIGH + TournamentBracketsComponent.GROUP_SEPARATION);
      const portion: number = (maxHeight / this.groupsByLevel.get(column)!.length);
      return (portion * (group + 1)) - portion / 2 - TournamentBracketsComponent.GROUP_HIGH / 2 - TournamentBracketsComponent.GROUP_SEPARATION / 2
    }
    return 0;
  }

  getGroupLeftSeparation(column: number, group: number): number {
    return (TournamentBracketsComponent.GROUP_WIDTH + TournamentBracketsComponent.LEVEL_SEPARATION) * column;
  }

  getArrowX1Coordinate(column: number, currentIndex: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * (column + 1) + TournamentBracketsComponent.LEVEL_SEPARATION * column;
  }

  getArrowY1Coordinate(column: number, currentIndex: number): number {
      return this.getGroupTopSeparation(column, currentIndex) + TournamentBracketsComponent.GROUP_HIGH / 2;
  }

  getArrowX2Coordinate(column: number, currentIndex: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * column + TournamentBracketsComponent.LEVEL_SEPARATION * column;
  }

  getArrowY2Coordinate(column: number, currentIndex: number): number {
    console.log(column, currentIndex)
      return this.getGroupTopSeparation(column, currentIndex) + TournamentBracketsComponent.GROUP_HIGH / 2;
  }

}
