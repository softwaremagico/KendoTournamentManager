import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {GroupsUpdatedService} from "../groups-updated.service";
import {TournamentBracketsComponent} from "../tournament-brackets.component";
import {Group} from "../../../../models/group";

@Component({
  selector: 'app-arrow',
  templateUrl: './arrow.component.html',
  styleUrls: ['./arrow.component.scss']
})
export class ArrowComponent implements OnInit {

  public static readonly ARROW_SIZE: number = 30;
  arrow_size: number = ArrowComponent.ARROW_SIZE;

  @Input()
  color: string = "#001239";

  @Input()
  getGroupTopSeparation: (level: number, group: number, groupsByLevel: Map<number, Group[]>) => number;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  level: number;

  @Input()
  groupSource: number;

  @Input()
  groupDestination: number;

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
    this.x1 = this.getArrowX1Coordinate(this.level, this.groupSource);
    this.y1 = this.getArrowY1Coordinate(this.level, this.groupSource);
    this.x2 = this.getArrowX2Coordinate(this.level + 1, this.groupDestination);
    this.y2 = this.getArrowY2Coordinate(this.level + 1, this.groupSource, this.groupDestination);
    this.height = Math.max(this.y2, this.y1) + ArrowComponent.ARROW_SIZE / 2;
    this.width = Math.max(this.x2, this.x1);
  }

  getArrowX1Coordinate(level: number, group: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * (level + 1) + TournamentBracketsComponent.LEVEL_SEPARATION * level + 5;
  }

  getArrowY1Coordinate(level: number, group: number): number {
    return this.getGroupTopSeparation(level, group, this.groupsByLevel) + this.getGroupHigh(level, group) / 2;
  }

  getArrowX2Coordinate(column: number, group: number): number {
    return TournamentBracketsComponent.GROUP_WIDTH * column + TournamentBracketsComponent.LEVEL_SEPARATION * column + 5;
  }

  getArrowY2Coordinate(column: number, sourceGroupIndex: number, destinationGroupIndex: number): number {
    let correction: number = 15;
    if (sourceGroupIndex % 2 === 0) {
      correction = -correction;
    }
    return this.getGroupTopSeparation(column, destinationGroupIndex, this.groupsByLevel) + this.getGroupHigh(column, sourceGroupIndex) / 2 + correction;
  }

}
