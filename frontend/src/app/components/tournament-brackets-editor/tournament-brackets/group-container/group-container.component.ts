import {Component, Input, OnInit} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../../models/team";
import {Group} from "../../../../models/group";
import {TournamentBracketsComponent} from "../tournament-brackets.component";
import {GroupService} from "../../../../services/group.service";

@Component({
  selector: 'app-group-container',
  templateUrl: './group-container.component.html',
  styleUrls: ['./group-container.component.scss']
})
export class GroupContainerComponent implements OnInit {

  @Input()
  index: number;

  @Input()
  level: number;

  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  getGroupTopSeparation: (level: number, group: number, groupsByLevel: Map<number, Group[]>) => number;

  @Input()
  getGroupLeftSeparation: (level: number, group: number) => number;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  @Input()
  totalTeams: number;

  groupHigh: number = TournamentBracketsComponent.GROUP_HIGH;

  teams: Team[] = [];

  estimatedTeams: number;

  constructor(private groupService: GroupService) {
  }

  ngOnInit(): void {
    this.groupHigh = this.getGroupHigh(this.level, this.index);
    this.estimatedTeams = Math.ceil(this.totalTeams / this.groupsByLevel.get(0)!.length);
  }

  dropTeam(event: CdkDragDrop<Team[], any>): void {
    const team: Team = this.transferCard(event);
    this.groupHigh = this.getGroupHigh(this.level, this.index);
    this.groupsByLevel.get(this.level)![this.index].teams = this.teams;
    //this.groupService.setTeamsToGroup(this.groupsByLevel!.get(this.level)![this.index]!.id!, this.teams);
  }

  private transferCard(event: CdkDragDrop<Team[], any>): Team {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

  checkDroppedElement(level: number, teamsByGroup: number, teams: Team[]): (drag: CdkDrag, drop: CdkDropList) => boolean {
    return function (drag: CdkDrag, drop: CdkDropList): boolean {
      return level === 0 && teams.length < teamsByGroup + 1;
    };
  }

  isLocked(): boolean {
    return this.level !== 0;
  }

}
