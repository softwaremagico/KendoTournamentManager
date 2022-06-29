import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../../services/participant.service";
import {Tournament} from "../../../models/tournament";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";
import {RoleType} from "../../../models/role-type";
import {RoleService} from "../../../services/role.service";
import {MessageService} from "../../../services/message.service";
import {Role} from "../../../models/role";
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  roleTypes: RoleType[] = RoleType.toArray();
  participants = new Map<RoleType, Participant[]>();

  constructor(public dialogRef: MatDialogRef<TournamentRolesComponent>,
              public participantService: ParticipantService, public roleService: RoleService,
              private messageService: MessageService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  getParticipantsContainer(role: RoleType): Participant[] {
    if (this.participants.get(role) === undefined) {
      this.participants.set(role, []);
    }
    return this.participants.get(role)!;
  }

  ngOnInit(): void {
    let participantsRequest = this.participantService.getAll();
    let roleRequests = this.roleService.getFromTournamentAndTypes(this.tournament.id!, RoleType.toArray());

    forkJoin([participantsRequest, roleRequests]).subscribe(([participants, roles]) => {
      //Get roles
      for (let roleType of this.roleTypes) {
        const rolesFromType: Role[] = roles.filter(role => role.roleType === roleType);
        this.participants.set(roleType, rolesFromType.map(role => role.participant));
        //Get participants and subtract the ones already with roles.
        const participantsIds: (number | undefined)[] = this.participants.get(roleType)!.map(participant => participant.id);
        participants = participants.filter((participantWithoutRole) => !participantsIds
          .includes(participantWithoutRole.id));
      }
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = participants;
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  private transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

  removeRole(event: CdkDragDrop<Participant[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    const participant: Participant = event.container.data[event.currentIndex]
    this.roleService.deleteByParticipantAndTournament(participant, this.tournament).subscribe(() => {
      this.messageService.infoMessage("Role for '" + participant.name + " " + participant.lastname + "' removed.");
    });
    this.userListData.participants.push(participant);
    this.userListData.filteredParticipants.sort((a, b) => a.lastname.localeCompare(b.lastname));
    this.userListData.participants.sort((a, b) => a.lastname.localeCompare(b.lastname));
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>, roleName: RoleType) {
    const participant: Participant = this.transferCard(event);
    const role: Role = new Role();
    role.tournament = this.tournament;
    role.participant = participant;
    role.roleType = roleName;
    this.roleService.add(role).subscribe(_role => {
      this.messageService.infoMessage("Role '" + _role.roleType + "' for '" + participant.name + " " + participant.lastname + "' stored.");
    });
    console.log('participants', this.userListData.participants.indexOf(participant))
    console.log('filteredParticipants', this.userListData.filteredParticipants.indexOf(participant))
    if (this.userListData.participants.includes(participant)) {
      this.userListData.participants.splice(this.userListData.participants.indexOf(participant), 1);
    }
    if (this.userListData.filteredParticipants.includes(participant)) {
      this.userListData.filteredParticipants.splice(this.userListData.filteredParticipants.indexOf(participant), 1);
    }
  }
}

