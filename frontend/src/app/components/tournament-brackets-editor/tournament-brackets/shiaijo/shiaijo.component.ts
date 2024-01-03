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
    this.groups = this.getGroupsInShiaijo(this.shiaijo);
    this.groupsUpdatedService.areTotalTeamsNumberUpdated.subscribe((_totalTeams: number): void => {
      this.totalTeams = _totalTeams;
      this.generateCoordinates();
    })
    this.groupsUpdatedService.areGroupsUpdated.subscribe((_groups: Group[]): void => {
      this.groupsByLevel = TournamentBracketsComponent.convert(_groups);
      this.groups = this.getGroupsInShiaijo(this.shiaijo);
      this.generateCoordinates();
    });
  }

  generateCoordinates(): void {
    const lastGroupIndex: number = this.getLastGroupIndex(this.shiaijo);

    this.x1 = 0;
    this.y1 = this.getYCoordinate(lastGroupIndex);
    this.x2 = 500;
    this.y2 = this.getYCoordinate(lastGroupIndex);
    this.height = Math.max(this.y2, this.y1);
    this.width = Math.max(this.x2, this.x1);

    console.log(this.shiaijo, '***REMOVED***>', lastGroupIndex, ': ', this.getGroupHigh2(0, lastGroupIndex + 1));
  }

  getGroupHigh2(level: number, index: number): number {
    if (this.groupsByLevel && this.groupsByLevel!.get(level) && this.groupsByLevel.get(level)![index]) {
      const estimatedTeams: number = Math.ceil(this.totalTeams / this.groupsByLevel.get(0)!.length);
      let teams: number = -1;
      for (const i of this.groupsByLevel.get(level)!.keys()) {
        if (this.groupsByLevel.get(level)![i].teams.length > teams) {
          teams = this.groupsByLevel.get(level)![i].teams.length;
        }
      }

      if (level == 0 && Math.max(estimatedTeams, teams) > 1) {
        return Math.max(estimatedTeams, teams) * 60;
      } else if (teams && teams > 1) {
        return teams * 60;
      }
    }
    return BracketsMeasures.GROUP_HIGH;
  }

  getYCoordinate(groupIndex: number): number {
    return (this.getGroupTopSeparation(0, groupIndex + 1, null)
      - (BracketsMeasures.GROUP_SEPARATION / 2));
  }

  getLastGroupIndex(shiaijo: number): number {
    const maxGroup: Group | undefined = this.getLastGroup();
    if (maxGroup) {
      return maxGroup.index;
    }
    return 0;
  }

  getLastGroup(): Group | undefined {
    if (this.groups && this.groups.length > 0) {
      return this.groups.reduce((group1: Group, group2: Group): Group => group1.index > group2.index ? group1 : group2);
    }
    return undefined;
  }

  getGroupsInShiaijo(shiaijo: number): Group[] {
    if (this.groupsByLevel.get(0)) {
      return this.groupsByLevel.get(0)!.filter((_group: Group): boolean => _group.shiaijo == shiaijo);
    }
    return [];
  }

  getPreviousShiaijoName(): string {
    return Tournament.SHIAIJO_NAMES[this.shiaijo];
  }

  getNextShiaijoName(): string {
    return Tournament.SHIAIJO_NAMES[this.shiaijo+1];
  }
}
