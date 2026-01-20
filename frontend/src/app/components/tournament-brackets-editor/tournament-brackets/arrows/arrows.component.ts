import {Component, Input, OnInit} from '@angular/core';
import {GroupsUpdatedService} from "../groups-updated.service";
import {Group} from "../../../../models/group";
import {BracketsMeasures} from "../brackets-measures";

@Component({
  selector: 'arrows',
  templateUrl: './arrows.component.html',
  styleUrls: ['./arrows.component.scss']
})
export class ArrowsComponent implements OnInit {

  public static readonly ARROW_SIZE: number = 30;
  public static readonly WINNER_SEPARATION: number = 15;
  arrow_size: number = ArrowsComponent.ARROW_SIZE;

  @Input()
  relations: { src: number, dest: number, winner: number }[] | undefined;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  numberOfWinnersFirstLevel: number = 1;

  @Input()
  level: number;

  totalTeams: number;

  cosa: number[] = [];

  coordinates: {
    x1: number;
    y1: number;
    x2: number;
    y2: number;
    winner: number;
    color: string;
  }[] = [];

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
    if (this.relations) {
      for (let r of this.relations) {
        this.coordinates.push({
          x1: this.getArrowX1Coordinate(this.level, r.src),
          y1: this.getArrowY1Coordinate(this.level, r.src, r.winner),
          x2: this.getArrowX2Coordinate(this.level + 1, r.dest),
          y2: this.getArrowY2Coordinate(this.level + 1, r.src, r.dest),
          winner: r.winner,
          color: r.winner == 0 ? 'var(--component-color)' : 'var(--secondary-team-arrow)'
        });
      }
    }
  }

  getArrowX1Coordinate(level: number, group: number): number {
    return 0;
  }

  getArrowY1Coordinate(level: number, group: number, winner: number): number {
    return this.getGroupTopSeparation(level, group, this.groupsByLevel) + this.getGroupHigh(level, group) / 2;
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

  getArrowX2Coordinate(column: number, group: number): number {
    return BracketsMeasures.LEVEL_SEPARATION;
  }

  getArrowY2Coordinate(column: number, sourceGroupIndex: number, destinationGroupIndex: number): number {
    return this.getGroupTopSeparation(column, destinationGroupIndex, this.groupsByLevel) + this.getGroupHigh(column, sourceGroupIndex) / 2;
  }

}
