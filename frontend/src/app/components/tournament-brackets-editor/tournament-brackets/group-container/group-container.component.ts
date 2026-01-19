import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../../models/team";
import {Group} from "../../../../models/group";
import {GroupService} from "../../../../services/group.service";
import {GroupsUpdatedService} from "../groups-updated.service";
import {Tournament} from "../../../../models/tournament";
import {BracketsMeasures} from "../brackets-measures";

@Component({
  selector: 'group-container',
  templateUrl: './group-container.component.html',
  styleUrls: ['./group-container.component.scss']
})
export class GroupContainerComponent implements OnInit {

  @Input()
  group: Group;

  @Input()
  index: number;

  @Input()
  level: number;

  @Input()
  groupsByLevel: Map<number, Group[]>;

  @Input()
  tournament: Tournament;

  @Input()
  droppingDisabled: boolean;

  @Input()
  getGroupTopSeparation: (level: number, group: number, groupsByLevel: Map<number, Group[]>) => number;

  @Input()
  getGroupLeftSeparation: (level: number, group: number) => number;

  @Input()
  getGroupHigh: (level: number, group: number) => number;

  @Input()
  selected: boolean = false;

  @Output()
  elementClicked: EventEmitter<Group> = new EventEmitter<Group>();

  totalTeams: number;

  groupHigh: number = BracketsMeasures.GROUP_HIGH;

  estimatedTeams: number;

  constructor(private groupService: GroupService, private groupsUpdatedService: GroupsUpdatedService) {
  }

  ngOnInit(): void {
    this.groupsUpdatedService.areTotalTeamsNumberUpdated.subscribe((_totalTeams: number): void => {
      this.totalTeams = _totalTeams;
      this.groupHigh = this.getGroupHigh(this.level, this.index);
      this.estimatedTeams = Math.ceil(this.totalTeams / this.groupsByLevel.get(0)!.length);
    });
  }

  dropTeam(event: CdkDragDrop<Team[], any>): void {
    this.transferCard(event);
    this.groupHigh = this.getGroupHigh(this.level, this.index);
    this.groupService.setTeamsToGroup(this.groupsByLevel.get(this.level)![this.index]!.id!, this.groupsByLevel.get(this.level)![this.index].teams)
      .subscribe((_group: Group): void => {
        this.groupsByLevel.get(this.level)![this.index] = _group;
      })
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

  checkDroppedElement(level: number, teamsByGroup: number, teams: Team[], droppingDisabled: boolean): (drag: CdkDrag, drop: CdkDropList) => boolean {
    return function (drag: CdkDrag, drop: CdkDropList): boolean {
      console.log(!droppingDisabled, level === 0 , teams.length < teamsByGroup)
      return !droppingDisabled && level === 0 && teams.length < teamsByGroup;
    };
  }

  isLocked(): boolean {
    return this.level !== 0;
  }

  isClicked() {
    this.elementClicked.emit(this.group);
  }
}
