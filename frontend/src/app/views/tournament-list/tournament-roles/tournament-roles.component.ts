import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../../services/participant.service";
import {Tournament} from "../../../models/tournament";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";
import {RoleType} from "../../../models/RoleType";
import {RoleService} from "../../../services/role.service";
import {MessageService} from "../../../services/message.service";
import {Role} from "../../../models/role";

@Component({
  selector: 'app-tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  roleTypes = RoleType.getKeys();
  participants = new Map<string, Participant[]>();

  constructor(public dialogRef: MatDialogRef<TournamentRolesComponent>,
              public participantService: ParticipantService, public roleService: RoleService,
              private messageService: MessageService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
    for (let role in this.roleTypes) {
      this.participants.set(role, []);
    }
  }

  getParticipantsContainer(role: string): Participant[] {
    if (this.participants.get(role) === undefined) {
      this.participants.set(role, []);
    }
    return this.participants.get(role)!;
  }

  ngOnInit(): void {
    this.participantService.getAll().subscribe(participants => {
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = participants;
    });

    this.roleService.getFromTournamentAndTypes(this.tournament.id!, RoleType.toArray()).subscribe(roles => {
      for (let roleType in this.roleTypes) {
        const rolesFromType: Role[] = roles.filter(role => role.roleType == roleType);
        this.participants.set(roleType, rolesFromType.map(role => role.participant));
      }
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  getRealIndex(currentIndex: number): number {
    //If filter is used, the index of the user is incorrect. Convert it
    return this.userListData.participants.indexOf(this.userListData.filteredParticipants[currentIndex]);
  }

  transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      this.getRealIndex(event.previousIndex),
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
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>, roleName: string) {
    const participant: Participant = this.transferCard(event);
    const role: Role = new Role();
    role.tournament = this.tournament;
    role.participant = participant;
    role.roleType = (<any>RoleType)[roleName];
    this.roleService.add(role).subscribe(role => {
      this.messageService.infoMessage("Role '" + roleName + "' for '" + participant.name + " " + participant.lastname + "' stored.");
    });
  }
}
