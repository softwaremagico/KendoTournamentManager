import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Group} from "../../../models/group";
import {GroupsUpdatedService} from "./groups-updated.service";
import {Tournament} from "../../../models/tournament";
import {BracketsMeasures} from "./brackets-measures";

@Component({
  selector: 'tournament-brackets',
  templateUrl: './tournament-brackets.component.html',
  styleUrls: ['./tournament-brackets.component.scss']
})
export class TournamentBracketsComponent implements OnInit {

  @Input()
  tournament: Tournament;

  @Input()
  droppingDisabled: boolean;

  @Input()
  selectedGroup: Group;

  @Output()
  elementClicked: EventEmitter<Group> = new EventEmitter<Group>();

  numberOfWinnersFirstLevel: number;

  totalTeams: number;

  relations: Map<number, { src: number, dest: number, winner: number }[]>;

  shiaijosByLevel: Map<number, number[]> = new Map();

  groupsByLevel: Map<number, Group[]> = new Map();


  constructor(private groupsUpdatedService: GroupsUpdatedService) {
  }

  ngOnInit(): void {
    this.updateShiaijos();
    this.groupsUpdatedService.areRelationsUpdated.subscribe((_relations: Map<number, {
      src: number,
      dest: number,
      winner: number,
    }[]>): void => {
      this.relations = _relations;
      this.numberOfWinnersFirstLevel = 0;
      _relations.forEach((value: {
        src: number,
        dest: number,
        winner: number,
      }[], key: number): void => {
        value.forEach(value1 => {
          this.numberOfWinnersFirstLevel = Math.max(this.numberOfWinnersFirstLevel, value1.winner + 1);
        });
      });
    });
  }

  private updateShiaijos(): void {
    this.groupsUpdatedService.areGroupsUpdated.subscribe((_groups: Group[]): void => {
      this.groupsByLevel = TournamentBracketsComponent.convert(_groups);
      this.shiaijosByLevel = this.getShiaijos();
    });
  }

  public static convert(groups: Group[]): Map<number, Group[]> {
    const groupsByLevel: Map<number, Group[]> = new Map();
    if (groups) {
      for (const group of groups) {
        if (group.level !== undefined) {
          if (!groupsByLevel.get(group.level)) {
            groupsByLevel.set(group.level, []);
          }
          groupsByLevel.get(group.level)?.push(group);
        }
      }
    }
    return groupsByLevel;
  }

  getGroupHigh(level: number, index: number): number {
    if (this.groupsByLevel?.get(level) && this.groupsByLevel.get(level)![index]) {
      const estimatedTeams: number = Math.ceil(this.totalTeams / this.groupsByLevel.get(0)!.length);
      let teams: number = -1;
      for (const i of this.groupsByLevel.get(level)!.keys()) {
        if (this.groupsByLevel.get(level)![i].teams.length > teams) {
          teams = this.groupsByLevel.get(level)![i].teams.length;
        }
      }

      if (level == 0 && Math.max(estimatedTeams, teams) > 1) {
        return Math.max(estimatedTeams, teams) * BracketsMeasures.TEAM_GROUP_HIGH;
      } else if (teams && teams > 1) {
        return teams * BracketsMeasures.TEAM_GROUP_HIGH;
      }
    }
    return BracketsMeasures.GROUP_HIGH;
  }

  getGroupTopSeparation(level: number, group: number, groupsByLevel: Map<number, Group[]> | null): number {
    let maxGroupsByLevel: number = 0;
    let levelWithMaxGroups: number = 0;
    //Now group 0 maybe is the one with the highest number of groups. Search for which level is.
    if (groupsByLevel) {
      for (let key of groupsByLevel.keys()) {
        if (groupsByLevel.get(key)!.length > maxGroupsByLevel) {
          maxGroupsByLevel = groupsByLevel.get(key)!.length;
          levelWithMaxGroups = key;
        }
      }
    }
    if (level == levelWithMaxGroups) {
      return group * (BracketsMeasures.GROUP_SEPARATION + this.getGroupHigh(level, group));
    }
    if (groupsByLevel?.get(levelWithMaxGroups) && groupsByLevel?.get(level)) {
      const maxHeight: number = groupsByLevel.get(levelWithMaxGroups)!.length * (this.getGroupHigh(levelWithMaxGroups, group) + BracketsMeasures.GROUP_SEPARATION);
      const portion: number = (maxHeight / groupsByLevel.get(level)!.length);
      return (portion * (group + 1)) - portion / 2 - this.getGroupHigh(level, group) / 2 - BracketsMeasures.GROUP_SEPARATION / 2
    }
    return 0;
  }

  getGroupLeftSeparation(level: number, group: number): number {
    return (BracketsMeasures.GROUP_WIDTH + BracketsMeasures.LEVEL_SEPARATION) * level;
  }

  getArrowX1Coordinate(level: number, group: number): number {
    return BracketsMeasures.GROUP_WIDTH * (level + 1) + BracketsMeasures.LEVEL_SEPARATION * level + 5;
  }

  getArrowY1Coordinate(level: number, group: number): number {
    return this.getGroupTopSeparation(level, group, this.groupsByLevel) + this.getGroupHigh(level, group) / 2;
  }

  getArrowX2Coordinate(column: number, group: number): number {
    return BracketsMeasures.GROUP_WIDTH * column + BracketsMeasures.LEVEL_SEPARATION * column + 5;
  }

  getArrowY2Coordinate(column: number, sourceGroupIndex: number, destinationGroupIndex: number): number {
    let correction: number = 15;
    if (sourceGroupIndex % 2 === 0) {
      correction = -correction;
    }
    return this.getGroupTopSeparation(column, destinationGroupIndex, this.groupsByLevel) + this.getGroupHigh(column, sourceGroupIndex) / 2 + correction;
  }

  getShiaijos(): Map<number, number[]> {
    const shiaijosByLevel: Map<number, number[]> = new Map();
    for (let key of this.groupsByLevel.keys()) {
      shiaijosByLevel.set(key, []);
      if (!this.tournament.shiaijos || this.groupsByLevel.get(key)?.length! <= 1) {
        shiaijosByLevel.set(key, [...Array(0).keys()]);
      } else {
        shiaijosByLevel.set(key, [...Array(Math.min(this.tournament.shiaijos - 1, (this.groupsByLevel.get(key)?.length! - 1))).keys()]);
      }
    }
    return shiaijosByLevel;
  }

  getGroupsInShiaijo(shiaijo: number, level: number): Group[] {
    if (this.groupsByLevel.get(level)) {
      return this.groupsByLevel.get(level)!.filter((_group: Group): boolean => _group.shiaijo == shiaijo);
    }
    return [];
  }

  isSelected(group: Group) {
    this.elementClicked.emit(group);
  }
}
