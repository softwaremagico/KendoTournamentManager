import {Component, Input} from '@angular/core';
import {Group} from "../../../../models/group";
import {GroupsUpdatedService} from "../groups-updated.service";
import {BracketsMeasures} from "../brackets-measures";
import {TournamentBracketsComponent} from "../tournament-brackets.component";
import {Tournament} from "../../../../models/tournament";

@Component({
  selector: 'app-shiaijo',
  templateUrl: './shiaijo.component.html',
  styleUrls: ['./shiaijo.component.scss']
})
export class ShiaijoComponent {

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

  constructor(private groupsUpdatedService: GroupsUpdatedService) {
  }

  ngOnInit(): void {
    this.groups = this.getGroupsInShiaijo(this.shiaijo, this.level);
    this.groupsUpdatedService.areTotalTeamsNumberUpdated.subscribe((_totalTeams: number): void => {
      this.totalTeams = _totalTeams;
      this.generateCoordinates();
    })
    this.groupsUpdatedService.areGroupsUpdated.subscribe((_groups: Group[]): void => {
      this.groupsByLevel = TournamentBracketsComponent.convert(_groups);
      this.groups = this.getGroupsInShiaijo(this.shiaijo, this.level);
      this.generateCoordinates();
    });
  }

  generateCoordinates(): void {
    const lastGroupIndex: number = this.getLastGroupIndex(this.shiaijo);

    this.x1 = this.level * (BracketsMeasures.GROUP_WIDTH + BracketsMeasures.LEVEL_SEPARATION) + BracketsMeasures.SHIAIJO_PADDING;
    this.y1 = this.getYCoordinate(lastGroupIndex);
    this.x2 = this.level * (BracketsMeasures.GROUP_WIDTH + BracketsMeasures.LEVEL_SEPARATION) + BracketsMeasures.GROUP_WIDTH - BracketsMeasures.SHIAIJO_PADDING;
    this.y2 = this.getYCoordinate(lastGroupIndex);
    this.height = Math.max(this.y2, this.y1);
    this.width = Math.max(this.x2, this.x1);
  }

  getYCoordinate(groupIndex: number): number {
    const firstGroupSeparation: number = this.getGroupTopSeparation(this.level, groupIndex, this.groupsByLevel);
    const secondGroupSeparation: number = this.getGroupTopSeparation(this.level, groupIndex + 1, this.groupsByLevel);
    const groupHeight: number = this.getGroupHigh(this.level, groupIndex);
    return ((secondGroupSeparation - firstGroupSeparation - groupHeight) / 2) + firstGroupSeparation + groupHeight;
  }

  getLastGroupIndex(shiaijo: number): number {
    const maxGroup: Group | undefined | null = this.getLastGroup();
    if (maxGroup) {
      return maxGroup.index;
    }
    return 0;
  }

  getLastGroup(): Group | undefined | null {
    if (this.groups && this.groups.length > 0) {
      return this.groups.reduce((group1: Group, group2: Group): Group | null => group1.index > group2.index ? group1 : group2, null);
    }
    return undefined;
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
