import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {Team} from "../../../models/team";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";

@Component({
  standalone: false,
  selector: 'member-selector',
  templateUrl: './member-selector.component.html',
  styleUrls: ['./member-selector.component.scss']
})
export class MemberSelectorComponent implements OnChanges {

  @Input()
  team: Team;

  @Input()
  selections: number = 1;

  @Input()
  autoSelectSingleMember: boolean = false;

  @Output() selectedMember: EventEmitter<Participant[]> = new EventEmitter<Participant[]>();

  members: Participant[];
  selectedMembers: Participant[] = [];

  ngOnChanges(): void {
    //Refresh automatically the team.
    const teamMembers: (Participant | undefined)[] = this.team?.members ?? [];
    //Removing undefined members.
    this.members = [...teamMembers.flatMap(p => p ? [p] : [])];

    //If there is only one possible option, select it automatically.
    if (this.autoSelectSingleMember && this.selections === 1 && this.members.length === 1) {
      const onlyMember: Participant = this.members[0];
      if (!this.isSelected(onlyMember) || this.selectedMembers.length !== 1) {
        this.selectedMembers = [onlyMember];
        this.selectedMember.emit(this.selectedMembers);
      }
    }
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

  selectUser(participant: Participant) {
    const isAlreadySelected = this.selectedMembers.some(selected => selected?.id === participant?.id);
    if (this.selections > 1) {
      if (isAlreadySelected) {
        this.selectedMembers = this.selectedMembers.filter(selected => selected?.id !== participant?.id);
      } else {
        this.selectedMembers.push(participant);
      }
    } else {
      this.selectedMembers = [];
      this.selectedMembers.push(participant);
    }
    this.selectedMember.emit(this.selectedMembers);
  }

  isSelected(participant: Participant): boolean {
    return this.selectedMembers.some(selected => selected?.id === participant?.id);
  }
}
