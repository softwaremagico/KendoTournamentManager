import {Component, Input, OnInit} from '@angular/core';
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../../models/team";
import {Group} from "../../../../models/group";

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
  groupsByLevel: Map<number, Group[]>

  @Input()
  getGroupTopSeparation: (column: number, group: number, groupsByLevel: Map<number, Group[]>) => number;

  @Input()
  getGroupLeftSeparation: (column: number, group: number) => number;

  teams: Team[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  dropTeam(event: CdkDragDrop<Team[], any>): void {
    const team: Team = this.transferCard(event);
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

}
