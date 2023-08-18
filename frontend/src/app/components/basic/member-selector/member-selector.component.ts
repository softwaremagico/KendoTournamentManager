import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {Team} from "../../../models/team";
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";

@Component({
  selector: 'app-member-selector',
  templateUrl: './member-selector.component.html',
  styleUrls: ['./member-selector.component.scss']
})
export class MemberSelectorComponent implements OnChanges {

  @Input()
  team: Team;

  members: Participant[];
  selectedMembers: Participant[] = [];

  @Output() selectedMember: EventEmitter<Participant> = new EventEmitter<Participant>();

  ngOnChanges(): void {
    //Refresh automatically the team.
    const teamMembers: (Participant | undefined)[] = this.team.members;
    //Removing undefined members.
    this.members = [...teamMembers.flatMap(p => p ? [p] : [])];
  }

  checkDroppedElement(item: CdkDrag<Participant>, drop: CdkDropList): boolean {
    return drop.data.length === 0;
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>): void {
    this.transferCard(event);
    this.selectedMember.emit(this.selectedMembers[0]);
  }

  transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    //Only one member allowed.
    if (event.container.data.length === 0 || event.container.data !== this.selectedMembers) {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
    return event.container.data[event.currentIndex];
  }

}
