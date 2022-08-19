import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {Team} from "../../../models/team";
import {CdkDrag, CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";

@Component({
  selector: 'app-member-selector',
  templateUrl: './member-selector.component.html',
  styleUrls: ['./member-selector.component.scss']
})
export class MemberSelectorComponent implements OnInit, OnChanges {

  @Input()
  team: Team;

  members: Participant[];
  selectedMembers: Participant[] = [];

  @Output() selectedMember = new EventEmitter<Participant>();

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges() {
    //Refresh automatically the team.
    this.members = this.team.members;
  }

  checkDroppedElement(item: CdkDrag<Participant>) {
    //TODO (softwaremagico): fitler drops.
    // if (!this.team.members.includes(item.data)) {
    //   return false;
    // }
    // return this.member === undefined;
    return true;
    //return this.selectedMembers.length === 0;
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>) {
    this.transferCard(event);
    this.selectedMember.emit(this.selectedMembers[0]);
  }

  transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

}
