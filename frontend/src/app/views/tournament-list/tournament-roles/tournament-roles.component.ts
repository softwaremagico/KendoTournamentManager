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
import {TranslateService} from "@ngx-translate/core";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";

@Component({
  selector: 'app-tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent extends RbacBasedComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  roleTypes: RoleType[] = RoleType.toArray();
  participants = new Map<RoleType, Participant[]>();

  constructor(public dialogRef: MatDialogRef<TournamentRolesComponent>,
              public participantService: ParticipantService, public roleService: RoleService,
              private messageService: MessageService, public translateService: TranslateService,
              rbacService: RbacService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    super(rbacService);
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
        this.participants.set(roleType, rolesFromType.map(role => role.participant).sort(function (a, b) {
          return a.lastname.localeCompare(b.lastname) || a.name.localeCompare(b.name);
        }));
        //Get participants and subtract the ones already with roles.
        const participantsIds: (number | undefined)[] = this.participants.get(roleType)!.map(participant => participant.id);
        participants = participants.filter((participantWithoutRole) => !participantsIds
          .includes(participantWithoutRole.id));
      }
      participants.sort(function (a, b) {
        return a.lastname.localeCompare(b.lastname) || a.name.localeCompare(b.name);
      });
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = participants;
    });
  }

  closeDialog() {
    this.dialogRef.close();
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

  removeRole(event: CdkDragDrop<Participant[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    const participant: Participant = event.container.data[event.currentIndex]
    this.roleService.deleteByParticipantAndTournament(participant, this.tournament).subscribe(() => {
      this.messageService.infoMessage("infoRoleDeleted");
    });
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
      this.messageService.infoMessage("infoRoleStored");
    });
    if (this.userListData.participants.includes(participant)) {
      this.userListData.participants.splice(this.userListData.participants.indexOf(participant), 1);
    }
    if (this.userListData.filteredParticipants.includes(participant)) {
      this.userListData.filteredParticipants.splice(this.userListData.filteredParticipants.indexOf(participant), 1);
    }
  }

  downloadPDF() {
    if (this.tournament && this.tournament.id) {
      this.roleService.getRolesByTournament(this.tournament.id).subscribe((pdf: Blob) => {
        const blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);
        let pwa = window.open(downloadURL);
        if (!pwa || pwa.closed || typeof pwa.closed == 'undefined') {
          alert(this.translateService.instant('disablePopUpBlocker'));
        }
      });
    }
  }

  countRole(roleType: RoleType): number {
    if (!this.participants || !this.participants.get(roleType)) {
      return 0;
    }
    return this.participants.get(roleType)!.length;
  }
}

