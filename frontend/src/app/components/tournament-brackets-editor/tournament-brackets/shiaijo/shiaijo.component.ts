import {Component, Input} from '@angular/core';
import {Group} from "../../../../models/group";
import {TournamentBracketsComponent} from "../tournament-brackets.component";
import {GroupsUpdatedService} from "../groups-updated.service";

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
  groups: Group[];

  @Input()
  totalShiaijos: number | undefined;

  @Input()
  shiajo: number;

  @Input()
  color: string = "#001239";


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
    this.groupsUpdatedService.areTotalTeamsNumberUpdated.subscribe((_totalTeams: number): void => {
      this.totalTeams = _totalTeams;
      this.generateCoordinates();
    })
  }

  generateCoordinates(): void {
    const lastGroupIndex: number = this.getLastGroupIndex(this.shiajo);

    this.x1 = 0;
    this.y1 = this.getYCoordinate(lastGroupIndex);
    this.x2 = 500;
    this.y2 = this.getYCoordinate(lastGroupIndex);
    this.height = Math.max(this.y2, this.y1);
    this.width = Math.max(this.x2, this.x1);

    console.log(this.groups);
    console.log(this.shiajo, '***REMOVED***>', lastGroupIndex, ': ', this.getGroupHigh(0, lastGroupIndex + 1));
  }

  getYCoordinate(groupIndex: number): number {
    return (this.getGroupTopSeparation(0, groupIndex + 1, null)
        -  (TournamentBracketsComponent.GROUP_SEPARATION / 2));
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
}
