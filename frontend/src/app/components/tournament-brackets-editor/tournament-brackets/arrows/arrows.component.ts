import {Component, Input, OnInit} from '@angular/core';
import {GroupsUpdatedService} from "../groups-updated.service";
import {Group} from "../../../../models/group";
import {BracketsMeasures} from "../brackets-measures";
import {TournamentBracketsComponent} from "../tournament-brackets.component";
import {ShiaijoComponent} from "../shiaijo/shiaijo.component";

@Component({
  selector: 'arrows',
  templateUrl: './arrows.component.html',
  styleUrls: ['./arrows.component.scss']
})
export class ArrowsComponent implements OnInit {

  public static readonly ARROW_SIZE: number = 30;
  arrow_size: number = ArrowsComponent.ARROW_SIZE;

  @Input()
  relations: { src: number, dest: number, winner: number }[] | undefined;

  @Input()
  getGroupTopSeparation: (level: number, group: number, groupsByLevel: Map<number, Group[]> | null) => number;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  @Input()
  getGroupYCoordinate: (level: number, group: number) => number;

  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  numberOfWinnersFirstLevel: number = 1;

  @Input()
  level: number;

  totalTeams: number;

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
    this.groupsUpdatedService.areGroupsUpdated.subscribe((_groups: Group[]): void => {
      this.groupsByLevel = TournamentBracketsComponent.convert(_groups);
      this.updateSize();
    })
    this.groupsUpdatedService.areTotalTeamsNumberUpdated.subscribe((_totalTeams: number): void => {
      if (_totalTeams != this.totalTeams) {
        this.totalTeams = _totalTeams;
      }
    })
    this.groupsUpdatedService.areRelationsUpdated.subscribe((_relations: Map<number, {
      src: number,
      dest: number,
      winner: number
    }[]>): void => {
      this.relations = _relations.get(this.level);
      this.generateCoordinates();
    })
    this.updateSize();
  }

  updateSize() {
    this.height = this.getTotalHigh();
    this.width = this.getTotalWidth();
  }

  getTotalHigh(): number {
    let maxGroupsByLevel: number = 0;
    let levelWithMaxGroups: number = 0;
    for (let key of this.groupsByLevel.keys()) {
      if (this.groupsByLevel.get(key)!.length > maxGroupsByLevel) {
        maxGroupsByLevel = this.groupsByLevel.get(key)!.length;
        levelWithMaxGroups = key;
      }
    }
    return Math.max(this.getGroupYCoordinate(levelWithMaxGroups, maxGroupsByLevel - 1), this.getGroupYCoordinate(0, this.groupsByLevel.get(0)!.length)) + this.getGroupHigh(0, 0);
  }

  getTotalWidth(): number {
    let maxLevel = 0;
    for (let key of this.groupsByLevel.keys()) {
      if (key > maxLevel) {
        maxLevel = key;
      }
    }
    if (this.level == maxLevel) {
      //Last level has no arrows
      return 0;
    }
    return (BracketsMeasures.levelSeparation(this.groupsByLevel.get(0)?.length));
  }

  generateCoordinates(): void {
    this.coordinates = [];
    if (this.relations && this.relations) {
      for (let r of this.relations) {
        this.coordinates.push({
          x1: this.getArrowX1Coordinate(this.level, r.src),
          y1: this.getArrowY1Coordinate(this.level, r.src, r.dest, r.winner),
          x2: this.getArrowX2Coordinate(this.level + 1, r.dest),
          y2: this.getArrowY2Coordinate(this.level + 1, r.src, r.dest, r.winner),
          winner: r.winner,
          color: r.winner == 0 ? 'var(--component-color)' : 'var(--secondary-team-arrow)'
        });
      }
    }
  }

  getArrowX1Coordinate(level: number, group: number): number {
    return 0;
  }

  getArrowY1Coordinate(level: number, sourceGroupIndex: number, destinationGroupIndex: number, winner: number): number {
    return this.getGroupYCoordinate(level, sourceGroupIndex) + this.getGroupHigh(level, sourceGroupIndex) / 2
      + this.getOutboundWinnerPixedDifference(destinationGroupIndex, this.getOutboundArrowsDest(sourceGroupIndex), winner)
      + (this.getNumberOfShiaijosCrossed(level, sourceGroupIndex) * this.getShiaijoLabelHeight());
  }

  getArrowX2Coordinate(column: number, group: number): number {
    return BracketsMeasures.levelSeparation(this.groupsByLevel.get(0)?.length);
  }

  getArrowY2Coordinate(level: number, sourceGroupIndex: number, destinationGroupIndex: number, winner: number): number {
    return this.getGroupYCoordinate(level, destinationGroupIndex) + this.getGroupHigh(level, destinationGroupIndex) / 2
      + this.getInboundWinnerPixedDifference(sourceGroupIndex, this.getInboundArrowsSrc(destinationGroupIndex), winner)
      + (this.getNumberOfShiaijosCrossed(level, destinationGroupIndex) * this.getShiaijoLabelHeight());
  }

  getInboundArrowsSrc(group: number): number[] {
    const arrows: number[] = [];
    if (this.relations) {
      for (let rel of this.relations) {
        if (rel.dest == group) {
          arrows.push(rel.src);
        }
      }
    }
    return arrows;
  }

  getOutboundArrowsDest(group: number): number[] {
    const arrows: number[] = [];
    if (this.relations) {
      for (let rel of this.relations) {
        if (rel.src == group) {
          arrows.push(rel.dest);
        }
      }
    }
    return arrows;
  }

  getOutboundWinnerPixedDifference(source: number, arrows: number[], winner: number): number {
    if (arrows[0] == arrows[1]) {
      if (winner == 0) {
        return -BracketsMeasures.WINNER_ARROWS_SEPARATION;
      } else {
        return BracketsMeasures.WINNER_ARROWS_SEPARATION;
      }
    }
    if (source == Math.max(...arrows)) {
      return BracketsMeasures.WINNER_ARROWS_SEPARATION;
    }
    return -BracketsMeasures.WINNER_ARROWS_SEPARATION;
  }

  getInboundWinnerPixedDifference(destination: number, arrows: number[], winner: number): number {
    //From same group
    if (arrows[0] == arrows[1]) {
      if (winner == 0) {
        return -BracketsMeasures.WINNER_ARROWS_SEPARATION;
      } else {
        return BracketsMeasures.WINNER_ARROWS_SEPARATION;
      }
    }
    if (destination == Math.max(...arrows)) {
      return BracketsMeasures.WINNER_ARROWS_SEPARATION;
    }
    return -BracketsMeasures.WINNER_ARROWS_SEPARATION;
  }

  getShiaijoLabelHeight(): number {
    return ShiaijoComponent.SHIAIJO_HEIGHT + 5;
  }


  getNumberOfShiaijosCrossed(level: number, group: number): number {
    let shiaijos: number = 0;
    if (this.groupsByLevel) {
      for (let g = 0; g < Math.min(this.groupsByLevel.get(level)!.length, group); g++) {
        if (g < this.groupsByLevel.get(level)!.length - 1
          && this.groupsByLevel.get(level)![g].shiaijo != this.groupsByLevel.get(level)![g + 1].shiaijo) {
          shiaijos++;
        }
      }
    }
    return shiaijos;
  }

}
